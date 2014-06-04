package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileHistory;
import org.complitex.osznconnection.file.service.RequestFileHistoryBean;
import org.complitex.osznconnection.file.web.component.process.RequestFileStatusRenderer;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 11.12.13 0:46
 */
public class RequestFileHistoryPanel extends Panel {
    @EJB
    private RequestFileHistoryBean requestFileHistoryBean;

    private Dialog dialog;
    private WebMarkupContainer container;

    private RequestFile requestFile = new RequestFile();

    public RequestFileHistoryPanel(String id) {
        super(id);

        dialog = new Dialog("dialog");
        add(dialog);

        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        dialog.add(container);

        container.add(new ListView<RequestFileHistory>("list",
                new LoadableDetachableModel<List<? extends RequestFileHistory>>() {
                    @Override
                    protected List<? extends RequestFileHistory> load() {
                        return requestFileHistoryBean.getRequestFileHistories(requestFile.getId());
                    }
                }) {
            @Override
            protected void populateItem(ListItem<RequestFileHistory> item) {
                RequestFileHistory history = item.getModelObject();

                item.add(DateLabel.forDateStyle("date", Model.of(history.getDate()), "MM"));
                item.add(new Label("status", RequestFileStatusRenderer.render(history.getStatus(), getLocale())));
            }
        });

        dialog.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, RequestFile requestFile){
        this.requestFile = requestFile;

        target.add(container);
        dialog.open(target);
    }
}
