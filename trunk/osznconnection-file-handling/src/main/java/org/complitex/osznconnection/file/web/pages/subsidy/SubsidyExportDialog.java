package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.organization.web.component.OrganizationPickerPanel;
import org.odlabs.wiquery.ui.datepicker.scope.DefaultJsScopeUiDatePickerDateTextEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.01.14 20:06
 */
public class SubsidyExportDialog extends Panel {
    private Dialog dialog;

    private static class ExportParameter implements Serializable{
        private int step = 0;
        private Integer exportType = 0;
        private Date date;
        private String type;

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public Integer getExportType() {
            return exportType;
        }

        public void setExportType(Integer exportType) {
            this.exportType = exportType;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public SubsidyExportDialog(String id) {
        super(id);

        dialog = new Dialog("dialog"){
            {getOptions().putLiteral("width", "auto");}
        };
        dialog.setTitle(new ResourceModel("export_title"));
        dialog.setWidth(400);
        add(dialog);

        final IModel<ExportParameter> model = new CompoundPropertyModel<>(new ExportParameter());

        final Form<ExportParameter> form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        dialog.add(form);

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

        WebMarkupContainer actionContainer = new WebMarkupContainer("action_container");
        structureContainer.setOutputMarkupId(true);
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
        WebMarkupContainer holderContainer = new WebMarkupContainer("holder_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 0;
            }
        };
        exportContainer.add(holderContainer);

        holderContainer.add(new OrganizationPickerPanel("organization_picker", Model.of(1l), Arrays.asList(0l,1l,2l)));

        //Район
        WebMarkupContainer districtContainer = new WebMarkupContainer("district_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 1;
            }
        };
        exportContainer.add(districtContainer);

        //Организация
        WebMarkupContainer organizationContainer = new WebMarkupContainer("organization_container"){
            @Override
            public boolean isVisible() {
                return model.getObject().getExportType() == 2;
            }
        };
        exportContainer.add(organizationContainer);

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
            public boolean isVisible() {
                return model.getObject().getStep() == 2;
            }
        });
    }

    public void open(AjaxRequestTarget target){
        dialog.open(target);
    }
}
