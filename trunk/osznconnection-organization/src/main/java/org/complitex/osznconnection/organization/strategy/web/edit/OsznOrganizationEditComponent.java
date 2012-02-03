package org.complitex.osznconnection.organization.strategy.web.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.SortedSet;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;
import org.complitex.osznconnection.service_provider_type.strategy.ServiceProviderTypeStrategy;

/**
 * 
 * @author Artem
 */
public class OsznOrganizationEditComponent extends OrganizationEditComponent {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy osznOrganizationStrategy;
    @EJB
    private ServiceProviderTypeStrategy serviceProviderTypeStrategy;
    private WebMarkupContainer calculationCenterContainer;
    private IModel<DomainObject> calculationCenterModel;
    private WebMarkupContainer serviceProviderTypeContainer;
    private List<DomainObject> serviceProviderTypesModel;

    public OsznOrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        super.init();

        final boolean enabled = enabled();

        final DomainObject organization = getDomainObject();

        //reference to calculation center. Only for user organizations.
        calculationCenterContainer = new WebMarkupContainer("calculationCenterContainer");
        calculationCenterContainer.setOutputMarkupPlaceholderTag(true);
        add(calculationCenterContainer);
        final IModel<String> calculationCenterLabelModel = DomainObjectInputPanel.labelModel(osznOrganizationStrategy.getEntity().
                getAttributeType(IOsznOrganizationStrategy.CALCULATION_CENTER).getAttributeNames(), getLocale());
        calculationCenterContainer.add(new Label("calculationCenterLabel", calculationCenterLabelModel));

        final List<DomainObject> allCalculationCentres = osznOrganizationStrategy.getAllCalculationCentres(getLocale());
        calculationCenterModel = new Model<DomainObject>();

        DisableAwareDropDownChoice<DomainObject> calculationCenter =
                new DisableAwareDropDownChoice<DomainObject>("calculationCenter", calculationCenterModel,
                allCalculationCentres, new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return osznOrganizationStrategy.displayDomainObject(object, getLocale());
            }
        });
        calculationCenter.setRequired(true);
        calculationCenter.setLabel(calculationCenterLabelModel);
        calculationCenter.setEnabled(enabled);
        calculationCenterContainer.add(calculationCenter);

        final Attribute calculationCenterAttribute = organization.getAttribute(IOsznOrganizationStrategy.CALCULATION_CENTER);
        if (calculationCenterAttribute != null && calculationCenterAttribute.getValueId() != null) {
            for (DomainObject center : allCalculationCentres) {
                if (center.getId().equals(calculationCenterAttribute.getValueId())) {
                    calculationCenterModel.setObject(center);
                }
            }
        }
        calculationCenterContainer.setVisible(isUserOrganization());

        //reference to the set of service provider types. Only for calculation centres.
        serviceProviderTypeContainer = new WebMarkupContainer("serviceProviderTypeContainer");
        serviceProviderTypeContainer.setOutputMarkupPlaceholderTag(true);
        add(serviceProviderTypeContainer);
        final IModel<String> serviceProviderTypeLabelModel = DomainObjectInputPanel.labelModel(osznOrganizationStrategy.getEntity().
                getAttributeType(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE).getAttributeNames(), getLocale());
        serviceProviderTypeContainer.add(new Label("serviceProviderTypeLabel", serviceProviderTypeLabelModel));

        final List<DomainObject> allServiceProviderTypes = serviceProviderTypeStrategy.getAll(getLocale());
        final DomainObjectDisableAwareRenderer serviceProviderTypeRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return serviceProviderTypeStrategy.displayDomainObject(object, getLocale());
            }
        };

        serviceProviderTypesModel = Lists.newArrayList();
        final List<Attribute> serviceProviderTypeAttributes =
                organization.getAttributes(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE);

        for (Attribute a : serviceProviderTypeAttributes) {
            if (a.getValueId() != null) {
                for (DomainObject spt : allServiceProviderTypes) {
                    if (a.getValueId().equals(spt.getId())) {
                        serviceProviderTypesModel.add(spt);
                    }
                }
            }
        }
        if (serviceProviderTypesModel.isEmpty()) {
            serviceProviderTypesModel.add(null);
        }

        final WebMarkupContainer serviceProviderTypeUpdateContainer = new WebMarkupContainer("serviceProviderTypeUpdateContainer");
        serviceProviderTypeUpdateContainer.setOutputMarkupId(true);
        serviceProviderTypeContainer.add(serviceProviderTypeUpdateContainer);

        ListView<DomainObject> serviceProviderTypes = new AjaxRemovableListView<DomainObject>("serviceProviderTypes",
                serviceProviderTypesModel) {

            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                final WebMarkupContainer fakeContainer = new WebMarkupContainer("fakeContainer");
                item.add(fakeContainer);

                IModel<DomainObject> serviceProviderTypeModel = new Model<DomainObject>() {

                    @Override
                    public DomainObject getObject() {
                        int index = getCurrentIndex(fakeContainer);
                        return serviceProviderTypesModel.get(index);
                    }

                    @Override
                    public void setObject(DomainObject serviceProviderType) {
                        int index = getCurrentIndex(fakeContainer);
                        serviceProviderTypesModel.set(index, serviceProviderType);
                    }
                };
                serviceProviderTypeModel.setObject(item.getModelObject());

                DisableAwareDropDownChoice<DomainObject> serviceProviderType =
                        new DisableAwareDropDownChoice<DomainObject>("serviceProviderType", serviceProviderTypeModel,
                        allServiceProviderTypes, serviceProviderTypeRenderer);
                serviceProviderType.setEnabled(enabled);
                serviceProviderType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                    }
                });
                item.add(serviceProviderType);

                addRemoveLink("removeServiceProviderType", item, null, serviceProviderTypeUpdateContainer).setVisible(enabled);
            }
        };
        serviceProviderTypeUpdateContainer.add(serviceProviderTypes);
        AjaxLink<Void> addServiceProviderType = new AjaxLink<Void>("addServiceProviderType") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                DomainObject newServiceProviderType = null;
                serviceProviderTypesModel.add(newServiceProviderType);
                target.addComponent(serviceProviderTypeUpdateContainer);
            }
        };
        addServiceProviderType.setVisible(enabled);
        serviceProviderTypeContainer.add(addServiceProviderType);
        serviceProviderTypeContainer.setVisible(isCalculationCenter());
    }

    @Override
    protected void onOrganizationTypeChanged(AjaxRequestTarget target) {
        super.onOrganizationTypeChanged(target);

        //calculation center container
        boolean calculationCenterContainerWasVisible = calculationCenterContainer.isVisible();
        calculationCenterContainer.setVisible(isUserOrganization());
        boolean calculationCenterContainerVisibleNow = calculationCenterContainer.isVisible();
        if (calculationCenterContainerWasVisible ^ calculationCenterContainerVisibleNow) {
            target.addComponent(calculationCenterContainer);
        }

        //service provider type container
        boolean serviceProviderTypeContainerWasVisible = serviceProviderTypeContainer.isVisible();
        serviceProviderTypeContainer.setVisible(isCalculationCenter());
        boolean serviceProviderTypeContainerVisibleNow = serviceProviderTypeContainer.isVisible();
        if (serviceProviderTypeContainerWasVisible ^ serviceProviderTypeContainerVisibleNow) {
            target.addComponent(serviceProviderTypeContainer);
        }
    }

    public boolean isCalculationCenter() {
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

    public boolean isServiceProviderTypeEntered() {
        if (!isCalculationCenter()) {
            return false;
        }

        if (serviceProviderTypesModel.isEmpty()) {
            return false;
        }

        for (DomainObject serviceProviderType : serviceProviderTypesModel) {
            if (serviceProviderType != null && serviceProviderType.getId() != null && serviceProviderType.getId() > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onPersist() {
        super.onPersist();

        final DomainObject organization = getDomainObject();

        if (isUserOrganization()) {
            organization.getAttribute(IOsznOrganizationStrategy.CALCULATION_CENTER).
                    setValueId(calculationCenterModel.getObject().getId());
        } else {
            organization.removeAttribute(IOsznOrganizationStrategy.CALCULATION_CENTER);
        }

        if (isCalculationCenter()) {
            organization.removeAttribute(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE);

            SortedSet<Long> serviceProviderTypeIds = Sets.newTreeSet();
            for (DomainObject serviceProviderType : serviceProviderTypesModel) {
                if (serviceProviderType != null && serviceProviderType.getId() != null && serviceProviderType.getId() > 0) {
                    serviceProviderTypeIds.add(serviceProviderType.getId());
                }
            }

            long attributeId = 1;
            for (long serviceProviderTypeId : serviceProviderTypeIds) {
                Attribute attribute = new Attribute();
                attribute.setAttributeId(attributeId++);
                attribute.setAttributeTypeId(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE);
                attribute.setValueTypeId(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE);
                attribute.setValueId(serviceProviderTypeId);
                organization.addAttribute(attribute);
            }
        } else {
            getDomainObject().removeAttribute(IOsznOrganizationStrategy.SERVICE_PROVIDER_TYPE);
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
