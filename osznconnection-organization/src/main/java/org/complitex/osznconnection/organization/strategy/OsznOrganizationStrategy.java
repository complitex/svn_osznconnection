package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Set;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.entity.DomainObject;
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
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.ResourceUtil;
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
        pageParameters.put(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters pageParameters = super.getHistoryPageParams(objectId);
        pageParameters.put(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters pageParameters = super.getListPageParams();
        pageParameters.put(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);
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
        if (entityAttributeType.getId().equals(DATA_SOURCE)) {
            return false;
        }
        return super.isSimpleAttributeType(entityAttributeType);
    }

    @Override
    protected void fillAttributes(DomainObject object) {
        super.fillAttributes(object);
        if (object.getAttribute(DATA_SOURCE).getLocalizedValues() == null) {
            object.getAttribute(DATA_SOURCE).setLocalizedValues(stringBean.newStringCultures());
        }
    }

    @Override
    protected void loadStringCultures(List<Attribute> attributes) {
        super.loadStringCultures(attributes);

        for (Attribute attribute : attributes) {
            if (attribute.getAttributeTypeId().equals(DATA_SOURCE)) {
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
    protected void deleteChecks(long objectId, Locale locale) throws DeleteException {
        if (ITSELF_ORGANIZATION_OBJECT_ID == objectId) {
            throw new DeleteException(ResourceUtil.getString(RESOURCE_BUNDLE, "delete_reserved_instance_error", locale));
        }
        super.deleteChecks(objectId, locale);
    }

    @Override
    public Set<Long> getServiceProviderTypeIds(long calculationCenterOrganizationId) {
        DomainObject calculationCenter = findById(calculationCenterOrganizationId, true);

        Set<Long> serviceProviderTypeIds = Sets.newHashSet();
        for (Attribute spta : calculationCenter.getAttributes(SERVICE_PROVIDER_TYPE)) {
            serviceProviderTypeIds.add(spta.getValueId());
        }
        return serviceProviderTypeIds;
    }

    @Override
    public long getCalculationCenterId(DomainObject userOrganization) {
        return userOrganization.getAttribute(CALCULATION_CENTER).getValueId();
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
                                if (fullDataSource.equalsIgnoreCase(currentDataSource)) {
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
        final String dataSource = AttributeUtil.getStringValue(calculationCenter, DATA_SOURCE);
        return dataSource != null ? dataSource.toLowerCase() : null;
    }
}
