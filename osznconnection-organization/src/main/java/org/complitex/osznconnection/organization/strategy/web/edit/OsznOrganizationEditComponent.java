package org.complitex.osznconnection.organization.strategy.web.edit;

import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.converter.BooleanConverter;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.web.component.DomainObjectInputPanel.SimpleTypeModel;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;

/**
 * 
 * @author Artem
 */
public class OsznOrganizationEditComponent extends OrganizationEditComponent {

    private WebMarkupContainer currentCaculationCenterContainer;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private EntityBean entityBean;
    private Attribute currentCalculationCenterAttribute;

    public OsznOrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        super.init();

        final DomainObject organization = getDomainObject();
        // current calculation center flag
        currentCaculationCenterContainer = new WebMarkupContainer("currentCaculationCenterContainer");
        currentCaculationCenterContainer.setOutputMarkupPlaceholderTag(true);
        add(currentCaculationCenterContainer);

        Label currentCalculationCenterLabel = new Label("currentCalculationCenterLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                EntityAttributeType attributeType = entityBean.getEntity("organization").
                        getAttributeType(IOsznOrganizationStrategy.CURRENT_CALCULATION_CENTER);
                return stringBean.displayValue(attributeType.getAttributeNames(), getLocale());
            }
        });
        currentCaculationCenterContainer.add(currentCalculationCenterLabel);

        currentCalculationCenterAttribute = organization.getAttribute(IOsznOrganizationStrategy.CURRENT_CALCULATION_CENTER);
        IModel<Boolean> model = (currentCalculationCenterAttribute == null) ? new Model<Boolean>()
                : new SimpleTypeModel<Boolean>(stringBean.getSystemStringCulture(currentCalculationCenterAttribute.getLocalizedValues()),
                new BooleanConverter());
        CheckBox currentCalculationCenter = new CheckBox("currentCalculationCenter", model);
        currentCalculationCenter.setEnabled(enabled());
        currentCaculationCenterContainer.add(currentCalculationCenter);
        currentCaculationCenterContainer.setVisible(isCalculationCenter());
    }

    @Override
    protected void onOrganizationTypeChanged(AjaxRequestTarget target) {
        super.onOrganizationTypeChanged(target);

        //current calculation center container
        boolean currentCalculationCenterContainerWasVisible = currentCaculationCenterContainer.isVisible();
        currentCaculationCenterContainer.setVisible(isCalculationCenter());
        boolean currentCalculationCenterContainerVisibleNow = currentCaculationCenterContainer.isVisible();
        if (currentCalculationCenterContainerWasVisible ^ currentCalculationCenterContainerVisibleNow) {
            target.addComponent(currentCaculationCenterContainer);
        }
    }

    private boolean isCalculationCenter() {
        for (DomainObject organizationType : getOrganizationTypesModel().getObject()) {
            if (organizationType.getId().equals(OsznOrganizationTypeStrategy.CALCULATION_CENTER)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOszn() {
        for (DomainObject organizationType : getOrganizationTypesModel().getObject()) {
            if (organizationType.getId().equals(OsznOrganizationTypeStrategy.OSZN)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isDistrictRequired() {
        return isOszn();
    }

    @Override
    protected boolean isDistrictVisible() {
        return super.isDistrictVisible() || isOszn();
    }

    @Override
    protected void onPersist() {
        super.onPersist();
        if (!isCalculationCenter()) {
            getDomainObject().removeAttribute(IOsznOrganizationStrategy.CURRENT_CALCULATION_CENTER);
        }
    }

    @Override
    protected String getStrategyName() {
        return OsznOrganizationStrategy.OSZN_ORGANIZATION_STRATEGY_NAME;
    }

    @Override
    protected boolean isOrganizationTypeEnabled() {
        Long organizationId = getDomainObject().getId();
        return !(organizationId != null && (organizationId == IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID))
                && super.isOrganizationTypeEnabled();
    }
}
