package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Date;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.edit.OsznOrganizationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.converter.BooleanConverter;
import org.complitex.dictionary.converter.IConverter;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;

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
        example.addAdditionalParam("organizationTypeIds", ImmutableList.of(OsznOrganizationTypeStrategy.OSZN,
                OsznOrganizationTypeStrategy.CALCULATION_CENTER));
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    @Transactional
    @Override
    public List<DomainObject> getAllOSZNs(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.addAdditionalParam("organizationTypeIds", ImmutableList.of(OsznOrganizationTypeStrategy.OSZN));
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
        example.addAdditionalParam("organizationTypeIds", ImmutableList.of(OsznOrganizationTypeStrategy.CALCULATION_CENTER));
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    private boolean isCalculationCenter(DomainObject organization) {
        List<Long> organizationTypeIds = getOrganizationTypeIds(organization);
        return organizationTypeIds != null && organizationTypeIds.contains(OsznOrganizationTypeStrategy.CALCULATION_CENTER);
    }

    private boolean isActiveCalculationCenter(DomainObject organization) {
        return isCalculationCenter(organization) && AttributeUtil.getBooleanValue(organization, CURRENT_CALCULATION_CENTER);
    }

    @Transactional
    private void setCurrentCalculationCenter(DomainObject organization, Date date) {
        if (isActiveCalculationCenter(organization)) {
            List<DomainObject> allCalculationCentres = getAllCalculationCentres(null);
            for (DomainObject calcCenter : allCalculationCentres) {
                if (isActiveCalculationCenter(calcCenter) && !calcCenter.getId().equals(organization.getId())) {
                    DomainObject oldCalcCenter = findById(calcCenter.getId(), true);
                    DomainObject newCalcCenter = CloneUtil.cloneObject(oldCalcCenter);
                    Attribute attribute = newCalcCenter.getAttribute(CURRENT_CALCULATION_CENTER);
                    IConverter<Boolean> converter = new BooleanConverter();
                    stringBean.getSystemStringCulture(attribute.getLocalizedValues()).setValue(converter.toString(Boolean.FALSE));
                    super.update(oldCalcCenter, newCalcCenter, date);
                }
            }
        }
    }

    @Transactional
    @Override
    public long getCurrentCalculationCenterId() {
        List<DomainObject> allCalculationCentres = getAllCalculationCentres(null);
        for (DomainObject calcCenter : allCalculationCentres) {
            if (isActiveCalculationCenter(calcCenter)) {
                return calcCenter.getId();
            }
        }
        throw new RuntimeException("Active calculation center has not been chosen.");
    }

    @Transactional
    @Override
    public void insert(DomainObject organization, Date insertDate) {
        super.insert(organization, insertDate);
        setCurrentCalculationCenter(organization, insertDate);
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.update(oldObject, newObject, updateDate);
        setCurrentCalculationCenter(newObject, updateDate);
    }

    @Transactional
    @Override
    public void updateAndPropagate(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.updateAndPropagate(oldObject, newObject, updateDate);
        setCurrentCalculationCenter(newObject, updateDate);
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        if (entityAttributeType.getId().equals(CURRENT_CALCULATION_CENTER)) {
            return false;
        }
        return super.isSimpleAttributeType(entityAttributeType);
    }

    @Override
    protected void loadStringCultures(List<Attribute> attributes) {
        super.loadStringCultures(attributes);

        for (Attribute attribute : attributes) {
            if (attribute.getAttributeTypeId().equals(CURRENT_CALCULATION_CENTER)) {
                if (attribute.getValueId() != null) {
                    loadStringCultures(attribute);
                } else {
                    attribute.setLocalizedValues(stringBean.newStringCultures());
                }
            }
        }
    }

    @Override
    protected void fillAttributes(DomainObject object) {
        super.fillAttributes(object);
        Attribute attribute = object.getAttribute(CURRENT_CALCULATION_CENTER);
        if (attribute != null && (attribute.getLocalizedValues() == null)) {
            List<StringCulture> localizedValues = stringBean.newStringCultures();
            stringBean.getSystemStringCulture(localizedValues).setValue(new BooleanConverter().toString(Boolean.FALSE));
            attribute.setLocalizedValues(localizedValues);
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
}
