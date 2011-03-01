package org.complitex.osznconnection.file.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.osznconnection.file.entity.AddressImportMessage;
import org.complitex.osznconnection.file.service.AddressImportService;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 18:46
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class AddressImportPage extends TemplatePage {
    @EJB
    private AddressImportService addressImportService;

    private int stopTimer = 0;

    public AddressImportPage() {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        Form form = new Form("form");
        container.add(form);

        Button process = new Button("process"){
            @Override
            public void onSubmit() {
                if (!addressImportService.isProcessing()) {
                    addressImportService.process();

                    container.add(newTimer());
                }
            }

            @Override
            public boolean isVisible() {
                return !addressImportService.isProcessing();
            }
        };
        form.add(process);

        container.add(new ListView<AddressImportMessage>("address_import",
                new LoadableDetachableModel<List<? extends AddressImportMessage>>() {
                    @Override
                    protected List<? extends AddressImportMessage> load() {
                        return addressImportService.getMessages();
                    }
                }){
            {
                setReuseItems(false);
            }

            @Override
            protected void populateItem(ListItem<AddressImportMessage> item) {
                AddressImportMessage message = item.getModelObject();

                String m = getStringOrKey("address_import") + ": " + getStringOrKey(message.getAddressImportFile()) +
                        " (" + message.getIndex() + "/" + message.getCount() + ")";

                item.add(new Label("message", m));
            }
        });

        container.add(new ListView<AddressImportMessage>("correction_import",
                new LoadableDetachableModel<List<? extends AddressImportMessage>>() {
                    @Override
                    protected List<? extends AddressImportMessage> load() {
                        return addressImportService.getCorrectionMessages();
                    }
                }){
            {
                setReuseItems(false);
            }

            @Override
            protected void populateItem(ListItem<AddressImportMessage> item) {
                AddressImportMessage message = item.getModelObject();

                String m = getStringOrKey("correction_import") + " " + getStringOrKey(message.getAddressImportFile()) +
                        " (" + message.getIndex() + "/" + message.getCount() + ")";

                item.add(new Label("message", m));
            }
        });

        container.add(new Label("error", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return addressImportService.getErrorMessage();
            }
        }));
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(){
        stopTimer = 0;

        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                if (!addressImportService.isProcessing()){
                    stopTimer++;
                }

                if (stopTimer > 2){
                    stop();
                }
            }
        };
    }
}
