/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.web.edit.OrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.template.strategy.AbstractStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.util.Numbers;
import org.complitex.osznconnection.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.template.web.security.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class OrganizationStrategy extends AbstractStrategy implements IOsznOrganizationStrategy {

    private static final Logger log = LoggerFactory.getLogger(OrganizationStrategy.class);
    private static final String ORGANIZATION_NAMESPACE = OrganizationStrategy.class.getPackage().getName() + ".Organization";
    private static final String RESOURCE_BUNDLE = OrganizationStrategy.class.getName();
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private DistrictStrategy districtStrategy;
    @EJB
    private LocaleBean localeBean;

    @Override
    public String getEntityTable() {
        return "organization";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }

    @Transactional
    @Override
    public void insert(DomainObject object) {
        super.insert(object);

        Attribute districtAttribute = getDistrictAttribute(object);
        Long districtId = districtAttribute.getValueId();
        if (districtId != null) {
            DomainObject districtObject = districtStrategy.findById(districtId, false);
            if (districtObject != null) {
                Set<Long> addSubjectIds = Sets.newHashSet(object.getId());
                districtStrategy.changeChildrenPermissionsInDistinctThread(districtId, districtObject.getPermissionId(), addSubjectIds, null);
            }
        }
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.update(oldObject, newObject, updateDate);
        changeSubjectsAcrossDistrictTree(oldObject, newObject);
    }

    private void changeSubjectsAcrossDistrictTree(DomainObject oldObject, DomainObject newObject) {
        long organizationId = newObject.getId();
        Set<Long> subjectIds = Sets.newHashSet(organizationId);
        Attribute oldDistrictAttribute = getDistrictAttribute(oldObject);
        Attribute newDistrictAttribute = getDistrictAttribute(newObject);
        Long oldDistrictId = oldDistrictAttribute.getValueId();
        Long newDistrictId = newDistrictAttribute.getValueId();
        if (!Numbers.isEqual(oldDistrictId, newDistrictId)) {
            //district reference has changed
            if (oldDistrictId != null) {
                long oldDistrictPermissionId = districtStrategy.findById(oldDistrictId, false).getPermissionId();
                districtStrategy.changeChildrenPermissionsInDistinctThread(oldDistrictId, oldDistrictPermissionId, null, subjectIds);
            }

            if (newDistrictId != null) {
                long newDistrictPermissionId = districtStrategy.findById(newDistrictId, false).getPermissionId();
                districtStrategy.changeChildrenPermissionsInDistinctThread(newDistrictId, newDistrictPermissionId, subjectIds, null);
            }
        }
    }

    @Transactional
    @Override
    public void updateAndPropagate(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.updateAndPropagate(oldObject, newObject, updateDate);
        changeSubjectsAcrossDistrictTree(oldObject, newObject);
    }

    @Override
    public IValidator getValidator() {
        return new OrganizationValidator(localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return OrganizationEditComponent.class;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<? extends DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(ORGANIZATION_NAMESPACE + "." + FIND_OPERATION, example);

        for (DomainObject object : objects) {
            loadAttributes(object);
        }
        return objects;
    }

    @Transactional
    @Override
    public int count(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);
        return (Integer) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<DomainObject> getAllOuterOrganizations(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setOrderByAttributeTypeId(NAME);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
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
        example.setOrderByAttributeTypeId(NAME);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
        configureExample(example, ImmutableMap.<String, Long>of(), null);

        return (List<DomainObject>) find(example);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<DomainObject> getAllCalculationCentres(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(CALCULATION_CENTER);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
        example.setOrderByAttributeTypeId(NAME);
        configureExample(example, ImmutableMap.<String, Long>of(), null);

        return (List<DomainObject>) find(example);
    }

    @Override
    public Attribute getDistrictAttribute(DomainObject organization) {
        return organization.getAttribute(DISTRICT);
    }

    @Override
    public Attribute getParentAttribute(DomainObject organization) {
        return organization.getAttribute(USER_ORGANIZATION_PARENT);
    }

    @Override
    public String getDistrictCode(DomainObject organization) {
        String districtCode = null;
        Attribute districtAttribute = getDistrictAttribute(organization);
        if (districtAttribute != null) {
            districtCode = districtStrategy.getDistrictCode(districtAttribute.getValueId());
        }
        return districtCode;
    }

    @Transactional
    @Override
    public DomainObject getItselfOrganization() {
        DomainObjectExample example = new DomainObjectExample(ITSELF_ORGANIZATION_OBJECT_ID);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return find(example).get(0);
    }

    @Override
    public String getCode(DomainObject organization) {
        return stringBean.getSystemStringCulture(organization.getAttribute(CODE).getLocalizedValues()).getValue();
    }

    @Override
    public String getName(DomainObject organization, Locale locale) {
        return stringBean.displayValue(organization.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Long validateCode(Long id, String code, Long parentId, Long parentEntityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", code);
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);

        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateCode", params);

        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }

        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Long validateName(Long id, String name, Long parentId, Long parentEntityId, Locale locale) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("name", name);
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);
        params.put("localeId", localeBean.convert(locale).getId());

        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateName", params);

        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }

        return null;
    }

    @Transactional
    @Override
    public List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(USER_ORGANIZATION);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
        example.setOrderByAttributeTypeId(NAME);
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
        Set<Long> childrenIds = Sets.newHashSet(sqlSession().selectList(ORGANIZATION_NAMESPACE + ".selectOrganizationChildrenObjectIds", parentOrganizationId));
        Set<Long> treeChildren = Sets.newHashSet(childrenIds);

        for (Long childId : childrenIds) {
            treeChildren.addAll(getTreeChildrenOrganizationIds(childId));
        }

        return treeChildren;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ORGANIZATION_MODULE_EDIT};
    }

    @Transactional
    @Override
    public void changeChildrenActivity(long parentId, boolean enable) {
        Set<Long> childrenIds = getTreeChildrenOrganizationIds(parentId);
        if (!childrenIds.isEmpty()) {
            updateChildrenActivity(childrenIds, !enable);
        }
    }

    @Transactional
    protected void updateChildrenActivity(Set<Long> childrenIds, boolean enabled) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("childrenIds", childrenIds);
        params.put("enabled", enabled);
        params.put("status", enabled ? StatusType.INACTIVE : StatusType.ACTIVE);
        sqlSession().update(ORGANIZATION_NAMESPACE + "." + UPDATE_CHILDREN_ACTIVITY_OPERATION, params);
    }
}
