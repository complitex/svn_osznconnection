/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;
import org.complitex.address.strategy.street.StreetStrategy;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;
import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Панель для корректировки адреса вручную, когда нет соответствующей коррекции и поиск по локальной адресной базе не дал результатов.
 * @author Artem
 */
public abstract class AddressCorrectionPanel<T extends AbstractRequest> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionPanel.class);

    private enum CORRECTED_ENTITY {

        CITY, STREET, BUILDING;
    }
    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;
    
    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;
    
    private CORRECTED_ENTITY correctedEntity;
    private Dialog dialog;
    private SearchComponent searchComponent;
    private SearchComponentState componentState;
    private FeedbackPanel messages;
    private WebMarkupContainer container;

    private String firstName;
    private String middleName;
    private String lastName;
    
    private String city;
    private String streetType;
    private String street;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;
    
    private Long cityId;
    private Long streetId;
    private Long buildingId;

    private T request;

    public AddressCorrectionPanel(String id, final Component... toUpdate) {
        super(id);

        //Диалог
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        dialog.setOutputMarkupId(true);
        add(dialog);

        //Контейнер для ajax
        container = new WebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);
        dialog.add(container);

        //Панель обратной связи
        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        container.add(new Label("name", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return lastName + " " + firstName + " " + middleName;
            }
        }));

        container.add(new Label("address", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return AddressRenderer.displayAddress(null, city, streetType, street, buildingNumber, buildingCorp, apartment, getLocale());
            }
        }));

        componentState = new SearchComponentState();
        // at start create fake search component
        searchComponent = new SearchComponent("searchComponent", componentState, ImmutableList.of(""), null, true);
        container.add(searchComponent);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    try {
                        correctAddress(request, getObjectId(componentState.get("city")), getObjectId(componentState.get("street")),
                                getStreetTypeId(componentState.get("street")), getObjectId(componentState.get("building")));

                        if (toUpdate != null) {
                            for (Component component : toUpdate) {
                                target.addComponent(component);
                            }
                        }
                        closeDialog(target);
                        return;
                    } catch (DublicateCorrectionException e) {
                        error(getString("dublicate_correction_error"));
                    } catch (MoreOneCorrectionException e) {
                        if ("city".equals(e.getEntity())) {
                            error(statusRenderService.displayStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION, getLocale()));
                        } else if ("street".equals(e.getEntity())) {
                            error(statusRenderService.displayStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION, getLocale()));
                        } else if ("street_type".equals(e.getEntity())){
                            error(statusRenderService.displayStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION, getLocale()));
                        }
                    } catch (NotFoundCorrectionException e) {
                        error(getString(e.getEntity() + "_not_found_correction"));
                    } catch (Exception e) {
                        error(getString("db_error"));
                        log.error("", e);
                    }
                }
                target.addComponent(messages);
            }
        };
        container.add(save);

        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        };
        container.add(cancel);
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private static Long getStreetTypeId(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    protected abstract void correctAddress(T request, Long cityId, Long streetId, Long streetTypeId, Long buildingId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException;

    protected boolean validate(SearchComponentState componentState) {
        boolean validated = true;
        switch (correctedEntity) {
            case BUILDING:
                DomainObject buildingObject = componentState.get("building");
                validated &= buildingObject != null && buildingObject.getId() != null && buildingObject.getId() > 0;
            case STREET:
                DomainObject streetObject = componentState.get("street");
                validated &= streetObject != null && streetObject.getId() != null && streetObject.getId() > 0;
            case CITY:
                DomainObject cityObject = componentState.get("city");
                validated &= cityObject != null && cityObject.getId() != null && cityObject.getId() > 0;
                break;
        }
        if (!validated) {
            error(getString("address_mistake"));
        }
        return validated;
    }

    private void initSearchComponentState(SearchComponentState componentState) {
        componentState.clear();
        Map<String, Long> ids = Maps.newHashMap();

        if (cityId != null) {
            ids.put("city", cityId);
            componentState.put("city", findObject(cityId, "city", ids));
        }

        if (streetId != null) {
            ids.put("street", streetId);
            componentState.put("street", findObject(streetId, "street", ids));
        }

        if (buildingId != null) {
            ids.put("building", buildingId);
            componentState.put("building", findObject(buildingId, "building", ids));
        }
    }

    private DomainObject findObject(Long objectId, String entity, Map<String, Long> ids) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        strategyFactory.getStrategy(entity).configureExample(example, ids, null);
        List<? extends DomainObject> objects = strategyFactory.getStrategy(entity).find(example);
        if (objects != null && !objects.isEmpty()) {
            return objects.get(0);
        }
        return null;
    }

    protected List<String> initFilters() {
        switch (correctedEntity) {
            case CITY:
                return ImmutableList.of("city");
            case STREET:
                return ImmutableList.of("city", "street");
            default:
                return ImmutableList.of("city", "street", "building");
        }
    }

    protected void initCorrectedEntity() {
        if (cityId == null) {
            correctedEntity = CORRECTED_ENTITY.CITY;
            return;
        }
        if (streetId == null) {
            correctedEntity = CORRECTED_ENTITY.STREET;
            return;
        }
        correctedEntity = CORRECTED_ENTITY.BUILDING;
    }

    private void closeDialog(AjaxRequestTarget target) {
        container.setVisible(false);
        target.addComponent(container);
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target, T request, String firstName, String middleName, String lastName, String city,
            String streetType, String street, String buildingNumber, String buildingCorp, String apartment, Long cityId, Long streetId, Long buildingId) {

        this.request = request;
            
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.city = city;
        this.streetType = streetType;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.buildingCorp = buildingCorp;
        this.apartment = apartment;
        this.cityId = cityId;
        this.streetId = streetId;
        this.buildingId = buildingId;

        initCorrectedEntity();
        initSearchComponentState(componentState);
        SearchComponent newSearchComponent = new SearchComponent("searchComponent", componentState, initFilters(), null, true);
        searchComponent.replaceWith(newSearchComponent);
        searchComponent = newSearchComponent;
        container.setVisible(true);
        target.addComponent(container);
        dialog.open(target);
    }
}