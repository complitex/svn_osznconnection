/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.address;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.address.util.AddressRenderer;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель для корректировки адреса вручную, когда нет соответствующей коррекции и поиск по локальной адресной базе не дал результатов.
 * @author Artem
 */
public abstract class AddressCorrectionPanel<T extends AbstractRequest> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionPanel.class);

    public enum CORRECTED_ENTITY {

        CITY, STREET, STREET_TYPE, BUILDING;
    }
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StatusRenderService statusRenderService;
    @EJB
    private StreetTypeStrategy streetTypeStrategy;
    private CORRECTED_ENTITY correctedEntity;
    private Dialog dialog;
    private WiQuerySearchComponent searchComponent;
    private SearchComponentState componentState;
    private DisableAwareDropDownChoice<DomainObject> streetTypeSelect;
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
    private Long streetTypeId;
    private Long streetId;
    private Long buildingId;
    private T request;
    private IModel<DomainObject> streetTypeModel;

    public AddressCorrectionPanel(String id, final long userOrganizationId, final Component... toUpdate) {
        super(id);

        //Диалог
        dialog = new Dialog("dialog") {

            {
                getOptions().putLiteral("width", "auto");
            }
        };
        dialog.setModal(true);
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
        searchComponent = new WiQuerySearchComponent("searchComponent", componentState, ImmutableList.of(""), null, ShowMode.ACTIVE, true);
        container.add(searchComponent);

        DomainObjectExample example = new DomainObjectExample();
        List<? extends DomainObject> streetTypes = streetTypeStrategy.find(example);
        streetTypeModel = new Model<>();
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return streetTypeStrategy.displayFullName(object, getLocale());
            }
        };
        streetTypeSelect = new DisableAwareDropDownChoice<>("streetTypeSelect", streetTypeModel,
                streetTypes, renderer);
        streetTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update street type model.
            }
        });
        container.add(streetTypeSelect);

        AjaxLink<Void> save = new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    try {
                        if (correctedEntity != CORRECTED_ENTITY.STREET_TYPE) {
                            correctAddress(request, correctedEntity, getObjectId(componentState.get("city")),
                                    getStreetTypeId(componentState.get("street")), getObjectId(componentState.get("street")),
                                    getObjectId(componentState.get("building")), userOrganizationId);
                        } else {
                            correctAddress(request, correctedEntity, null, getObjectId(streetTypeModel.getObject()),
                                    null, null, userOrganizationId);
                        }

                        if (toUpdate != null) {
                            for (Component component : toUpdate) {
                                target.add(component);
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
                        } else if ("street_type".equals(e.getEntity())) {
                            error(statusRenderService.displayStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION, getLocale()));
                        }
                    } catch (NotFoundCorrectionException e) {
                        error(getString(e.getEntity() + "_not_found_correction"));
                    } catch (Exception e) {
                        error(getString("db_error"));
                        log.error("", e);
                    }
                }
                target.add(messages);
            }
        };
        container.add(save);

        AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {

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

    protected abstract void correctAddress(T request, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException;

    protected boolean validate(SearchComponentState componentState) {
        boolean validated = true;
        String errorMessageKey = null;
        switch (correctedEntity) {
            case BUILDING:
                DomainObject buildingObject = componentState.get("building");
                validated &= buildingObject != null && buildingObject.getId() != null && buildingObject.getId() > 0;
            case STREET:
                DomainObject streetObject = componentState.get("street");
                validated &= streetObject != null && streetObject.getId() != null && streetObject.getId() > 0;
            case CITY:
                errorMessageKey = "address_mistake";
                DomainObject cityObject = componentState.get("city");
                validated &= cityObject != null && cityObject.getId() != null && cityObject.getId() > 0;
                break;
            case STREET_TYPE:
                errorMessageKey = "street_type_required";
                DomainObject streetTypeObject = streetTypeModel.getObject();
                validated = streetTypeObject != null && streetTypeObject.getId() != null && streetTypeObject.getId() > 0;
                break;
        }
        if (!validated) {
            error(getString(errorMessageKey));
        }
        return validated;
    }

    private void initSearchComponentState(SearchComponentState componentState) {
        componentState.clear();

        if (cityId != null) {
            componentState.put("city", findObject(cityId, "city"));
        }

        if (streetId != null) {
            componentState.put("street", findObject(streetId, "street"));
        }

        if (buildingId != null) {
            componentState.put("building", findObject(buildingId, "building"));
        }
    }

    private DomainObject findObject(Long objectId, String entity) {
        IStrategy strategy = strategyFactory.getStrategy(entity);
        return strategy.findById(objectId, true);
    }

    protected List<String> initFilters() {
        switch (correctedEntity) {
            case CITY:
                return ImmutableList.of("city");
            case STREET:
                return ImmutableList.of("city", "street");
            case BUILDING:
                return ImmutableList.of("city", "street", "building");
        }
        return ImmutableList.of("city", "street", "building");
    }

    protected void initCorrectedEntity(boolean ignoreStreetType) {
        if (cityId == null) {
            correctedEntity = CORRECTED_ENTITY.CITY;
            return;
        }
        if (streetTypeId == null && !ignoreStreetType) {
            correctedEntity = CORRECTED_ENTITY.STREET_TYPE;
            return;
        }
        if (streetId == null) {
            correctedEntity = CORRECTED_ENTITY.STREET;
            return;
        }
        correctedEntity = CORRECTED_ENTITY.BUILDING;
    }

    protected void closeDialog(AjaxRequestTarget target) {
        //container.setVisible(false); access denied bug

        target.add(container);
        dialog.close(target);
    }

    /**
     * Only for non-street-type requests.
     * @param target
     * @param request
     * @param firstName
     * @param middleName
     * @param lastName
     * @param city
     * @param street
     * @param buildingNumber
     * @param buildingCorp
     * @param apartment
     * @param cityId
     * @param streetId
     * @param buildingId
     */
    public void open(AjaxRequestTarget target, T request, String firstName, String middleName, String lastName, String city,
            String street, String buildingNumber, String buildingCorp, String apartment, Long cityId, Long streetId, Long buildingId) {
        open(target, request, firstName, middleName, lastName, city, null, street, buildingNumber, buildingCorp,
                apartment, cityId, null, streetId, buildingId, false);
    }

    /**
     * Only for street-type-enabled requests.
     * @param target
     * @param request
     * @param firstName
     * @param middleName
     * @param lastName
     * @param city
     * @param streetType
     * @param street
     * @param buildingNumber
     * @param buildingCorp
     * @param apartment
     * @param cityId
     * @param streetTypeId
     * @param streetId
     * @param buildingId
     */
    public void open(AjaxRequestTarget target, T request, String firstName, String middleName, String lastName, String city,
            String streetType, String street, String buildingNumber, String buildingCorp, String apartment, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId) {
        open(target, request, firstName, middleName, lastName, city, streetType, street, buildingNumber, buildingCorp,
                apartment, cityId, streetTypeId, streetId, buildingId, true);
    }

    private void open(AjaxRequestTarget target, T request, String firstName, String middleName, String lastName, String city,
            String streetType, String street, String buildingNumber, String buildingCorp, String apartment, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId, boolean streetTypeEnabled) {
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
        this.streetTypeId = streetTypeId;
        this.streetId = streetId;
        this.buildingId = buildingId;

        initCorrectedEntity(!streetTypeEnabled);
        if (correctedEntity != CORRECTED_ENTITY.STREET_TYPE) {
            initSearchComponentState(componentState);
            WiQuerySearchComponent newSearchComponent = 
                    new WiQuerySearchComponent("searchComponent", componentState, initFilters(), null, ShowMode.ACTIVE, true);
            searchComponent.replaceWith(newSearchComponent);
            searchComponent = newSearchComponent;
            streetTypeSelect.setVisible(false);
        } else {
            streetTypeModel.setObject(null);
            searchComponent.setVisible(false);
            streetTypeSelect.setVisible(true);
        }

        container.setVisible(true);
        target.add(container);
        dialog.open(target);
    }
}
