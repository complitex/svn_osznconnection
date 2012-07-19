package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class OsznOrganizationStrategy extends OrganizationStrategy implements IOsznOrganizationStrategy {

    private static final Logger log = LoggerFactory.getLogger(OsznOrganizationStrategy.class);
    public static final String OSZN_ORGANIZATION_STRATEGY_NAME = OsznOrganizationStrategy.class.getSimpleName();
    private static final String RESOURCE_BUNDLE = OsznOrganizationStrategy.class.getName();
    private static final String MAPPING_NAMESPACE = OsznOrganizationStrategy.class.getPackage().getName() + ".OsznOrganization";
    public static final List<Long> LOAD_SAVE_FILE_DIR_ATTRIBUTES =
            ImmutableList.of(LOAD_PAYMENT_BENEFIT_FILES_DIR, SAVE_PAYMENT_BENEFIT_FILES_DIR,
            LOAD_ACTUAL_PAYMENT_DIR, SAVE_ACTUAL_PAYMENT_DIR, LOAD_SUBSIDY_DIR, SAVE_SUBSIDY_DIR, REFERENCES_DIR);
    private static final List<Long> CUSTOM_ATTRIBUTE_TYPES = ImmutableList.<Long>builder().
            add(DATA_SOURCE).
            addAll(LOAD_SAVE_FILE_DIR_ATTRIBUTES).
            add(EDRPOU).
            add(ROOT_REQUEST_FILE_DIRECTORY).
            build();
    private static final List<Long> ATTRIBUTE_TYPES_WITH_CUSTOM_STRING_PROCESSING =
            ImmutableList.<Long>builder().
            add(DATA_SOURCE).
            addAll(LOAD_SAVE_FILE_DIR_ATTRIBUTES).
            add(ROOT_REQUEST_FILE_DIRECTORY).
            build();
    @EJB
    private LocaleBean localeBean;
    @EJB
    private StringCultureBean stringBean;

    @Override
    public IValidator getValidator() {
        return new OsznOrganizationValidator(localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass() {
        return OsznOrganizationEditComponent.class;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters pageParameters = super.getEditPageParams(objectId, parentId, parentEntity);
        pageParameters.set(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters pageParameters = super.getHistoryPageParams(objectId);
        pageParameters.set(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters pageParameters = super.getListPageParams();
        pageParameters.set(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
        return pageParameters;
    }

    @Transactional
    @Override
    public DomainObject getItselfOrganization() {
        return findById(ITSELF_ORGANIZATION_OBJECT_ID, true);
    }

    @Transactional
    @Override
    public List<DomainObject> getAllOuterOrganizations(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, ImmutableList.of(OsznOrganizationTypeStrategy.OSZN,
                OsznOrganizationTypeStrategy.CALCULATION_CENTER));
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @Transactional
    @Override
    public List<DomainObject> getAllOSZNs(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, ImmutableList.of(OsznOrganizationTypeStrategy.OSZN));
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @Transactional
    @Override
    public List<DomainObject> getAllCalculationCentres(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, ImmutableList.of(OsznOrganizationTypeStrategy.CALCULATION_CENTER));
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        if (CUSTOM_ATTRIBUTE_TYPES.contains(entityAttributeType.getId())) {
            return false;
        }
        return super.isSimpleAttributeType(entityAttributeType);
    }

    @Override
    protected void fillAttributes(DomainObject object) {
        super.fillAttributes(object);

        for (long attributeTypeId : CUSTOM_ATTRIBUTE_TYPES) {
            if (object.getAttribute(attributeTypeId).getLocalizedValues() == null) {
                object.getAttribute(attributeTypeId).setLocalizedValues(stringBean.newStringCultures());
            }
        }
    }

    @Override
    protected void loadStringCultures(List<Attribute> attributes) {
        super.loadStringCultures(attributes);

        for (Attribute attribute : attributes) {
            if (CUSTOM_ATTRIBUTE_TYPES.contains(attribute.getAttributeTypeId())) {
                if (attribute.getValueId() != null) {
                    loadStringCultures(attribute);
                } else {
                    attribute.setLocalizedValues(stringBean.newStringCultures());
                }
            }
        }
    }

    @Transactional
    @Override
    public OsznOrganization findById(long id, boolean runAsAdmin) {
        DomainObject object = super.findById(id, runAsAdmin);
        if (object == null) {
            return null;
        }
        ServiceAssociationList serviceAssociationList = new ServiceAssociationList();
        if (isUserOrganization(object)) {
            serviceAssociationList = loadServiceAssociations(object);
        }
        return new OsznOrganization(object, serviceAssociationList);
    }

    @Override
    public OsznOrganization newInstance() {
        return new OsznOrganization(super.newInstance(), new ServiceAssociationList());
    }

    @Override
    public OsznOrganization findHistoryObject(long objectId, Date date) {
        DomainObject object = super.findHistoryObject(objectId, date);
        if (object == null) {
            return null;
        }
        ServiceAssociationList serviceAssociationList = new ServiceAssociationList();
        if (isUserOrganization(object)) {
            serviceAssociationList = loadServiceAssociations(object);
        }
        return new OsznOrganization(object, serviceAssociationList);
    }

    @Transactional
    @Override
    public void insert(DomainObject object, Date insertDate) {
        OsznOrganization osznOrganization = (OsznOrganization) object;
        if (!osznOrganization.getServiceAssociationList().isEmpty()
                && !osznOrganization.getServiceAssociationList().hasNulls()) {
            addServiceAssociationAttributes(osznOrganization);
        }

        super.insert(object, insertDate);
    }

    @Transactional
    private void addServiceAssociationAttributes(OsznOrganization osznOrganization) {
        osznOrganization.removeAttribute(SERVICE_ASSOCIATIONS);

        long i = 1;
        for (ServiceAssociation serviceAssociation : osznOrganization.getServiceAssociationList()) {
            saveServiceAssociation(serviceAssociation);

            Attribute a = new Attribute();
            a.setAttributeTypeId(SERVICE_ASSOCIATIONS);
            a.setValueId(serviceAssociation.getId());
            a.setValueTypeId(SERVICE_ASSOCIATIONS);
            a.setAttributeId(i++);
            osznOrganization.addAttribute(a);
        }
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        OsznOrganization newOrganization = (OsznOrganization) newObject;
        OsznOrganization oldOrganization = (OsznOrganization) oldObject;

        if (!newOrganization.getServiceAssociationList().isEmpty()
                && !newOrganization.getServiceAssociationList().hasNulls()) {
            if (!newOrganization.getServiceAssociationList().equals(oldOrganization.getServiceAssociationList())) {
                addServiceAssociationAttributes(newOrganization);
            }
        } else {
            newOrganization.removeAttribute(SERVICE_ASSOCIATIONS);
        }

        super.update(oldObject, newObject, updateDate);
    }

    @Transactional
    private void saveServiceAssociation(ServiceAssociation serviceAssociation) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertServiceAssociation", serviceAssociation);
    }

    @Transactional
    @Override
    protected void deleteChecks(long objectId, Locale locale) throws DeleteException {
        if (ITSELF_ORGANIZATION_OBJECT_ID == objectId) {
            throw new DeleteException(ResourceUtil.getString(RESOURCE_BUNDLE, "delete_reserved_instance_error", locale));
        }
        super.deleteChecks(objectId, locale);
    }

    @Transactional
    @Override
    public void delete(long objectId, Locale locale) throws DeleteException {
        deleteChecks(objectId, locale);

        sqlSession().delete(MAPPING_NAMESPACE + ".deleteServiceAssociations",
                ImmutableMap.of("objectId", objectId, "organizationServiceAssociationsAT", SERVICE_ASSOCIATIONS));

        deleteStrings(objectId);
        deleteAttribute(objectId);
        deleteObject(objectId, locale);
    }

    @Override
    public ServiceAssociationList getServiceAssociations(DomainObject userOrganization) {
        return loadServiceAssociations(userOrganization);
    }

    @Override
    public List<RemoteDataSource> findRemoteDataSources(String currentDataSource) {
        final String JDBC_PREFIX = "jdbc";
        final String GLASSFISH_INTERNAL_SUFFIX = "__pm";
        final Set<String> PREDEFINED_DATA_SOURCES = ImmutableSet.of("sample", "__TimerPool", "__default");

        Set<RemoteDataSource> remoteDataSources = Sets.newTreeSet(new Comparator<RemoteDataSource>() {

            @Override
            public int compare(RemoteDataSource o1, RemoteDataSource o2) {
                return o1.getDataSource().compareTo(o2.getDataSource());
            }
        });

        boolean currentDataSourceEnabled = false;

        try {
            Context context = new InitialContext();
            final NamingEnumeration<NameClassPair> resources = context.list(JDBC_PREFIX);
            if (resources != null) {
                while (resources.hasMore()) {
                    final NameClassPair nc = resources.next();
                    if (nc != null) {
                        final String name = nc.getName();
                        if (!Strings.isEmpty(name) && !name.endsWith(GLASSFISH_INTERNAL_SUFFIX)
                                && !PREDEFINED_DATA_SOURCES.contains(name)) {
                            final String fullDataSource = JDBC_PREFIX + "/" + name;
                            Object jndiObject = null;
                            try {
                                jndiObject = context.lookup(fullDataSource);
                            } catch (NamingException e) {
                            }

                            if (jndiObject instanceof DataSource) {
                                boolean current = false;
                                if (fullDataSource.equals(currentDataSource)) {
                                    currentDataSourceEnabled = true;
                                    current = true;
                                }
                                remoteDataSources.add(new RemoteDataSource(fullDataSource, current));
                            }
                        }

                    }
                }
            }
        } catch (NamingException e) {
            log.error("", e);
        }

        if (!currentDataSourceEnabled && !Strings.isEmpty(currentDataSource)) {
            remoteDataSources.add(new RemoteDataSource(currentDataSource, true, false));
        }

        return Lists.newArrayList(remoteDataSources);
    }

    @Override
    public String getDataSource(long calculationCenterId) {
        DomainObject calculationCenter = findById(calculationCenterId, true);
        return AttributeUtil.getStringValue(calculationCenter, DATA_SOURCE);
    }

    @Override
    public String getRelativeRequestFilesPath(long osznId, long fileTypeAttributeTypeId) {
        DomainObject oszn = findById(osznId, true);
        return AttributeUtil.getStringValue(oszn, fileTypeAttributeTypeId);
    }

    @Override
    public String getRootRequestFilesStoragePath(long userOrganizationId) {
        DomainObject userOrganization = findById(userOrganizationId, true);
        return AttributeUtil.getStringValue(userOrganization, ROOT_REQUEST_FILE_DIRECTORY);
    }

    @Transactional
    @Override
    protected Long insertStrings(long attributeTypeId, List<StringCulture> strings) {
        /* if it's data source or one of load/save request file directory attributes 
         * or root directory for loading and saving request files
         * then string value should be inserted as is and not upper cased. */
        return ATTRIBUTE_TYPES_WITH_CUSTOM_STRING_PROCESSING.contains(attributeTypeId)
                ? stringBean.insertStrings(strings, getEntityTable(), false)
                : super.insertStrings(attributeTypeId, strings);
    }

    private ServiceAssociationList loadServiceAssociations(DomainObject userOrganization) {
        if (!isUserOrganization(userOrganization)) {
            throw new IllegalArgumentException("DomainObject is not user organization. Organization id: " + userOrganization.getId());
        }

        List<Attribute> serviceAssociationAttributes = userOrganization.getAttributes(SERVICE_ASSOCIATIONS);
        Set<Long> serviceAssociationIds = Sets.newHashSet();
        for (Attribute serviceAssociation : serviceAssociationAttributes) {
            serviceAssociationIds.add(serviceAssociation.getValueId());
        }

        final List<ServiceAssociation> serviceAssociations = sqlSession().selectList(
                MAPPING_NAMESPACE + ".getServiceAssociations", ImmutableMap.of("ids", serviceAssociationIds));

        Collections.sort(serviceAssociations, new Comparator<ServiceAssociation>() {

            @Override
            public int compare(ServiceAssociation o1, ServiceAssociation o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        return new ServiceAssociationList(serviceAssociations);
    }
}
