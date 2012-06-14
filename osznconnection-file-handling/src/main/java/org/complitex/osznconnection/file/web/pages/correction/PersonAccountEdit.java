/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.Lists;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean;
import org.complitex.osznconnection.file.web.model.OrganizationModel;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Страница для редактирования записей в локальной таблице номеров л/c.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PersonAccountEdit extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(PersonAccountEdit.class);
    public static final String CORRECTION_ID = "correction_id";
    @EJB
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private OsznSessionBean osznSessionBean;
    private Long correctionId;
    private PersonAccount personAccount;

    public PersonAccountEdit(PageParameters params) {
        this.correctionId = params.get(CORRECTION_ID).toOptionalLong();
        personAccount = personAccountLocalBean.findById(this.correctionId);

        //Проверка доступа к данным
        if (!osznSessionBean.isAuthorized(personAccount.getOsznId(), personAccount.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        init();
    }

    private void saveOrUpdate() {
        try {
            personAccountLocalBean.update(personAccount);
            back(true);
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    private void delete() {
        try {
            personAccountLocalBean.delete(personAccount);
            back(false);
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    private void back(boolean useScrolling) {
        if (useScrolling) {
            PageParameters backPageParameters = new PageParameters();
            backPageParameters.set(AbstractCorrectionList.SCROLL_PARAMETER, personAccount.getId());
            setResponsePage(PersonAccountList.class, backPageParameters);
        } else {
            setResponsePage(PersonAccountList.class);
        }
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        final IModel<PersonAccount> model = new CompoundPropertyModel<PersonAccount>(personAccount);
        Form<PersonAccount> form = new Form<PersonAccount>("form", model);
        add(form);

        form.add(new TextField<String>("puAccountNumber").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("lastName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("firstName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("middleName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("city").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("streetType").setEnabled(false));
        form.add(new TextField<String>("street").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("buildingNumber").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("buildingCorp").setEnabled(false));
        form.add(new TextField<String>("apartment").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("accountNumber").setRequired(true));

        final IModel<List<DomainObject>> allOsznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };

        final IModel<DomainObject> osznModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return model.getObject().getOsznId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                model.getObject().setOsznId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOsznsModel.getObject();
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> oszn = new DisableAwareDropDownChoice<DomainObject>("oszn", osznModel,
                allOsznsModel, organizationRenderer);
        oszn.setRequired(true);
        oszn.setEnabled(false);
        form.add(oszn);

        final IModel<List<DomainObject>> allCalculationCentresModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllCalculationCentres(getLocale());
            }
        };
        final IModel<DomainObject> calculationCenterModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return model.getObject().getCalculationCenterId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                model.getObject().setCalculationCenterId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allCalculationCentresModel.getObject();
            }
        };
        DisableAwareDropDownChoice<DomainObject> calculationCenter = new DisableAwareDropDownChoice<DomainObject>("calculationCenter",
                calculationCenterModel, allCalculationCentresModel, organizationRenderer);
        calculationCenter.setRequired(true);
        calculationCenter.setEnabled(false);
        form.add(calculationCenter);

        //user organization
        final IModel<List<DomainObject>> allUserOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getUserOrganizations(getLocale());
            }
        };

        final IModel<DomainObject> userOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return model.getObject().getUserOrganizationId();
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                model.getObject().setUserOrganizationId(userOrganizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allUserOrganizationsModel.getObject();
            }
        };
        final DisableAwareDropDownChoice<DomainObject> userOrganization = new DisableAwareDropDownChoice<DomainObject>("userOrganization",
                userOrganizationModel, allUserOrganizationsModel, organizationRenderer);
        userOrganization.setRequired(true);
        userOrganization.setEnabled(false);
        form.add(userOrganization);

        //save-cancel functional
        AjaxButton submit = new AjaxButton("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                saveOrUpdate();
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        };
        form.add(submit);
        Link<Void> cancel = new Link<Void>("cancel") {

            @Override
            public void onClick() {
                back(true);
            }
        };
        form.add(cancel);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                delete();
            }
        });
        return toolbar;
    }
}
