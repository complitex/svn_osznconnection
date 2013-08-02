/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.privilege;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.template.web.pages.EntityDescription;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.List;
import java.util.Locale;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.PRIVILEGE_MODULE_EDIT)
public class PrivilegeDescriptionTemplateMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(MenuResources.class, locale, "description_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return EjbBeanLocator.getBean(StringCultureBean.class).displayValue(
                        EjbBeanLocator.getBean(StrategyFactory.class).getStrategy("privilege").getEntity().getEntityNames(), locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return EntityDescription.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters().set(EntityDescription.ENTITY, "privilege");
            }

            @Override
            public String getTagId() {
                return "privilege_description_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "privilege_description_menu";
    }
}
