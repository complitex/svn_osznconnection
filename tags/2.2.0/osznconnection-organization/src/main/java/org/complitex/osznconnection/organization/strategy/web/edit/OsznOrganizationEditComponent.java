package org.complitex.osznconnection.organization.strategy.web.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.IDisableAwareChoiceRenderer;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;
import org.complitex.osznconnection.service_provider_type.strategy.ServiceProviderTypeStrategy;

import javax.ejb.EJB;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Artem
 */
public class OsznOrganizationEditComponent extends OrganizationEditComponent {
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private OsznOrganizationStrategy osznOrganizationStrategy;

    @EJB
    private ServiceProviderTypeStrategy serviceProviderTypeStrategy;

    @EJB
    private StringCultureBean stringBean;

    private WebMarkupContainer serviceAssociationsContainer;
    private WebMarkupContainer dataSourceContainer;
    private WebMarkupContainer loadSaveDirsContainer;
    private WebMarkupContainer edrpouContainer;
    private WebMarkupContainer rootDirectoryContainer;
    private WebMarkupContainer rootExportDirectoryContainer;
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

        final boolean isDisabled = isDisabled();
        final boolean enabled = enabled();

        final OsznOrganization organization = getDomainObject();

        //reference to `service_association` helper table. It is user organization only attribute.
        {
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
                            target.add(serviceAssociationsUpdateContainer);
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
                            target.add(calculationCenter);
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
                    target.add(serviceAssociationsUpdateContainer);
                }
            };
            addServiceAssociation.setVisible(enabled);
            serviceAssociationsContainer.add(addServiceAssociation);
            serviceAssociationsContainer.setVisible(isUserOrganization());
        }

        //reference to jdbc data source. Only for calculation centres.
        {
            dataSourceContainer = new WebMarkupContainer("dataSourceContainer");
            dataSourceContainer.setOutputMarkupPlaceholderTag(true);
            add(dataSourceContainer);
            final IModel<String> dataSourceLabelModel = new ResourceModel("dataSourceLabel");
            dataSourceContainer.add(new Label("dataSourceLabel", dataSourceLabelModel));
            dataSourceModel = new Model<>();

            final String currentDataSource = AttributeUtil.getStringValue(organization, OsznOrganizationStrategy.DATA_SOURCE);
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

        //load/save directories for request files. It is oszn only attributes.
        {
            loadSaveDirsContainer = new WebMarkupContainer("loadSaveDirsContainer");
            loadSaveDirsContainer.setOutputMarkupPlaceholderTag(true);
            add(loadSaveDirsContainer);

            loadSaveDirsContainer.add(new ListView<Long>("dir", OsznOrganizationStrategy.LOAD_SAVE_FILE_DIR_ATTRIBUTES) {

                @Override
                protected void populateItem(ListItem<Long> item) {
                    final long attributeTypeId = item.getModelObject();
                    final EntityAttributeType attributeType =
                            osznOrganizationStrategy.getEntity().getAttributeType(attributeTypeId);
                    item.add(new Label("label",
                            DomainObjectInputPanel.labelModel(attributeType.getAttributeNames(), getLocale())));
                    item.add(new WebMarkupContainer("required").setVisible(attributeType.isMandatory()));

                    Attribute attribute = organization.getAttribute(attributeTypeId);
                    if (attribute == null) {
                        attribute = new Attribute();
                        attribute.setAttributeTypeId(attributeTypeId);
                        attribute.setObjectId(organization.getId());
                        attribute.setAttributeId(1L);
                        attribute.setLocalizedValues(stringBean.newStringCultures());
                    }
                    item.add(DomainObjectInputPanel.newInputComponent("organization", getStrategyName(), organization,
                            attribute, getLocale(), isDisabled));
                }
            });

            //initial visibility
            loadSaveDirsContainer.setVisible(isOszn());
        }

        //EDRPOU. It is user organization only attribute.
        {
            edrpouContainer = new WebMarkupContainer("edrpouContainer");
            edrpouContainer.setOutputMarkupPlaceholderTag(true);
            add(edrpouContainer);
            final long attributeTypeId = OsznOrganizationStrategy.EDRPOU;
            Attribute attribute = organization.getAttribute(attributeTypeId);
            if (attribute == null) {
                attribute = new Attribute();
                attribute.setAttributeTypeId(attributeTypeId);
                attribute.setObjectId(organization.getId());
                attribute.setAttributeId(1L);
                attribute.setLocalizedValues(stringBean.newStringCultures());
            }
            final EntityAttributeType attributeType =
                    osznOrganizationStrategy.getEntity().getAttributeType(attributeTypeId);
            edrpouContainer.add(new Label("label",
                    DomainObjectInputPanel.labelModel(attributeType.getAttributeNames(), getLocale())));
            edrpouContainer.add(new WebMarkupContainer("required").setVisible(attributeType.isMandatory()));

            edrpouContainer.add(
                    DomainObjectInputPanel.newInputComponent("organization", getStrategyName(),
                    organization, attribute, getLocale(), isDisabled));

            //initial visibility
            edrpouContainer.setVisible(isUserOrganization());
        }

        //Root directory for loading and saving request files. It is user organization only attribute.
        {
            rootDirectoryContainer = new WebMarkupContainer("rootDirectoryContainer");
            rootDirectoryContainer.setOutputMarkupPlaceholderTag(true);
            add(rootDirectoryContainer);

            final long attributeTypeId = OsznOrganizationStrategy.ROOT_REQUEST_FILE_DIRECTORY;
            Attribute attribute = organization.getAttribute(attributeTypeId);
            if (attribute == null) {
                attribute = new Attribute();
                attribute.setAttributeTypeId(attributeTypeId);
                attribute.setObjectId(organization.getId());
                attribute.setAttributeId(1L);
                attribute.setLocalizedValues(stringBean.newStringCultures());
            }
            final EntityAttributeType attributeType =
                    osznOrganizationStrategy.getEntity().getAttributeType(attributeTypeId);
            rootDirectoryContainer.add(new Label("label",
                    DomainObjectInputPanel.labelModel(attributeType.getAttributeNames(), getLocale())));
            rootDirectoryContainer.add(new WebMarkupContainer("required").setVisible(attributeType.isMandatory()));

            rootDirectoryContainer.add(
                    DomainObjectInputPanel.newInputComponent("organization", getStrategyName(),
                    organization, attribute, getLocale(), isDisabled));

            //initial visibility
            rootDirectoryContainer.setVisible(isUserOrganization());
        }

        //Root Export directory for loading and saving request files. It is user organization only attribute.
        {
            rootExportDirectoryContainer = new WebMarkupContainer("rootExportDirectoryContainer");
            rootExportDirectoryContainer.setOutputMarkupPlaceholderTag(true);
            add(rootExportDirectoryContainer);

            final long attributeTypeId = OsznOrganizationStrategy.ROOT_EXPORT_DIRECTORY;
            Attribute attribute = organization.getAttribute(attributeTypeId);
            if (attribute == null) {
                attribute = new Attribute();
                attribute.setAttributeTypeId(attributeTypeId);
                attribute.setObjectId(organization.getId());
                attribute.setAttributeId(1L);
                attribute.setLocalizedValues(stringBean.newStringCultures());
            }
            final EntityAttributeType attributeType =
                    osznOrganizationStrategy.getEntity().getAttributeType(attributeTypeId);
            rootExportDirectoryContainer.add(new Label("label",
                    DomainObjectInputPanel.labelModel(attributeType.getAttributeNames(), getLocale())));
            rootExportDirectoryContainer.add(new WebMarkupContainer("required").setVisible(attributeType.isMandatory()));

            rootExportDirectoryContainer.add(
                    DomainObjectInputPanel.newInputComponent("organization", getStrategyName(),
                            organization, attribute, getLocale(), isDisabled));

            //initial visibility
            rootExportDirectoryContainer.setVisible(isUserOrganization());
        }
    }

    @Override
    protected void onOrganizationTypeChanged(AjaxRequestTarget target) {
        super.onOrganizationTypeChanged(target);

        //service association
        {
            boolean serviceAssociationContainerWasVisible = serviceAssociationsContainer.isVisible();
            serviceAssociationsContainer.setVisible(isUserOrganization());
            boolean serviceAssociationContainerVisibleNow = serviceAssociationsContainer.isVisible();
            if (serviceAssociationContainerWasVisible ^ serviceAssociationContainerVisibleNow) {
                target.add(serviceAssociationsContainer);
            }
        }

        //data source
        {
            boolean dataSourceContainerWasVisible = dataSourceContainer.isVisible();
            dataSourceContainer.setVisible(isCalculationCenter());
            boolean dataSourceContainerVisibleNow = dataSourceContainer.isVisible();
            if (dataSourceContainerWasVisible ^ dataSourceContainerVisibleNow) {
                target.add(dataSourceContainer);
            }
        }

        //load/save directory.
        {
            boolean loadSaveDirsContainerWasVisible = loadSaveDirsContainer.isVisible();
            loadSaveDirsContainer.setVisible(isOszn());
            boolean loadSaveDirsContainerVisibleNow = loadSaveDirsContainer.isVisible();
            if (loadSaveDirsContainerWasVisible ^ loadSaveDirsContainerVisibleNow) {
                target.add(loadSaveDirsContainer);
            }
        }

        //edrpou.
        {
            boolean edrpouContainerWasVisible = edrpouContainer.isVisible();
            edrpouContainer.setVisible(isUserOrganization());
            boolean edrpouContainerVisibleNow = edrpouContainer.isVisible();
            if (edrpouContainerWasVisible ^ edrpouContainerVisibleNow) {
                target.add(edrpouContainer);
            }
        }

        //root directory.
        {
            boolean rootDirectoryContainerWasVisible = rootDirectoryContainer.isVisible();
            rootDirectoryContainer.setVisible(isUserOrganization());
            boolean rootDirectoryContainerVisibleNow = rootDirectoryContainer.isVisible();
            if (rootDirectoryContainerWasVisible ^ rootDirectoryContainerVisibleNow) {
                target.add(rootDirectoryContainer);
            }
        }

        //root export directory.
        {
            boolean rootExportDirectoryContainerWasVisible = rootDirectoryContainer.isVisible();
            rootExportDirectoryContainer.setVisible(isUserOrganization());
            boolean rootExportDirectoryContainerVisibleNow = rootDirectoryContainer.isVisible();
            if (rootExportDirectoryContainerWasVisible ^ rootExportDirectoryContainerVisibleNow) {
                target.add(rootExportDirectoryContainer);
            }
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

        if (!isOszn()) {
            //load/save request file dirs
            for (long attributeTypeId : OsznOrganizationStrategy.LOAD_SAVE_FILE_DIR_ATTRIBUTES) {
                organization.removeAttribute(attributeTypeId);
            }
        }

        if (!isUserOrganization()) {
            //service associations
            organization.removeAttribute(OsznOrganizationStrategy.SERVICE_ASSOCIATIONS);

            //edrpou
            organization.removeAttribute(OsznOrganizationStrategy.EDRPOU);

            //root directory
            organization.removeAttribute(OsznOrganizationStrategy.ROOT_REQUEST_FILE_DIRECTORY);
            organization.removeAttribute(OsznOrganizationStrategy.ROOT_EXPORT_DIRECTORY);
        }

        if (!isCalculationCenter()) {
            //data source
            getDomainObject().removeAttribute(OsznOrganizationStrategy.DATA_SOURCE);
        } else {
            //data source
            String dataSource = dataSourceModel.getObject().getDataSource();
            stringBean.getSystemStringCulture(organization.getAttribute(OsznOrganizationStrategy.DATA_SOURCE).getLocalizedValues()).
                    setValue(new StringConverter().toString(dataSource));
        }
    }

    @Override
    protected String getStrategyName() {
        return OsznOrganizationStrategy.OSZN_ORGANIZATION_STRATEGY_NAME;
    }

    @Override
    protected boolean isOrganizationTypeEnabled() {
        Long organizationId = getDomainObject().getId();
        return !(organizationId != null && (organizationId.equals(OsznOrganizationStrategy.MODULE_ID)))
                && super.isOrganizationTypeEnabled();
    }
}
