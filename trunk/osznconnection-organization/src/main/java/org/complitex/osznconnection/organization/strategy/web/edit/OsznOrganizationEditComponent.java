package org.complitex.osznconnection.organization.strategy.web.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.converter.StringConverter;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.IDisableAwareChoiceRenderer;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;
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
    @EJB
    private StringCultureBean stringBean;
    private WebMarkupContainer serviceAssociationsContainer;
    private WebMarkupContainer dataSourceContainer;
    private IModel<RemoteDataSource> dataSourceModel;

    public OsznOrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected OsznOrganization getDomainObject() {
        return (OsznOrganization) super.getDomainObject();
    }

    @Override
    protected void init() {
        super.init();

        final boolean enabled = enabled();

        final OsznOrganization organization = getDomainObject();

        //reference to `service_association` helper table. It is user organization only attribute.
        final ServiceAssociationList serviceAssociationList = organization.getServiceAssociationList();
        if (isNew()) {
            serviceAssociationList.addNew();
        }
        serviceAssociationsContainer = new WebMarkupContainer("serviceAssociationsContainer");
        serviceAssociationsContainer.setOutputMarkupPlaceholderTag(true);
        add(serviceAssociationsContainer);

        final List<DomainObject> allServiceProviderTypes = serviceProviderTypeStrategy.getAll(getLocale());
        final DomainObjectDisableAwareRenderer serviceProviderTypeRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return serviceProviderTypeStrategy.displayDomainObject(object, getLocale());
            }
        };

        final List<DomainObject> allCalculationCentres = osznOrganizationStrategy.getAllCalculationCentres(getLocale());
        final DomainObjectDisableAwareRenderer calculationCenterRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return osznOrganizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        final WebMarkupContainer serviceAssociationsUpdateContainer = new WebMarkupContainer("serviceAssociationsUpdateContainer");
        serviceAssociationsUpdateContainer.setOutputMarkupId(true);
        serviceAssociationsContainer.add(serviceAssociationsUpdateContainer);

        ListView<ServiceAssociation> serviceAssociations = new AjaxRemovableListView<ServiceAssociation>("serviceAssociations",
                serviceAssociationList) {

            @Override
            protected void populateItem(ListItem<ServiceAssociation> item) {
                final WebMarkupContainer fakeContainer = new WebMarkupContainer("fakeContainer");
                item.add(fakeContainer);

                final ServiceAssociation serviceAssociation = item.getModelObject();

                //service provider type
                IModel<DomainObject> serviceProviderTypeModel = new Model<DomainObject>() {

                    @Override
                    public DomainObject getObject() {
                        Long serviceProviderTypeId = serviceAssociation.getServiceProviderTypeId();
                        if (serviceProviderTypeId != null) {
                            for (DomainObject o : allServiceProviderTypes) {
                                if (serviceProviderTypeId.equals(o.getId())) {
                                    return o;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public void setObject(DomainObject serviceProviderType) {
                        serviceAssociation.setServiceProviderTypeId(serviceProviderType != null
                                ? serviceProviderType.getId() : null);
                    }
                };
                //initialize model:
                Long serviceProviderTypeId = serviceAssociation.getServiceProviderTypeId();
                if (serviceProviderTypeId != null) {
                    for (DomainObject o : allServiceProviderTypes) {
                        if (serviceProviderTypeId.equals(o.getId())) {
                            serviceProviderTypeModel.setObject(o);
                        }
                    }
                }

                IModel<List<DomainObject>> selectServiceProviderTypeModel = new AbstractReadOnlyModel<List<DomainObject>>() {

                    @Override
                    public List<DomainObject> getObject() {
                        List<DomainObject> selectList = Lists.newArrayList();

                        Long serviceProviderTypeId = serviceAssociation.getServiceProviderTypeId();
                        for (DomainObject spt : allServiceProviderTypes) {
                            if (!serviceAssociationList.containsServiceProviderType(spt.getId())
                                    || spt.getId().equals(serviceProviderTypeId)) {
                                selectList.add(spt);
                            }
                        }
                        return selectList;
                    }
                };

                DisableAwareDropDownChoice<DomainObject> serviceProviderType =
                        new DisableAwareDropDownChoice<DomainObject>("serviceProviderType", serviceProviderTypeModel,
                        selectServiceProviderTypeModel, serviceProviderTypeRenderer);
                serviceProviderType.setEnabled(enabled);
                serviceProviderType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.addComponent(serviceAssociationsUpdateContainer);
                    }
                });
                item.add(serviceProviderType);

                //calculation center
                IModel<DomainObject> calculationCenterModel = new Model<DomainObject>() {

                    @Override
                    public DomainObject getObject() {
                        Long calculationCenterId = serviceAssociation.getCalculationCenterId();
                        if (calculationCenterId != null) {
                            for (DomainObject o : allCalculationCentres) {
                                if (calculationCenterId.equals(o.getId())) {
                                    return o;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public void setObject(DomainObject calculationCenter) {
                        serviceAssociation.setCalculationCenterId(calculationCenter != null
                                ? calculationCenter.getId() : null);
                    }
                };
                //initialize model:
                Long calculationCenterId = serviceAssociation.getCalculationCenterId();
                if (calculationCenterId != null) {
                    for (DomainObject o : allCalculationCentres) {
                        if (calculationCenterId.equals(o.getId())) {
                            calculationCenterModel.setObject(o);
                        }
                    }
                }

                final DisableAwareDropDownChoice<DomainObject> calculationCenter =
                        new DisableAwareDropDownChoice<DomainObject>("calculationCenter", calculationCenterModel,
                        allCalculationCentres, calculationCenterRenderer);
                calculationCenter.setEnabled(enabled);
                calculationCenter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.addComponent(calculationCenter);
                    }
                });
                item.add(calculationCenter);

                //remove link
                addRemoveLink("removeServiceAssociation", item, null, serviceAssociationsUpdateContainer).setVisible(enabled);
            }

            @Override
            protected boolean approveRemoval(ListItem<ServiceAssociation> item) {
                return serviceAssociationList.size() > 1;
            }
        };
        serviceAssociationsUpdateContainer.add(serviceAssociations);
        AjaxLink<Void> addServiceAssociation = new AjaxLink<Void>("addServiceAssociation") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                serviceAssociationList.addNew();
                target.addComponent(serviceAssociationsUpdateContainer);
            }
        };
        addServiceAssociation.setVisible(enabled);
        serviceAssociationsContainer.add(addServiceAssociation);
        serviceAssociationsContainer.setVisible(isUserOrganization());

        //reference to jdbc data source. Only for calculation centres.
        dataSourceContainer = new WebMarkupContainer("dataSourceContainer");
        dataSourceContainer.setOutputMarkupPlaceholderTag(true);
        add(dataSourceContainer);
        final IModel<String> dataSourceLabelModel = new ResourceModel("dataSourceLabel");
        dataSourceContainer.add(new Label("dataSourceLabel", dataSourceLabelModel));
        dataSourceModel = new Model<RemoteDataSource>();

        final String currentDataSource = AttributeUtil.getStringValue(organization, IOsznOrganizationStrategy.DATA_SOURCE);
        final List<RemoteDataSource> allDataSources = osznOrganizationStrategy.findRemoteDataSources(currentDataSource);

        for (RemoteDataSource ds : allDataSources) {
            if (ds.isCurrent()) {
                dataSourceModel.setObject(ds);
                break;
            }
        }

        DisableAwareDropDownChoice<RemoteDataSource> dataSource =
                new DisableAwareDropDownChoice<RemoteDataSource>("dataSource", dataSourceModel, allDataSources,
                new IDisableAwareChoiceRenderer<RemoteDataSource>() {

                    @Override
                    public Object getDisplayValue(RemoteDataSource remoteDataSource) {
                        return remoteDataSource.getDataSource();
                    }

                    @Override
                    public boolean isDisabled(RemoteDataSource remoteDataSource) {
                        return !remoteDataSource.isExist();
                    }

                    @Override
                    public String getIdValue(RemoteDataSource remoteDataSource, int index) {
                        return remoteDataSource.getDataSource();
                    }
                });
        dataSource.setRequired(true);
        dataSource.setLabel(dataSourceLabelModel);
        dataSource.setEnabled(enabled);
        dataSourceContainer.add(dataSource);
        dataSourceContainer.setVisible(isCalculationCenter());
    }

    @Override
    protected void onOrganizationTypeChanged(AjaxRequestTarget target) {
        super.onOrganizationTypeChanged(target);

        //service association
        boolean serviceAssociationContainerWasVisible = serviceAssociationsContainer.isVisible();
        serviceAssociationsContainer.setVisible(isUserOrganization());
        boolean serviceAssociationContainerVisibleNow = serviceAssociationsContainer.isVisible();
        if (serviceAssociationContainerWasVisible ^ serviceAssociationContainerVisibleNow) {
            target.addComponent(serviceAssociationsContainer);
        }

        //data source
        boolean dataSourceContainerWasVisible = dataSourceContainer.isVisible();
        dataSourceContainer.setVisible(isCalculationCenter());
        boolean dataSourceContainerVisibleNow = dataSourceContainer.isVisible();
        if (dataSourceContainerWasVisible ^ dataSourceContainerVisibleNow) {
            target.addComponent(dataSourceContainer);
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
    public boolean isUserOrganization() {
        return super.isUserOrganization();
    }

    @Override
    protected boolean isDistrictRequired() {
        return isOszn();
    }

    @Override
    protected boolean isDistrictVisible() {
        return super.isDistrictVisible() || isOszn();
    }

    public boolean isServiceAssociationListEmpty() {
        return getDomainObject().getServiceAssociationList().isEmpty();
    }

    public boolean isServiceAssociationListHasNulls() {
        return getDomainObject().getServiceAssociationList().hasNulls();
    }

    public Set<String> getDuplicateServiceProviderTypes() {
        Set<Long> duplicates = getDomainObject().getServiceAssociationList().getDuplicateServiceProviderTypeIds();
        if (duplicates != null) {
            Set<String> result = Sets.newHashSet();
            final List<DomainObject> allServiceProviderTypes = serviceProviderTypeStrategy.getAll(getLocale());
            for (long serviceProviderTypeId : duplicates) {
                for (DomainObject spt : allServiceProviderTypes) {
                    if (spt.getId().equals(serviceProviderTypeId)) {
                        result.add(serviceProviderTypeStrategy.displayDomainObject(spt, getLocale()));
                        break;
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Override
    protected void onPersist() {
        super.onPersist();

        final DomainObject organization = getDomainObject();

        if (!isUserOrganization()) {
            organization.removeAttribute(IOsznOrganizationStrategy.SERVICE_ASSOCIATIONS);
        }

        if (isCalculationCenter()) {
            //data source
            String dataSource = dataSourceModel.getObject().getDataSource();
            dataSource = dataSource != null ? dataSource.toUpperCase() : null;
            stringBean.getSystemStringCulture(organization.getAttribute(IOsznOrganizationStrategy.DATA_SOURCE).getLocalizedValues()).
                    setValue(new StringConverter().toString(dataSource));
        } else {
            getDomainObject().removeAttribute(IOsznOrganizationStrategy.DATA_SOURCE);
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
