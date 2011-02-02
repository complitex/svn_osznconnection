/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.privilege.web;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation("PRIVILEGE_MODULE_EDIT")
public class PrivilegeTemplateMenu extends ResourceTemplateMenu {

    private static IStrategy getStrategy() {
        return EjbBeanLocator.getBean(StrategyFactory.class).getStrategy("privilege");
    }

    @Override
    public String getTitle(Locale locale) {
        return getString(MenuResources.class, locale, "privilege_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getStrategy().getPluralEntityLabel(locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return getStrategy().getListPage();
            }

            @Override
            public PageParameters getParameters() {
                return getStrategy().getListPageParams();
            }

            @Override
            public String getTagId() {
                return "privilege_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "privilege_menu";
    }
}
