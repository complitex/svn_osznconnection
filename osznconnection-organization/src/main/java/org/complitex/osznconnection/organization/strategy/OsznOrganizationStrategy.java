package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
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
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;

/**
 *
 * @author Artem
 */
@Stateless
public class OsznOrganizationStrategy extends OrganizationStrategy implements IOsznOrganizationStrategy {

    public static final String OSZN_ORGANIZATION_STRATEGY_NAME = OsznOrganizationStrategy.class.getSimpleName();
    private static final String RESOURCE_BUNDLE = OsznOrganizationStrategy.class.getName();
    @EJB
    private LocaleBean localeBean;

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
}
