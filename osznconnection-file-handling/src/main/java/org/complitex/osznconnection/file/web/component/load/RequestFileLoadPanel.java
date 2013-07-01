package org.complitex.osznconnection.file.web.component.load;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.model.OrganizationModel;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.template.web.template.TemplateSession;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;

public final class RequestFileLoadPanel extends Panel {

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private OsznOrganizationStrategy organizationStrategy;

    @EJB
    private SessionBean sessionBean;

    private final Dialog dialog;
    private static final String MONTH_COMPONENT_ID = "monthComponent";

    public static enum MonthParameterViewMode {

        RANGE, EXACT, HIDDEN
    }

    public RequestFileLoadPanel(String id, IModel<String> title, final IRequestFileLoader loader,
            final MonthParameterViewMode monthParameterViewMode) {
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

            Long userOrganizationId;

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
        Long currentUserOrganizationId = sessionBean.getCurrentUserOrganizationId(getSession());
        userOrganizationContainer.setVisible(currentUserOrganizationId == null);

        final DropDownChoice<Integer> year = new YearDropDownChoice("year", new Model<Integer>());
        year.setRequired(true);
        form.add(year);

        WebMarkupContainer monthParameterContainer = new WebMarkupContainer("monthParameterContainer");
        monthParameterContainer.setVisible(monthParameterViewMode != MonthParameterViewMode.HIDDEN);
        form.add(monthParameterContainer);

        final IModel<MonthRange> monthRangeModel = new Model<>();
        Component monthComponent;
        switch (monthParameterViewMode) {
            case RANGE:
                monthComponent = new MonthRangePanel(MONTH_COMPONENT_ID, monthRangeModel);
                break;
            case EXACT:
                IModel<Integer> monthPickerModel = new Model<Integer>() {

                    @Override
                    public void setObject(Integer month) {
                        if (month != null) {
                            monthRangeModel.setObject(new MonthRange(month));
                        } else {
                            monthRangeModel.setObject(null);
                        }
                    }

                    @Override
                    public Integer getObject() {
                        MonthRange monthRange = monthRangeModel.getObject();
                        return monthRange != null ? monthRange.getMonthFrom() : null;
                    }
                };
                monthComponent = new MonthPickerPanel(MONTH_COMPONENT_ID, monthPickerModel);
                break;
            case HIDDEN:
            default:
                monthComponent = new EmptyPanel(MONTH_COMPONENT_ID);
                break;
        }
        monthParameterContainer.add(monthComponent);

        //Загрузить
        AjaxButton load = new AjaxButton("load", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final DomainObject oszn = osznModel.getObject();
                Long mainUserOrganizationId = sessionBean.getCurrentUserOrganizationId(RequestFileLoadPanel.this.getSession());
                long currentUserOrganizationId = mainUserOrganizationId != null ? mainUserOrganizationId
                        : userOrganizationModel.getOrganizationId();

                DateParameter dateParameter;
                if (monthParameterViewMode == MonthParameterViewMode.HIDDEN) {
                    dateParameter = new DateParameter(year.getModelObject());
                } else {
                    final MonthRange monthRange = monthRangeModel.getObject();
                    dateParameter = new DateParameter(year.getModelObject(),
                            monthRange.getMonthFrom(), monthRange.getMonthTo());
                }
                loader.load(currentUserOrganizationId, oszn.getId(), dateParameter, target);

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
