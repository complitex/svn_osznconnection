/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web.edit;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Map;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;

import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.description.EntityType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.strategy.web.CanEditUtil;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Artem
 */
public class OrganizationEditComponent extends AbstractComplexAttributesPanel {

    private static final Logger log = LoggerFactory.getLogger(OrganizationEditComponent.class);
    
    @EJB(name = "DistrictStrategy")
    private DistrictStrategy districtStrategy;

    @EJB(name = "OrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    private Attribute districtAttribute;
    private Attribute parentAttribute;

    private class DistrictSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long districtId = ids.get("district");
            if (districtId != null && districtId > 0) {
                districtAttribute.setValueId(districtId);
            } else {
                districtAttribute.setValueId(null);
            }
        }
    }
    private SearchComponentState componentState;

    public OrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        //district container
        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setOutputMarkupPlaceholderTag(true);
        add(districtContainer);

        //district required container
        final WebMarkupContainer districtRequired = new WebMarkupContainer("districtRequired");
        districtContainer.add(districtRequired);

        //parent container
        final WebMarkupContainer parentContainer = new WebMarkupContainer("parentContainer");
        parentContainer.setOutputMarkupPlaceholderTag(true);
        add(parentContainer);

        final DomainObject currentOrganization = getInputPanel().getObject();
        final DropDownChoice<EntityType> selectType = getInputPanel().getSelectType();
        if (selectType != null) {
            if (currentOrganization.getId() == null) {
                //new organization
                selectType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update domain object

                        setDistrictVisibility(districtContainer, districtRequired, getInputPanel().getObject().getEntityTypeId());
                        setParentVisibility(parentContainer, getInputPanel().getObject().getEntityTypeId());
                        selectType.setEnabled(false);
                        target.addComponent(districtContainer);
                        target.addComponent(parentContainer);
                        target.addComponent(selectType);
                    }
                });
            } else {
                selectType.setEnabled(false);
            }
        }

        //district
        componentState = new SearchComponentState();
        districtAttribute = organizationStrategy.getDistrictAttribute(currentOrganization);
        if (districtAttribute != null) {
            Long districtId = districtAttribute.getValueId();

            if (districtId != null) {
                DomainObject district = null;
                DomainObjectExample example = new DomainObjectExample();
                example.setId(districtId);
                district = districtStrategy.find(example).get(0);

                Strategy.RestrictedObjectInfo info = districtStrategy.findParentInSearchComponent(districtId, null);
                if (info != null) {
                    componentState = districtStrategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put("district", district);
                }
            }
        }

        districtContainer.add(new SearchComponent("district", componentState, ImmutableList.of("city", "district"), new DistrictSearchCallback(),
                !isDisabled() && CanEditUtil.canEdit(currentOrganization)));
        setDistrictVisibility(districtContainer, districtRequired, currentOrganization.getEntityTypeId());
        //parent
        parentAttribute = organizationStrategy.getParentAttribute(currentOrganization);
        IModel<Long> parentModel = new Model<Long>();
        if (parentAttribute != null) {
            parentModel = new Model<Long>() {

                @Override
                public Long getObject() {
                    return parentAttribute.getValueId();
                }

                @Override
                public void setObject(Long object) {
                    parentAttribute.setValueId(object);
                }
            };
        }

        parentContainer.add(new UserOrganizationPicker("parent", parentModel));
        setParentVisibility(parentContainer, currentOrganization.getEntityTypeId());
    }

    private void setDistrictVisibility(WebMarkupContainer districtContainer, WebMarkupContainer districtRequiredContainer, Long entityTypeId) {
        if (districtAttribute == null) {
            districtContainer.setVisible(false);
            return;
        }

        if (entityTypeId != null && (entityTypeId.equals(IOsznOrganizationStrategy.OSZN)
                || (entityTypeId.equals(OrganizationStrategy.USER_ORGANIZATION)))) {
            districtContainer.setVisible(true);
            if(entityTypeId.equals(OrganizationStrategy.USER_ORGANIZATION)){
                districtRequiredContainer.setVisible(false);
            }
        } else {
            districtContainer.setVisible(false);
            districtAttribute.setValueId(null);
            componentState.clear();
        }
    }

    private void setParentVisibility(WebMarkupContainer parentContainer, Long entityTypeId) {
        if (parentAttribute == null) {
            parentContainer.setVisible(false);
            return;
        }

        if (entityTypeId != null && entityTypeId.equals(IOsznOrganizationStrategy.USER_ORGANIZATION)) {
            parentContainer.setVisible(true);
        } else {
            parentContainer.setVisible(false);
            parentAttribute.setValueId(null);
        }
    }

    public boolean isDistrictEntered() {
        DomainObject district = componentState.get("district");
        Long districtId = district != null ? district.getId() : null;
        return districtId != null && districtId > 0;
    }
}
