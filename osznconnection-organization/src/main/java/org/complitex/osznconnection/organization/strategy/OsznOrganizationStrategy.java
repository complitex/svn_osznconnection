package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.wicket.PageParameters;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.edit.OrganizationValidator;
import org.complitex.template.strategy.TemplateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author Artem
 */
@Stateless
public class OsznOrganizationStrategy extends OrganizationStrategy implements IOsznOrganizationStrategy {
    private static final  Logger log = LoggerFactory.getLogger(OsznOrganizationStrategy.class);

    private static final String OSZN_ORGANIZATION_STRATEGY_NAME = OsznOrganizationStrategy.class.getSimpleName();

    private static final String ORGANIZATION_NAMESPACE = OsznOrganizationStrategy.class.getPackage().getName() + ".Organization";

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private LocaleBean localeBean;

    @Override
    public IValidator getValidator() {
        return new OrganizationValidator(localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return OsznOrganizationEditComponent.class;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters pageParameters = super.getEditPageParams(objectId, parentId, parentEntity);

        pageParameters.put(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);

        return pageParameters;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters pageParameters =  super.getListPageParams();

        pageParameters.put(STRATEGY, OSZN_ORGANIZATION_STRATEGY_NAME);

        return pageParameters;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<DomainObject> getAllOuterOrganizations(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        example.addAdditionalParam("entityTypeIds", ImmutableList.of(OSZN, CALCULATION_CENTER));
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<DomainObject> getAllOSZNs(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(OSZN);
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<DomainObject> getAllCalculationCentres(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(CALCULATION_CENTER);
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
    public List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(USER_ORGANIZATION);
        if(locale != null){
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        example.setAdmin(true);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        List<? extends DomainObject> userOrganizations = find(example);
        if (excludeOrganizationsId == null) {
            return userOrganizations;
        }

        List<DomainObject> finalUserOrganizations = Lists.newArrayList();
        Set<Long> excludeSet = Sets.newHashSet(excludeOrganizationsId);
        for (DomainObject userOrganization : userOrganizations) {
            if (!excludeSet.contains(userOrganization.getId())) {
                finalUserOrganizations.add(userOrganization);
            }
        }
        return finalUserOrganizations;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId) {
        Set<Long> childrenIds = Sets.newHashSet(sqlSession().selectList(ORGANIZATION_NAMESPACE + ".findOrganizationChildrenObjectIds",
                parentOrganizationId));
        Set<Long> treeChildren = Sets.newHashSet(childrenIds);

        for (Long childId : childrenIds) {
            treeChildren.addAll(getTreeChildrenOrganizationIds(childId));
        }

        return treeChildren;
    }
}
