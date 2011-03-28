package org.complitex.osznconnection.organization.strategy.web.edit;

import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.EJB;
import org.apache.wicket.Component;
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

        // current calculation center flag
        final DomainObject organization = getDomainObject();
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
        currentCaculationCenterContainer.add(currentCalculationCenter);
        boolean visibility = getCurrentCalculationCenterVisibility();
        currentCaculationCenterContainer.setVisible(visibility);
    }

    @Override
    protected Collection<Component> onTypeChanged() {
        Collection<Component> componentsToUpdate = super.onTypeChanged();
        if (componentsToUpdate == null) {
            componentsToUpdate = new ArrayList<Component>();
        }
        componentsToUpdate.add(currentCaculationCenterContainer);
        boolean visibility = getCurrentCalculationCenterVisibility();
        currentCaculationCenterContainer.setVisible(visibility);
        if (!visibility) {
            getDomainObject().removeAttribute(IOsznOrganizationStrategy.CURRENT_CALCULATION_CENTER);
        }

        return componentsToUpdate;
    }

    private boolean getCurrentCalculationCenterVisibility() {
        Long entityTypeId = getDomainObject().getEntityTypeId();
        if ((currentCalculationCenterAttribute != null) && (entityTypeId != null) && entityTypeId.equals(IOsznOrganizationStrategy.CALCULATION_CENTER)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isDistrictVisible(Long entityTypeId) {
        return super.isDistrictVisible(entityTypeId) || entityTypeId.equals(IOsznOrganizationStrategy.OSZN);
    }

    @Override
    protected boolean isDistrictNotRequired(Long entityTypeId) {
        return super.isDistrictNotRequired(entityTypeId);
    }
}
