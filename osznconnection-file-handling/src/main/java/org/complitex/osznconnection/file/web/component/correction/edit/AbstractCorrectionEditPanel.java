/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.service.CorrectionBean;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.web.model.OrganizationModel;
import org.complitex.osznconnection.file.web.pages.correction.AbstractCorrectionList;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;

/**
 * Абстрактная панель для редактирования коррекций.
 * @author Artem
 */
public abstract class AbstractCorrectionEditPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AbstractCorrectionEditPanel.class);
    @EJB
    private CorrectionBean correctionBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private OsznSessionBean osznSessionBean;
    private Long correctionId;
    private Correction correction;
    private WebMarkupContainer form;
    private Panel correctionInputPanel;
    private IModel<List<DomainObject>> allOuterOrganizationsModel;

    public AbstractCorrectionEditPanel(String id, String entity, Long correctionId) {
        super(id);
        this.correctionId = correctionId;
        if (isNew()) {
            correction = newObjectCorrection(entity);
        } else {
            correction = initObjectCorrection(entity, this.correctionId);
            correction.setEntity(entity);

            //Проверка доступа к данным
            if (!osznSessionBean.isAuthorized(correction.getOrganizationId(), correction.getUserOrganizationId())) {
                throw new UnauthorizedInstantiationException(this.getClass());
            }
        }

        init();
    }

    public boolean isNew() {
        return correctionId == null;
    }

    protected Correction initObjectCorrection(String entity, Long correctionId) {
        return correctionBean.findById(entity, correctionId);
    }

    protected Correction newObjectCorrection(String entity) {
        Correction c = new Correction(entity);
        c.setUserOrganizationId(osznSessionBean.getCurrentUserOrganizationId());
        return c;
    }

    protected Correction getModel() {
        return correction;
    }

    protected String displayCorrection() {
        return correction.getCorrection();
    }

    protected abstract IModel<String> internalObjectLabel(Locale locale);

    protected abstract Panel internalObjectPanel(String id);

    protected abstract String getNullObjectErrorMessage();

    protected String getNullCorrectionErrorMessage() {
        return new StringResourceModel("Required", Model.ofMap(ImmutableMap.of("label", getString("correction")))).getObject();
    }

    protected boolean freezeOrganization() {
        return false;
    }

    protected boolean checkCorrectionEmptiness() {
        return true;
    }

    protected final boolean validate() {
        boolean valid = preValidate();
        if (checkCorrectionEmptiness() && Strings.isEmpty(getModel().getCorrection())) {
            error(getNullCorrectionErrorMessage());
            valid = false;
        }

        if (getModel().getObjectId() == null) {
            error(getNullObjectErrorMessage());
            valid = false;
        }

        //calculation center must have null user organization but oszn must have non null user organization.
        if (isNew()) {
            boolean isOszn = false;
            //find outer organization object and determine whether it is oszn or calculation center.
            for (DomainObject outerOrganization : allOuterOrganizationsModel.getObject()) {
                if (outerOrganization.getId().equals(correction.getOrganizationId())) {
                    //choosen outer organization found.
                    isOszn = outerOrganization.getAttribute(IOsznOrganizationStrategy.ORGANIZATION_TYPE).getValueId().
                            equals(OsznOrganizationTypeStrategy.OSZN);
                    break;
                }
            }

            if (isOszn && correction.getUserOrganizationId() == null) {
                error(getString("oszn_must_have_user_organization"));
                valid = false;
            }
            
            if(!isOszn && correction.getUserOrganizationId() != null){
                error(getString("calculation_center_must_not_have_user_organization"));
                valid = false;
            }
        }


        if (valid && validateExistence()) {
            error(getString("exist"));
            valid = false;
        }
        return valid;
    }

    protected boolean preValidate() {
        return true;
    }

    protected boolean validateExistence() {
        return correctionBean.checkExistence(getModel());
    }

    protected void back(boolean useScrolling) {
        PageParameters backPageParameters = getBackPageParameters();
        if (backPageParameters == null && useScrolling) {
            backPageParameters = new PageParameters();
        }
        if (useScrolling) {
            backPageParameters.set(AbstractCorrectionList.SCROLL_PARAMETER, getModel().getId());
        }
        if (backPageParameters != null) {
            setResponsePage(getBackPageClass(), backPageParameters);
        } else {
            setResponsePage(getBackPageClass());
        }
    }

    protected abstract Class<? extends Page> getBackPageClass();

    protected abstract PageParameters getBackPageParameters();

    protected void saveOrUpdate() {
        try {
            if (isNew()) {
                save();
            } else {
                update();
            }
            back(true);
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    protected void save() {
        correctionBean.insert(correction);
    }

    protected void update() {
        correctionBean.update(correction);
    }

    protected void delete() {
        correctionBean.delete(correction);
    }

    public void executeDeletion() {
        try {
            delete();
            back(false);
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    protected WebMarkupContainer getFormContainer() {
        return form;
    }

    protected boolean isOrganizationCodeRequired() {
        return false;
    }

    protected Panel getCorrectionInputPanel(String id) {
        return new DefaultCorrectionInputPanel(id, new PropertyModel<String>(getModel(), "correction"));
    }

    protected abstract IModel<String> getTitleModel();

    protected void init() {
        IModel<String> titleModel = getTitleModel();
        add(new Label("title", titleModel));
        add(new Label("label", titleModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        form = new Form<Void>("form");
        add(form);

        WebMarkupContainer codeRequiredContainer = new WebMarkupContainer("codeRequiredContainer");
        form.add(codeRequiredContainer);

        boolean isOrganizationCodeRequired = isOrganizationCodeRequired();

        codeRequiredContainer.setVisible(isOrganizationCodeRequired);

        TextField<String> code = new TextField<String>("code", new PropertyModel<String>(correction, "code"));
        code.setRequired(isOrganizationCodeRequired);

        form.add(code);

        allOuterOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOuterOrganizations(getLocale());
            }
        };

        final IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                correction.setOrganizationId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOuterOrganizationsModel.getObject();
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        final DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
                outerOrganizationModel, allOuterOrganizationsModel, organizationRenderer);
        if (freezeOrganization()) {
            organization.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    organization.setEnabled(false);
                    target.add(organization);
                    if (correctionInputPanel.isVisible() && freezeOrganization()) {
                        target.add(correctionInputPanel);
                    }
                }
            });
        }
        organization.setRequired(true);
        organization.setEnabled(isNew());
        form.add(organization);

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
                return correction.getUserOrganizationId();
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                correction.setUserOrganizationId(userOrganizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allUserOrganizationsModel.getObject();
            }
        };
        final DisableAwareDropDownChoice<DomainObject> userOrganization = new DisableAwareDropDownChoice<DomainObject>("userOrganization",
                userOrganizationModel, allUserOrganizationsModel, organizationRenderer);
        userOrganization.setNullValid(true);
        userOrganization.setEnabled(isNew() && osznSessionBean.isAdmin());
        form.add(userOrganization);

        if (isNew()) {
            correction.setInternalOrganizationId(IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
        }

        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getItselfOrganization());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getInternalOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return internalOrganizations;
            }
        };
        DisableAwareDropDownChoice<DomainObject> internalOrganization = new DisableAwareDropDownChoice<DomainObject>("internalOrganization",
                internalOrganizationModel, internalOrganizations, organizationRenderer);
        internalOrganization.setEnabled(false);
        form.add(internalOrganization);

        form.add(new Label("internalObjectLabel", internalObjectLabel(getLocale())));
        form.add(internalObjectPanel("internalObject"));

        //correction input panel
        correctionInputPanel = getCorrectionInputPanel("correctionInput");
        correctionInputPanel.setVisible(isNew());
        if (correctionInputPanel.isVisible() && freezeOrganization()) {
            correctionInputPanel.setOutputMarkupId(true);
        }
        form.add(correctionInputPanel);
        //correction label
        form.add(new Label("correctionLabel", displayCorrection()).setVisible(!isNew()));

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                if (AbstractCorrectionEditPanel.this.validate()) {
                    saveOrUpdate();
                }
            }
        };
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back(true);
            }
        };
        form.add(cancel);
    }
}
