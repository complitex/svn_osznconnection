package org.complitex.osznconnection.information.web;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.osznconnection.commons.web.pages.EntityDescription;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;
import org.complitex.osznconnection.information.BookEntities;
import org.complitex.osznconnection.information.resource.CommonResources;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
public class InformationDescriptionTemplateMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(CommonResources.class, locale, "description_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = Lists.newArrayList();
        for (final String bookEntity : BookEntities.getEntityDescriptions()) {
            links.add(new ITemplateLink() {

                @Override
                public String getLabel(Locale locale) {
                    return EjbBeanLocator.getBean(StringCultureBean.class).displayValue(EjbBeanLocator.getBean(StrategyFactory.class).
                            getStrategy(bookEntity).getEntity().getEntityNames(), locale);
                }

                @Override
                public Class<? extends Page> getPage() {
                    return EntityDescription.class;
                }

                @Override
                public PageParameters getParameters() {
                    return new PageParameters(ImmutableMap.of(EntityDescription.ENTITY, bookEntity));
                }

                @Override
                public String getTagId() {
                    return bookEntity + "_description_item";
                }
            });
        }
        return links;
    }

    @Override
    public String getTagId() {
        return "description_menu";
    }
}
