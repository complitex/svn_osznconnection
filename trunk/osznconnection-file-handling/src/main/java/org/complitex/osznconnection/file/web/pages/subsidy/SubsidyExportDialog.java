package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.complitex.dictionary.web.component.DatePicker;
import org.odlabs.wiquery.ui.datepicker.scope.DefaultJsScopeUiDatePickerDateTextEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.01.14 20:06
 */
public class SubsidyExportDialog extends Panel {
    private Dialog dialog;

    private class ExportParameter implements Serializable{
        private int step = 0;
        private Date date;
        private String type;
    }

    public SubsidyExportDialog(String id) {
        super(id);

        dialog = new Dialog("dialog");
        add(dialog);

        Form<ExportParameter> form = new Form<>("form", new CompoundPropertyModel<>(new ExportParameter()));
        form.setOutputMarkupId(true);
        dialog.add(form);

        WebMarkupContainer structureContainer = new WebMarkupContainer("structure_container");
        structureContainer.setOutputMarkupId(true);
        form.add(structureContainer);

        structureContainer.add(new DatePicker<>("date")
                .setChangeMonth(true)
                .setOnSelectEvent(new DefaultJsScopeUiDatePickerDateTextEvent(
                        "var month=$(\"#ui-datepicker-div .ui-datepicker-month :selected\").val();" +
                        "var year = $(\"#ui-datepicker-div .ui-datepicker-year :selected\").val();" +
                        "$(this).datepicker('setDate', new Date(year, month, 1));")));

    }

    public void open(AjaxRequestTarget target){
        dialog.open(target);
    }
}
