package org.complitex.osznconnection.file.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.osznconnection.file.web.model.OrganizationModel;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.template.TemplateSession;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.12.10 16:52
 */
public class RequestFileLoadPanel extends Panel {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private OsznSessionBean osznSessionBean;
    private final Dialog dialog;

    public static interface ILoader extends Serializable {

        void load(long userOrganizationId, long osznId, String districtCode, int monthFrom, int monthTo, int year,
                AjaxRequestTarget target);
    }

    public RequestFileLoadPanel(String id, IModel<String> title, ILoader loader) {
        this(id, title, loader, true);
    }

    public RequestFileLoadPanel(String id, IModel<String> title, final ILoader loader, final boolean showDatePeriod) {
        super(id);

        dialog = new Dialog("dialog") {

            {
                getOptions().putLiteral("width", "auto");
            }
        };
        dialog.setModal(true);
        dialog.setMinHeight(100);
        dialog.setTitle(title);
        add(dialog);

        WebMarkupContainer content = new WebMarkupContainer("content");
        dialog.add(content);

        final FeedbackPanel messages = new FeedbackPanel("messages", new ContainerFeedbackMessageFilter(content));
        messages.setOutputMarkupId(true);
        content.add(messages);

        //Форма
        Form<Void> form = new Form<Void>("form");
        content.add(form);

        //ОСЗН
        final IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        final IModel<DomainObject> osznModel = new Model<DomainObject>();
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        DisableAwareDropDownChoice<DomainObject> oszn = new DisableAwareDropDownChoice<DomainObject>("oszn", osznModel,
                osznsModel, organizationRenderer);
        oszn.setRequired(true);
        form.add(oszn);

        //user organization
        final WebMarkupContainer userOrganizationContainer = new WebMarkupContainer("userOrganizationContainer");
        form.add(userOrganizationContainer);
        final IModel<List<DomainObject>> userOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getUserOrganizations(getLocale());
            }
        };
        final OrganizationModel userOrganizationModel = new OrganizationModel() {

            private Long userOrganizationId;

            @Override
            public Long getOrganizationId() {
                return userOrganizationId;
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                this.userOrganizationId = userOrganizationId;
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return userOrganizationsModel.getObject();
            }
        };
        DisableAwareDropDownChoice<DomainObject> userOrganization = new DisableAwareDropDownChoice<DomainObject>(
                "userOrganization", userOrganizationModel, userOrganizationsModel, organizationRenderer);
        userOrganization.setRequired(true);
        userOrganizationContainer.add(userOrganization);
        Long currentUserOrganizationId = osznSessionBean.getCurrentUserOrganizationId(getSession());
        userOrganizationContainer.setVisible(currentUserOrganizationId == null);

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
        AjaxButton load = new AjaxButton("load", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                int f = showDatePeriod ? from.getModelObject() : 0;
                int t = showDatePeriod ? to.getModelObject() : 0;

                if (t < f && showDatePeriod) {
                    error(getString("error.to_less_then_from"));
                    return;
                }

                final DomainObject oszn = osznModel.getObject();

                Long mainUserOrganizationId = osznSessionBean.getCurrentUserOrganizationId(RequestFileLoadPanel.this.getSession());
                long currentUserOrganizationId = mainUserOrganizationId != null ? mainUserOrganizationId
                        : userOrganizationModel.getOrganizationId();
                loader.load(currentUserOrganizationId, oszn.getId(),
                        organizationStrategy.getDistrictCode(oszn), f, t, year.getModelObject(), target);

                target.add(messages);
                dialog.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        };
        form.add(load);

        //Отмена
        AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        };
        form.add(cancel);
    }

    public void open(AjaxRequestTarget target) {
        dialog.open(target);
    }

    @Override
    public TemplateSession getSession() {
        return (TemplateSession) super.getSession();
    }
}
