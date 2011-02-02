/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.ownership.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.template.web.pages.EntityDescription;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation("OWNERSHIP_MODULE_EDIT")
public class OwnershipDescriptionTemplateMenu extends ResourceTemplateMenu {

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
                        EjbBeanLocator.getBean(StrategyFactory.class).getStrategy("ownership").getEntity().getEntityNames(), locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return EntityDescription.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters(ImmutableMap.of(EntityDescription.ENTITY, "ownership"));
            }

            @Override
            public String getTagId() {
                return "ownership_description_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "ownership_description_menu";
    }
}
