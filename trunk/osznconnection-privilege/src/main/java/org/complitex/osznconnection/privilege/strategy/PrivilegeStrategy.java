/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.privilege.strategy;

import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.template.strategy.AbstractStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Artem
 */
@Stateless(name = "PrivilegeStrategy")
public class PrivilegeStrategy extends AbstractStrategy {

    private static final String RESOURCE_BUNDLE = PrivilegeStrategy.class.getName();
    /**
     * Attribute type ids
     */
    public static final long NAME = 1200;
    public static final long CODE = 1201;

    @EJB
    private StringCultureBean stringBean;

    @Override
    public String getEntityTable() {
        return "privilege";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME, CODE);
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
    public int getSearchTextFieldSize() {
        return 40;
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.PRIVILEGE_MODULE_EDIT};
    }
}
