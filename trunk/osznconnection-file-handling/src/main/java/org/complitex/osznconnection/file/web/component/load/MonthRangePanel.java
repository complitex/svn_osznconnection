/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.load;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionary.web.component.MonthDropDownChoice;

/**
 *
 * @author Artem
 */
public final class MonthRangePanel extends FormComponentPanel<MonthRange> {

    private Integer monthFrom;
    private Integer monthTo;
    private final MonthDropDownChoice from;
    private final MonthDropDownChoice to;

    public MonthRangePanel(String id, IModel<MonthRange> model) {
        super(id, model);

        //Период
        from = new MonthDropDownChoice("from", new PropertyModel<Integer>(this, "monthFrom"));
        from.setOutputMarkupId(true);
        from.setRequired(true);
        add(from);

        to = new MonthDropDownChoice("to", new PropertyModel<Integer>(this, "monthTo"));
        to.setOutputMarkupId(true);
        to.setRequired(true);
        add(to);

        from.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                to.setModelObject(from.getModelObject());

                target.add(to);
            }
        });
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        MonthRange monthRange = getModelObject();
        if (monthRange != null) {
            monthFrom = monthRange.getMonthFrom();
            monthTo = monthRange.getMonthTo();
        }
    }

    @Override
    protected void convertInput() {
        Integer monthFrom = from.getConvertedInput();
        Integer monthTo = to.getConvertedInput();
        if (monthFrom != null && monthTo != null) {
            MonthRange newMonthRange = null;
            if (monthFrom > monthTo) {
                error(getString("error.to_less_than_from"));
            } else {
                newMonthRange = new MonthRange(monthFrom, monthTo);
            }
            setConvertedInput(newMonthRange);
        }
    }
}
