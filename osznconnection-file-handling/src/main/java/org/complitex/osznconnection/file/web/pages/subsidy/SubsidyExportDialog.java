package org.complitex.osznconnection.file.web.pages.subsidy;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.complitex.address.web.component.DistrictSelectPanel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.organization.web.component.OrganizationMultiselectPanel;
import org.complitex.organization.web.component.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.odlabs.wiquery.ui.datepicker.scope.DefaultJsScopeUiDatePickerDateTextEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import java.util.Arrays;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.01.14 20:06
 */
public class SubsidyExportDialog extends Panel {
    private static final TextTemplate CENTER_DIALOG_JS = new PackageTextTemplate(OrganizationPicker.class, "CenterDialog.js");

    private Dialog dialog;

    public SubsidyExportDialog(String id) {
        super(id);

        dialog = new Dialog("dialog"){
            {getOptions().putLiteral("width", "auto");}
        };
        dialog.setTitle(new ResourceModel("export_title"));
        dialog.setWidth(650);
        add(dialog);

        final IModel<SubsidyExportParameter> model = new CompoundPropertyModel<>(new SubsidyExportParameter());

        final Form<SubsidyExportParameter> form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        dialog.add(form);

        final FeedbackPanel feedbackPanel = new FeedbackPanel("messages");
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        //Дата и тип файла
        WebMarkupContainer structureContainer = new WebMarkupContainer("structure_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getStep() == 0;
            }
        };
        form.add(structureContainer);

        structureContainer.add(new DatePicker<>("date")
                .setChangeMonth(true)
                .setOnSelectEvent(new DefaultJsScopeUiDatePickerDateTextEvent(
                        "var month=$(\"#ui-datepicker-div .ui-datepicker-month :selected\").val();" +
                                "var year = $(\"#ui-datepicker-div .ui-datepicker-year :selected\").val();" +
                                "$(this).datepicker('setDate', new Date(year, month, 1));")));

        final WebMarkupContainer actionContainer = new WebMarkupContainer("action_container");
        actionContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(actionContainer);

        //Вариант выгрузки
        WebMarkupContainer exportTypeContainer = new WebMarkupContainer("export_type_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getStep() == 1;
            }
        };
        form.add(exportTypeContainer);

        exportTypeContainer.add(new RadioChoice<>("exportType", Arrays.asList(0,1,2), new IChoiceRenderer<Integer>() {
            @Override
            public Object getDisplayValue(Integer object) {
                switch (object){
                    case 0: return getString("export_type_holder");
                    case 1: return getString("export_type_district");
                    case 2: return getString("export_type_organization");
                }

                return null;
            }

            @Override
            public String getIdValue(Integer object, int index) {
                return object.toString();
            }
        }));

        //Выгрузка
        WebMarkupContainer exportContainer = new WebMarkupContainer("export_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getStep() == 2;
            }
        };
        form.add(exportContainer);

        //Балансодержатель
        exportContainer.add(new OrganizationMultiselectPanel("balance_holder",
                new PropertyModel<List<DomainObject>>(model, "balanceHolders"),
                Arrays.asList(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE)){

            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 0;
            }

            @Override
            protected void onAdd(AjaxRequestTarget target) {
                target.add(actionContainer.setVisible(false));
            }

            @Override
            protected void onSelect(AjaxRequestTarget target, DomainObject domainObject) {
                target.add(actionContainer.setVisible(true));
            }

            @Override
            protected void onCancel(AjaxRequestTarget target) {
                target.add(actionContainer.setVisible(true));
            }
        });

        //Район
        exportContainer.add(new DistrictSelectPanel("districts", new PropertyModel<List<Long>>(model, "districts")){
            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 1;
            }
        });

        //Организация
        exportContainer.add(new OrganizationMultiselectPanel("organization",
                new PropertyModel<List<DomainObject>>(model, "organizations"),
                Arrays.asList(OrganizationTypeStrategy.SERVICING_ORGANIZATION_TYPE)){
            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 2;
            }

            @Override
            protected void onAdd(AjaxRequestTarget target) {
                target.add(actionContainer.setVisible(false));
            }

            @Override
            protected void onSelect(AjaxRequestTarget target, DomainObject domainObject) {
                target.add(actionContainer.setVisible(true));
            }

            @Override
            protected void onCancel(AjaxRequestTarget target) {
                target.add(actionContainer.setVisible(true));
            }
        });

        actionContainer.add(new AjaxLink("back") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                int step = model.getObject().getStep();

                if (step > 0) {
                    model.getObject().setStep(step - 1);
                }else {
                    dialog.close(target);
                }

                target.add(form);
            }
        }.add(AttributeModifier.replace("value", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return model.getObject().getStep() > 0 ? getString("back") : getString("cancel");
            }
        })));

        actionContainer.add(new AjaxSubmitLink("next") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                model.getObject().setStep(model.getObject().getStep() + 1);

                target.add(form);

                target.appendJavaScript(CENTER_DIALOG_JS.asString(ImmutableMap.of("dialogId", dialog.getMarkupId())));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
            }

            @Override
            public boolean isVisible() {
                return model.getObject().getStep() < 2;
            }
        });

        actionContainer.add(new AjaxButton("export") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
            }

            @Override
            public boolean isVisible() {
                return model.getObject().getStep() == 2;
            }
        });
    }

    public void open(AjaxRequestTarget target){
        dialog.open(target);
    }
}
