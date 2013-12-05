package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.organization.strategy.AbstractOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.*;
import javax.sql.DataSource;
import java.util.*;

import static org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy.*;

/**
 *
 * @author Artem
 */
@Stateless(name = IOrganizationStrategy.BEAN_NAME)
public class OsznOrganizationStrategy extends AbstractOrganizationStrategy<DomainObject> {
    public final static Long MODULE_ID = 0L;

    /**
     * Reference to jdbc data source. It is calculation center only attribute.
     */
    public final static long DATA_SOURCE = 913;

    /**
     * References to associations between service provider types and calculation centres. It is user organization only attribute.
     */
    public final static long SERVICE_ASSOCIATIONS = 914;

    /**
     * Load payments/benefits directory. It is OSZN only attribute.
     */
    public final static long LOAD_PAYMENT_BENEFIT_FILES_DIR = 915;

    /**
     * Save payments/benefits directory. It is OSZN only attribute.
     */
    public final static long SAVE_PAYMENT_BENEFIT_FILES_DIR = 916;

    /**
     * Load actual payments directory. It is OSZN only attribute.
     */
    public final static long LOAD_ACTUAL_PAYMENT_DIR = 917;

    /**
     * Save actual payments directory. It is OSZN only attribute.
     */
    public final static long SAVE_ACTUAL_PAYMENT_DIR = 918;

    /**
     * Load subsidies directory. It is OSZN only attribute.
     */
    public final static long LOAD_SUBSIDY_DIR = 919;

    /**
     * Save subsidies directory. It is OSZN only attribute.
     */
    public final static long SAVE_SUBSIDY_DIR = 920;

    /**
     * Load dwelling characteristics directory. It is OSZN only attribute.
     */
    public final static long LOAD_DWELLING_CHARACTERISTICS_DIR = 921;

    /**
     * Save dwelling characteristics directory. It is OSZN only attribute.
     */
    public final static long SAVE_DWELLING_CHARACTERISTICS_DIR = 922;

    /**
     * Load facility service type directory. It is OSZN only attribute.
     */
    public final static long LOAD_FACILITY_SERVICE_TYPE_DIR = 923;

    /**
     * Save facility service type directory. It is OSZN only attribute.
     */
    public final static long SAVE_FACILITY_SERVICE_TYPE_DIR = 924;

    /**
     * References directory. It is OSZN only attribute.
     */
    public final static long REFERENCES_DIR = 925;

    /**
     * EDRPOU(ЕДРПОУ). It is user organization only attribute.
     */
    public final static long EDRPOU = 926;

    /**
     * Root directory for loading and saving request files. It is user organization only attribute.
     */
    public final static long ROOT_REQUEST_FILE_DIRECTORY = 927;

    /**
     * Save facility form2 directory. It is OSZN only attribute.
     */
    public final static long SAVE_FACILITY_FORM2_DIR = 928;


    /**
     * Itself organization instance id.
     */


    private final Logger log = LoggerFactory.getLogger(OsznOrganizationStrategy.class);
    public static final String OSZN_ORGANIZATION_STRATEGY_NAME = IOrganizationStrategy.BEAN_NAME;
    private static final String RESOURCE_BUNDLE = OsznOrganizationStrategy.class.getName();
    private static final String MAPPING_NAMESPACE = OsznOrganizationStrategy.class.getPackage().getName() + ".OsznOrganization";

    public static final List<Long> LOAD_SAVE_FILE_DIR_ATTRIBUTES =
            ImmutableList.of(LOAD_PAYMENT_BENEFIT_FILES_DIR, SAVE_PAYMENT_BENEFIT_FILES_DIR,
            LOAD_ACTUAL_PAYMENT_DIR, SAVE_ACTUAL_PAYMENT_DIR, LOAD_SUBSIDY_DIR, SAVE_SUBSIDY_DIR, 
            LOAD_DWELLING_CHARACTERISTICS_DIR, SAVE_DWELLING_CHARACTERISTICS_DIR, REFERENCES_DIR,
            LOAD_FACILITY_SERVICE_TYPE_DIR, SAVE_FACILITY_SERVICE_TYPE_DIR, SAVE_FACILITY_FORM2_DIR);

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

    @Override
    public Long getModuleId() {
        return MODULE_ID;
    }

    @Transactional
    @Override
    public List<DomainObject> getAllOuterOrganizations(Locale locale) {
        return getOrganizations(Arrays.asList(OSZN, CALCULATION_CENTER, SERVICING_ORGANIZATION), locale);
    }

    @Transactional
    public List<DomainObject> getAllOSZNs(Locale locale) {
        return getOrganizations(Arrays.asList(OSZN), locale);
    }

    @Transactional
      public List<DomainObject> getAllCalculationCentres(Locale locale) {
        return getOrganizations(Arrays.asList(CALCULATION_CENTER), locale);
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        return !CUSTOM_ATTRIBUTE_TYPES.contains(entityAttributeType.getId())
                && super.isSimpleAttributeType(entityAttributeType);
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
        if (MODULE_ID == objectId) {
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

    /**
     * Figures out list of service associations. Each service association is link between service provider type and
     * caluclation center.
     *
     * @param userOrganization User organization.
     * @return Service associations list.
     */
    public ServiceAssociationList getServiceAssociations(DomainObject userOrganization) {
        return loadServiceAssociations(userOrganization);
    }

    /**
     * Finds remote jdbc data sources.
     * @param currentDataSource Current data source.
     * @return Remote jdbc data sources.
     */
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

    /**
     * Figures out data source of calculation center.
     *
     * @param calculationCenterId Calculation center's id
     * @return Calculation center's data source
     */
    public String getDataSource(long calculationCenterId) {
        DomainObject calculationCenter = findById(calculationCenterId, true);
        return AttributeUtil.getStringValue(calculationCenter, DATA_SOURCE);
    }

    /**
     * Returns relative path to request files storage.
     * @param osznId Oszn's id.
     * @param fileTypeAttributeTypeId Attribute type id corresponding desired file type.
     * @return Relative path to request files storage.
     */
    public String getRelativeRequestFilesPath(long osznId, long fileTypeAttributeTypeId) {
        DomainObject oszn = findById(osznId, true);
        return AttributeUtil.getStringValue(oszn, fileTypeAttributeTypeId);
    }

    /**
     * Returns root directory to request files storage.
     * @param userOrganizationId User organization's id.
     * @return Root directory to request files storage.
     */
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
