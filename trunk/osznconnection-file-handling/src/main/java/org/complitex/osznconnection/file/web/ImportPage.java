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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.file.service.ImportService;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 18:46
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class ImportPage extends TemplatePage {
    @EJB
    private ImportService importService;

    @EJB(name = "OrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    private int stopTimer = 0;

    public ImportPage() {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        container.add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        container.add(form);

        //Организация
        final IModel<DomainObject> organizationModel = new Model<DomainObject>();

        DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
                organizationModel,
                new LoadableDetachableModel<List<DomainObject>>() {

                    @Override
                    protected List<DomainObject> load() {
                        return organizationStrategy.getAllCalculationCentres(getLocale());
                    }
                }, new DomainObjectDisableAwareRenderer() {

                    @Override
                    public Object getDisplayValue(DomainObject object) {
                        return organizationStrategy.displayDomainObject(object, getLocale());
                    }
                });
        organization.setRequired(true);
        form.add(organization);

        Button process = new Button("process"){
            @Override
            public void onSubmit() {
                if (!importService.isProcessing()) {
                    importService.process(organizationModel.getObject().getId());

                    container.add(newTimer());
                }
            }

            @Override
            public boolean isVisible() {
                return !importService.isProcessing();
            }
        };
        form.add(process);

        container.add(new Label("header_dictionary_import", getStringOrKey("dictionary_import")){
            @Override
            public boolean isVisible() {
                return !importService.getMessages().isEmpty();
            }
        });

        container.add(new ListView<ImportMessage>("dictionary_import",
                new LoadableDetachableModel<List<? extends ImportMessage>>() {
                    @Override
                    protected List<? extends ImportMessage> load() {
                        return importService.getMessages();
                    }
                }){
            {
                setReuseItems(false);
            }

            @Override
            protected void populateItem(ListItem<ImportMessage> item) {
                ImportMessage message = item.getModelObject();

                String m = getStringOrKey(message.getImportFile().name()) +
                        " (" + message.getIndex() + "/" + message.getCount() + ")";

                item.add(new Label("message", m));
            }
        });

        container.add(new Label("header_correction_import", getStringOrKey("correction_import")){
            @Override
            public boolean isVisible() {
                return !importService.getCorrectionMessages().isEmpty();
            }
        });

        container.add(new ListView<ImportMessage>("correction_import",
                new LoadableDetachableModel<List<? extends ImportMessage>>() {
                    @Override
                    protected List<? extends ImportMessage> load() {
                        return importService.getCorrectionMessages();
                    }
                }){
            {
                setReuseItems(false);
            }

            @Override
            protected void populateItem(ListItem<ImportMessage> item) {
                ImportMessage message = item.getModelObject();

                String m = getStringOrKey(message.getImportFile().name()) +
                        " (" + message.getIndex() + "/" + message.getCount() + ")";

                item.add(new Label("message", m));
            }
        });

        container.add(new Label("error", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return importService.getErrorMessage();
            }
        }));
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(){
        stopTimer = 0;

        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                if (!importService.isProcessing()){
                    stopTimer++;
                }

                if (stopTimer > 2){
                    if (importService.isSuccess()){
                        info(getString("success"));
                    }
                    stop();
                }
            }
        };
    }
}
