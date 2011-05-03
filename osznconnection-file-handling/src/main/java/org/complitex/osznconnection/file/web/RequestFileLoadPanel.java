package org.complitex.osznconnection.file.web;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.12.10 16:52
 */
public class RequestFileLoadPanel extends Panel {
    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    private final Dialog dialog;

    public static interface ILoader extends Serializable{
        void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year);
    }

    public RequestFileLoadPanel(String id, String title, final ILoader loader){
        this(id, title, loader, true);
    }

    public RequestFileLoadPanel(String id, String title, final ILoader loader, final boolean showDatePeriod) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(420);
        dialog.setMinHeight(100);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setOutputMarkupId(true);
        dialog.setOutputMarkupPlaceholderTag(true);
        dialog.setTitle(title);
        dialog.setVisible(false);
        add(dialog);

        dialog.add(new FeedbackPanel("messages"));

        //Форма
        Form form = new Form("form");
        dialog.add(form);

        //Организация
        IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        final IModel<DomainObject> organizationModel = new Model<DomainObject>();
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization", organizationModel,
                osznsModel, renderer);
        organization.setRequired(true);
        form.add(organization);

        final DropDownChoice<Integer> year = new YearDropDownChoice("year", new Model<Integer>());
        year.setRequired(showDatePeriod);
        form.add(year);

        WebMarkupContainer datePeriodContainer = new WebMarkupContainer("date_period_container");
        datePeriodContainer.setVisible(showDatePeriod);
        form.add(datePeriodContainer);

        //Период
        final DropDownChoice<Integer> from = new MonthDropDownChoice("from", new Model<Integer>());
        from.setRequired(showDatePeriod);
        datePeriodContainer.add(from);

        final DropDownChoice<Integer> to = new MonthDropDownChoice("to", new Model<Integer>());
        to.setRequired(showDatePeriod);
        datePeriodContainer.add(to);

        //Загрузить
        Button load = new Button("load") {

            @Override
            public void onSubmit() {
                int f = showDatePeriod ? from.getModelObject() : 0;
                int t = showDatePeriod ? to.getModelObject() : 0;

                if (t < f && showDatePeriod) {
                    error(getString("error.to_less_then_from"));
                    return;
                }

                DomainObject oszn = organizationModel.getObject();
                loader.load(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t, year.getModelObject());
//                getSession().info(getString("info.start_loading"));

                dialog.setAutoOpen(false);
                dialog.close();
            }
        };
        form.add(load);

        //Отмена
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                dialog.setAutoOpen(false);
                dialog.setVisible(false);
                dialog.close();
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }

    public void open(){
        dialog.setAutoOpen(true);
        dialog.setVisible(true);
    }
}

