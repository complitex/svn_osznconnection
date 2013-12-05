package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.12.13 18:06
 */
public class SubsidyEditPanel extends Panel {
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    private Dialog dialog;
    private Subsidy subsidy = new Subsidy();
    private WebMarkupContainer container;

    public SubsidyEditPanel(String id, Component toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(680);
        dialog.setCloseOnEscape(false);
        add(dialog);

        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        dialog.add(container);

        FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        RequestFileDescription description = requestFileDescriptionBean.getFileDescription (RequestFileType.SUBSIDY);

        container.add(new ListView<RequestFileFieldDescription>("fields", description.getFields()) {
            @Override
            protected void populateItem(ListItem<RequestFileFieldDescription> item) {
                RequestFileFieldDescription fileFieldDescription = item.getModelObject();
                item.add(new Label("name", fileFieldDescription.getName()));
                item.add(new TextField<>("field", new PropertyModel<>(subsidy, "dbfFields[" + fileFieldDescription.getName()+"]")));
            }
        });
    }

    public void open(AjaxRequestTarget target, Subsidy subsidy){
        this.subsidy = subsidy;

        target.add(container);
        dialog.open(target);
    }
}
