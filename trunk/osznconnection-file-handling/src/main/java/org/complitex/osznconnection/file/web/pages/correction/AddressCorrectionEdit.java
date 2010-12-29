/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.StreetCorrection;
import org.complitex.osznconnection.file.web.component.correction.edit.AddressCorrectionInputPanel;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 * Страница для редактирования коррекций адресов.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTED_ENTITY = "entity";
    public static final String CORRECTION_ID = "correction_id";
    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private class Callback implements ISearchCallback, Serializable {

        private Correction correction;
        private String entity;

        public Callback(Correction correction, String entity) {
            this.correction = correction;
            this.entity = entity;
        }

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(entity);
            if (id != null && id > 0) {
                correction.setObjectId(id);
            }
        }
    }

    /**
     * Стандартная панель редактирования коррекции элемента адреса.
     */
    private abstract class AddressCorrectionEditPanel extends AbstractCorrectionEditPanel {

        public AddressCorrectionEditPanel(String id, String entity, Long correctionId) {
            super(id, entity, correctionId);
        }

        @Override
        protected IModel<String> internalObjectLabel(Locale locale) {
            return new ResourceModel("address");
        }

        @Override
        protected Panel internalObjectPanel(String id) {
            Correction correction = getModel();
            String entity = correction.getEntity();
            SearchComponentState componentState = new SearchComponentState();
            if (!isNew()) {
                long objectId = correction.getObjectId();
                Strategy.RestrictedObjectInfo info = getStrategy(entity).findParentInSearchComponent(objectId, null);
                if (info != null) {
                    componentState = getStrategy(entity).getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put(entity, findObject(objectId, entity));
                }
            }
            return new SearchComponent(id, componentState, getSearchFilters(), new Callback(correction, entity), true);
        }

        @Override
        protected String getNullObjectErrorMessage() {
            return getString("address_required");
        }

        protected Strategy getStrategy(String entity) {
            return strategyFactory.getStrategy(entity);
        }

        protected DomainObject findObject(long objectId, String entity) {
            DomainObjectExample example = new DomainObjectExample();
            example.setId(objectId);
            return getStrategy(entity).find(example).get(0);
        }

        protected abstract List<String> getSearchFilters();

        @Override
        protected Class<? extends Page> getBackPageClass() {
            return AddressCorrectionList.class;
        }

        @Override
        protected PageParameters getBackPageParameters() {
            PageParameters parameters = new PageParameters();
            parameters.put(AddressCorrectionList.CORRECTED_ENTITY, getModel().getEntity());
            return parameters;
        }
    }

    /**
     * Панель редактирования коррекции населенного пункта.
     */
    private class CityCorrectionEditPanel extends AddressCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public CityCorrectionEditPanel(String id, Long correctionId) {
            super(id, "city", correctionId);
        }

        @Override
        protected Correction initObjectCorrection(String entity, Long correctionId) {
            return addressCorrectionBean.findCityCorrectionById(correctionId);
        }

        @Override
        protected List<String> getSearchFilters() {
            return ImmutableList.of("city");
        }
    }

    /**
     * Панель редактирования коррекции района.
     */
    private class DistrictCorrectionEditPanel extends AddressCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public DistrictCorrectionEditPanel(String id, Long correctionId) {
            super(id, "district", correctionId);
        }

        @Override
        protected Correction initObjectCorrection(String entity, Long correctionId) {
            return addressCorrectionBean.findDistrictCorrectionById(correctionId);
        }

        @Override
        protected List<String> getSearchFilters() {
            return ImmutableList.of("city", "district");
        }

        @Override
        protected boolean freezeOrganization() {
            return true;
        }

        @Override
        protected Class<? extends Page> getBackPageClass() {
            return DistrictCorrectionList.class;
        }

        @Override
        protected boolean validate() {
            boolean valid = true;
            if (getModel().getParentId() == null) {
                error(getString("correction_required"));
                valid = false;
            }
            return valid && super.validate();
        }

        @Override
        protected String displayCorrection() {
            Correction correction = getModel();
            String city = null;
            if (correction.getParent() != null) {
                city = correction.getParent().getCorrection();
            }
            return AddressRenderer.displayAddress(null, city, correction.getCorrection(), getLocale());
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            return new AddressCorrectionInputPanel(id, getModel());
        }
    }

    /**
     * Панель редактирования коррекции улицы.
     */
    private class StreetCorrectionEditPanel extends AddressCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public StreetCorrectionEditPanel(String id, Long correctionId) {
            super(id, "street", correctionId);
        }

        @Override
        protected StreetCorrection getModel() {
            return (StreetCorrection) super.getModel();
        }

        @Override
        protected StreetCorrection newObjectCorrection(String entity) {
            return new StreetCorrection();
        }

        @Override
        protected StreetCorrection initObjectCorrection(String entity, Long correctionId) {
            return addressCorrectionBean.findStreetCorrectionById(correctionId);
        }

        @Override
        protected String displayCorrection() {
            StreetCorrection correction = getModel();

            String city = null;
            if (correction.getParent() != null) {
                city = correction.getParent().getCorrection();
            }
            String streetType = null;
            if (correction.getStreetTypeCorrection() != null) {
                streetType = correction.getStreetTypeCorrection().getCorrection();
            }
            if (Strings.isEmpty(streetType)) {
                streetType = null;
            }
            return AddressRenderer.displayAddress(null, city, streetType, correction.getCorrection(), null, null, null, getLocale());
        }

        @Override
        protected Class<? extends Page> getBackPageClass() {
            return StreetCorrectionList.class;
        }

        @Override
        protected void save() {
            addressCorrectionBean.insertStreet(getModel());
        }

        @Override
        protected void update() {
            addressCorrectionBean.updateStreet(getModel());
        }

        @Override
        protected void delete() {
            addressCorrectionBean.delete(getModel());
        }

        @Override
        protected boolean validateExistence() {
            return addressCorrectionBean.checkStreetExistence(getModel());
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            return new AddressCorrectionInputPanel(id, getModel());
        }

        @Override
        protected boolean freezeOrganization() {
            return true;
        }

        @Override
        protected List<String> getSearchFilters() {
            return ImmutableList.of("city", "street");
        }

        @Override
        protected boolean validate() {
            boolean valid = true;
            if (getModel().getParentId() == null) {
                error(getString("correction_required"));
                valid = false;
            }
            return valid && super.validate();
        }
    }

    /**
     * Панель редактирования коррекции дома.
     */
    private class BuildingCorrectionEditPanel extends AddressCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public BuildingCorrectionEditPanel(String id, Long correctionId) {
            super(id, "building", correctionId);
        }

        @Override
        protected BuildingCorrection getModel() {
            return (BuildingCorrection) super.getModel();
        }

        @Override
        protected BuildingCorrection initObjectCorrection(String entity, Long correctionId) {
            return addressCorrectionBean.findBuildingCorrectionById(correctionId);
        }

        @Override
        protected BuildingCorrection newObjectCorrection(String entity) {
            return new BuildingCorrection();
        }

        @Override
        protected String displayCorrection() {
            BuildingCorrection correction = getModel();

            String city = null;
            String street = null;
            if (correction.getParent() != null && correction.getParent().getParent() != null) {
                city = correction.getParent().getParent().getCorrection();
                street = correction.getParent().getCorrection();
            }
            return AddressRenderer.displayAddress(null, city, null, street, correction.getCorrection(), correction.getCorrectionCorp(), null,
                    getLocale());
        }

        @Override
        protected Class<? extends Page> getBackPageClass() {
            return BuildingCorrectionList.class;
        }

        @Override
        protected void save() {
            addressCorrectionBean.insertBuilding(getModel());
        }

        @Override
        protected void update() {
            addressCorrectionBean.updateBuilding(getModel());
        }

        @Override
        protected void delete() {
            addressCorrectionBean.delete(getModel());
        }

        @Override
        protected boolean validateExistence() {
            return addressCorrectionBean.checkBuildingExistence(getModel());
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            return new AddressCorrectionInputPanel(id, getModel());
        }

        @Override
        protected List<String> getSearchFilters() {
            return ImmutableList.of("city", "street", "building");
        }

        @Override
        protected boolean freezeOrganization() {
            return true;
        }

        @Override
        protected boolean validate() {
            boolean valid = true;
            if (getModel().getParentId() == null) {
                error(getString("correction_required"));
                valid = false;
            }
            return valid && super.validate();
        }
    }
    private AbstractCorrectionEditPanel addressEditPanel;

    public AddressCorrectionEdit(PageParameters params) {
        String entity = params.getString(CORRECTED_ENTITY);
        Long correctionId = params.getAsLong(CORRECTION_ID);
        if ("city".equals(entity)) {
            addressEditPanel = new CityCorrectionEditPanel("addressEditPanel", correctionId);
        } else if ("district".equals(entity)) {
            addressEditPanel = new DistrictCorrectionEditPanel("addressEditPanel", correctionId);
        } else if ("street".equals(entity)) {
            addressEditPanel = new StreetCorrectionEditPanel("addressEditPanel", correctionId);
        } else if ("building".equals(entity)) {
            addressEditPanel = new BuildingCorrectionEditPanel("addressEditPanel", correctionId);
        }
        add(addressEditPanel);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                addressEditPanel.executeDeletion();
            }

            @Override
            public boolean isVisible() {
                return !addressEditPanel.isNew();
            }
        });
        return toolbar;
    }
}

