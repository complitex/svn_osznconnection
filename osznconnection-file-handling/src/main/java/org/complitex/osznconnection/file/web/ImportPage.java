package org.complitex.osznconnection.file.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.file.entity.CorrectionImportFile;
import org.complitex.osznconnection.file.service.ImportService;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.ownership.entity.OwnershipImportFile;
import org.complitex.osznconnection.privilege.entity.PrivilegeImportFile;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.*;

import static org.complitex.address.entity.AddressImportFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 18:46
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class ImportPage extends TemplatePage {
    @EJB
    private ImportService importService;

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    private int stopTimer = 0;

    public ImportPage() {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        final IModel<List<IImportFile>> dictionaryModel = new ListModel<IImportFile>();
        final IModel<List<IImportFile>> correctionModel = new ListModel<IImportFile>();

        container.add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        container.add(form);

        //Справочники
        List<IImportFile> dictionaryList = new ArrayList<IImportFile>();
        Collections.addAll(dictionaryList, values());
        Collections.addAll(dictionaryList, PrivilegeImportFile.values());
        Collections.addAll(dictionaryList, OwnershipImportFile.values());

        form.add(new CheckBoxMultipleChoice<IImportFile>("dictionary", dictionaryModel, dictionaryList,
                new IChoiceRenderer<IImportFile>() {
                    @Override
                    public Object getDisplayValue(IImportFile object) {

                        return object.getFileName() + getStatus(importService.getDictionaryMessage(object));
                    }

                    @Override
                    public String getIdValue(IImportFile object, int index) {
                        return object.name();
                    }
                }));

        //Организация
        final IModel<DomainObject> organizationModel = new Model<DomainObject>();

        final DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
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
        form.add(organization);

        //Коррекции
        List<IImportFile> correctionList = new ArrayList<IImportFile>();
        Collections.addAll(correctionList, CITY, DISTRICT, STREET_TYPE, STREET, BUILDING);
        Collections.addAll(correctionList, CorrectionImportFile.values());

        form.add(new CheckBoxMultipleChoice<IImportFile>("corrections", correctionModel, correctionList,
                new IChoiceRenderer<IImportFile>() {
                    @Override
                    public Object getDisplayValue(IImportFile object) {

                        return object.getFileName() + getStatus(importService.getCorrectionMessage(object));
                    }

                    @Override
                    public String getIdValue(IImportFile object, int index) {
                        return object.name();
                    }
                }));

        //Кнопка импортировать
        Button process = new Button("process"){
            @Override
            public void onSubmit() {
                if (!correctionModel.getObject().isEmpty() && organization.getDefaultModelObject() == null){
                    error(getStringOrKey("error_organization_required"));
                    return;
                }

                if (!importService.isProcessing()) {
                    importService.process(dictionaryModel.getObject(), correctionModel.getObject(),
                            organizationModel.getObject() != null ? organizationModel.getObject().getId() : null);

                    container.add(newTimer());
                }
            }

            @Override
            public boolean isVisible() {
                return !importService.isProcessing();
            }
        };
        form.add(process);

        //Ошибки
        container.add(new Label("error", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return importService.getErrorMessage();
            }
        }){
            @Override
            public boolean isVisible() {
                return importService.getErrorMessage() != null;
            }
        });
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

    private String getStatus(ImportMessage im){
        if (im != null) {
            if (im.getIndex() < 1 && !importService.isProcessing()){
                return " - " + getStringOrKey("error");
            }else if (im.getIndex() == im.getCount()){
                return " - " + getStringFormat("complete", im.getIndex());
            }else{
                return " - " + getStringFormat("processing", im.getIndex(), im.getCount());
            }
        }

        return "";
    }

}
