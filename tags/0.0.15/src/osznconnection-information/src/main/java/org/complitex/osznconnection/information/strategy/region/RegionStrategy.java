package org.complitex.osznconnection.information.strategy.region;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.web.DomainObjectListPanel;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.osznconnection.information.resource.CommonResources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;

/**
 *
 * @author Artem
 */
@Stateless
public class RegionStrategy extends AbstractStrategy {

    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    /*
     * Attribute type ids
     */
    private static final long NAME = 700L;

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String getEntityTable() {
        return "region";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
        Long countryId = ids.get("country");
        example.setParentId(countryId);
        example.setParentEntity("country");
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country");
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
            configureExampleImpl(list.getExample(), ids, null);
            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long countryId = ids.get("country");
            if (countryId != null && countryId > 0) {
                inputPanel.getObject().setParentId(countryId);
                inputPanel.getObject().setParentEntityId(800L);
            } else {
                inputPanel.getObject().setParentId(null);
                inputPanel.getObject().setParentEntityId(null);
            }
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"city"};
    }

    @Override
    public String[] getParents() {
        return new String[]{"country"};
    }
}