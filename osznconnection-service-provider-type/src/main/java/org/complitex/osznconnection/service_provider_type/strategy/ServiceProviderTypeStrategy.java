/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.service_provider_type.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@Stateless
public class ServiceProviderTypeStrategy extends TemplateStrategy {

    /**
     * Attribute type ids
     */
    public static final long NAME = 1600;
    /**
     * Predefined service provider types ids:
     */
    public static final long APARTMENT_FEE = 1;
    // uncomment when these service provider types will enabled.
    /*
    public static final long HEATING = 2;
    public static final long HOT_WATER_SUPPLY = 3;
    public static final long COLD_WATER_SUPPLY = 4;
    public static final long GAS_SUPPLY = 5;
    public static final long POWER_SUPPLY = 6;
     */
    public static final long GARBAGE_DISPOSAL = 7;
    /*
    public static final long DRAINAGE = 8;
     */
    public static final Set<Long> RESERVED_SERVICE_PROVIDER_TYPES =
            Sets.newHashSet(APARTMENT_FEE, GARBAGE_DISPOSAL);
    // uncomment when these service provider types will enabled.
            /* HEATING, HOT_WATER_SUPPLY, COLD_WATER_SUPPLY, GAS_SUPPLY, POWER_SUPPLY, DRAINAGE);*/
    private static Map<Long, DomainObject> reservedServiceProviderTypeMap = new ConcurrentHashMap<Long, DomainObject>();

    @PostConstruct
    private void init() {
        if (reservedServiceProviderTypeMap.isEmpty()) {
            for (long serviceProviderTypeId : RESERVED_SERVICE_PROVIDER_TYPES) {
                DomainObject serviceProviderType = findById(serviceProviderTypeId, true);
                if (serviceProviderType != null) {
                    reservedServiceProviderTypeMap.put(serviceProviderTypeId, serviceProviderType);
                } else {
                    throw new IllegalStateException("Database does not contain reserved service provider. Service provider id: "
                            + serviceProviderTypeId);
                }
            }
        }
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PageParameters getListPageParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEntityTable() {
        return "service_provider_type";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return AttributeUtil.getStringCultureValue(object, NAME, locale);
    }

    @Transactional
    @Override
    public DomainObject findById(long id, boolean runAsAdmin) {
        if (runAsAdmin) {
            final DomainObject object = reservedServiceProviderTypeMap.get(id);
            if (object != null) {
                return object;
            }
        }
        return super.findById(id, runAsAdmin);
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

    public List<DomainObject> getAll(Locale locale) {
        List<DomainObject> all = Lists.newArrayList(reservedServiceProviderTypeMap.values());
        Collections.sort(all, new Comparator<DomainObject>() {

            @Override
            public int compare(DomainObject o1, DomainObject o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return all;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADMIN_MODULE_EDIT};
    }
}