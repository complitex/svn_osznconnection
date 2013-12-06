package org.complitex.osznconnection.file.web.component.lookup;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;
import org.complitex.osznconnection.file.web.component.account.AccountNumberPickerPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.accordion.Accordion;
import org.odlabs.wiquery.ui.accordion.AccordionActive;
import org.odlabs.wiquery.ui.accordion.AccordionAnimated;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;

import static org.apache.wicket.util.string.Strings.isEmpty;

public abstract class AbstractLookupPanel<T extends AbstractRequest> extends Panel {

    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StatusRenderService statusRenderService;
    @EJB
    private LookupBean lookupBean;
    private IModel<String> apartmentModel;
    private IModel<List<? extends AccountDetail>> accountDetailsModel;
    private IModel<AccountDetail> accountDetailModel;
    private AccountNumberPickerPanel accountNumberPickerPanel;
    private FeedbackPanel messages;
    private Dialog dialog;
    private Accordion accordion;
    private SearchComponentState addressSearchComponentState;
    private WiQuerySearchComponent addressSearchComponent;
    private T request;
    private T initialRequest;
    private IModel<String> accountNumberModel;
    private int lastAccordionActive;
    private final long userOrganizationId;

    public AbstractLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id);
        this.userOrganizationId = userOrganizationId;
        init(toUpdate);
    }

    private void init(final Component... toUpdate) {
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(680);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        dialog.add(messages);

        accordion = new Accordion("accordion");
        accordion.setAnimated(new AccordionAnimated(false));
        accordion.setOutputMarkupPlaceholderTag(true);
        accordion.setAutoHeight(false);
        dialog.add(accordion);

        //lookup by address
        addressSearchComponentState = new SearchComponentState();
        apartmentModel = new Model<String>();
        TextField<String> apartment = new TextField<String>("apartment", apartmentModel);
        apartment.setOutputMarkupId(true);
        apartment.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        accordion.add(apartment);

        IndicatingAjaxLink<Void> lookupByAddress = new IndicatingAjaxLink<Void>("lookupByAddress") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                lastAccordionActive = 0;

                boolean wasVisible = accountNumberPickerPanel.isVisible();
                accountDetailsModel.setObject(null);
                accountDetailModel.setObject(null);

                initInternalAddress(request, getObjectId(addressSearchComponentState.get("city")),
                        getObjectId(addressSearchComponentState.get("street")), getStreetType(addressSearchComponentState.get("street")),
                        getObjectId(addressSearchComponentState.get("building")), apartmentModel.getObject());

                if (isInternalAddressCorrect(request)) {
                    boolean outgoingAddressResolved = false;
                    try {
                        resolveOutgoingAddress(request, userOrganizationId);
                        outgoingAddressResolved = true;
                    } catch (Exception e) {
                        error(getString("db_error"));
                        LoggerFactory.getLogger(getClass()).error("", e);
                    }
                    if (outgoingAddressResolved) {
                        if (request.getStatus() == RequestStatus.ACCOUNT_NUMBER_NOT_FOUND) {
                            try {
                                List<AccountDetail> accountDetails = acquireAccountDetailsByAddress(request, userOrganizationId);
                                if (accountDetails == null || accountDetails.isEmpty()) {
                                    error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                                } else {
                                    accountDetailsModel.setObject(accountDetails);
                                    if (accountDetails.size() == 1) {
                                        accountDetailModel.setObject(accountDetails.get(0));
                                    }
                                }
                            } catch (DBException e) {
                                error(getString("remote_db_error"));
                                LoggerFactory.getLogger(getClass()).error("", e);
                            }
                        } else {
                            error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                        }
                    }
                } else {
                    error(getString("address_required"));
                }

                target.add(messages);
                boolean becameVisible = accountDetailsModel.getObject() != null && !accountDetailsModel.getObject().isEmpty();
                accountNumberPickerPanel.setVisible(becameVisible);
                if (wasVisible || becameVisible) {
                    target.add(accountNumberPickerPanel);
                }
            }
        };
        accordion.add(lookupByAddress);
        addressSearchComponent = new WiQuerySearchComponent("addressSearchComponent", addressSearchComponentState,
                ImmutableList.of("city", "street", "building"), null, ShowMode.ACTIVE, true);
        addressSearchComponent.setOutputMarkupPlaceholderTag(true);
        addressSearchComponent.setVisible(false);
        accordion.add(addressSearchComponent);

        //lookup by account number
        accountNumberModel = new Model<>();
        TextField<String> accountNumber = new TextField<String>("accountNumber", accountNumberModel);
        accountNumber.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        accordion.add(accountNumber);
        IndicatingAjaxLink<Void> lookupByAccount = new IndicatingAjaxLink<Void>("lookupByAccount") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                lastAccordionActive = 1;

                boolean wasVisible = accountNumberPickerPanel.isVisible();
                accountDetailsModel.setObject(null);
                accountDetailModel.setObject(null);

                if (!isEmpty(accountNumberModel.getObject())) {
                    String outgoingDistrict = null;
                    try {
                        outgoingDistrict = resolveOutgoingDistrict(request, userOrganizationId);
                    } catch (Exception e) {
                        LoggerFactory.getLogger(getClass()).error("", e);
                        error(getString("db_error"));
                    }
                    if (!isEmpty(outgoingDistrict)) {
                        try {
                            List<AccountDetail> accountDetails = acquireAccountDetailsByAccount(request, outgoingDistrict,
                                    accountNumberModel.getObject(), userOrganizationId);
                            if (accountDetails == null || accountDetails.isEmpty()) {
                                error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                            } else {
                                accountDetailsModel.setObject(accountDetails);
                                if (accountDetails.size() == 1) {
                                    accountDetailModel.setObject(accountDetails.get(0));
                                }
                            }
                        } catch (DBException e) {
                            error(getString("remote_db_error"));
                            LoggerFactory.getLogger(getClass()).error("", e);
                        } catch (UnknownAccountNumberTypeException e) {
                            error(getString("unknown_account_number_type"));
                        }
                    } else {
                        error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                    }
                } else {
                    error(getString("lookup_by_account_required"));
                }

                target.add(messages);
                boolean becameVisible = accountDetailsModel.getObject() != null && !accountDetailsModel.getObject().isEmpty();
                accountNumberPickerPanel.setVisible(becameVisible);
                if (wasVisible || becameVisible) {
                    target.add(accountNumberPickerPanel);
                }
            }
        };
        accordion.add(lookupByAccount);

        //account number picker panel
        accountDetailModel = new Model<AccountDetail>();
        accountDetailsModel = Model.ofList(null);
        accountNumberPickerPanel = new AccountNumberPickerPanel("accountNumberPickerPanel", accountDetailsModel, accountDetailModel);
        accountNumberPickerPanel.setOutputMarkupPlaceholderTag(true);
        accountNumberPickerPanel.setVisible(false);
        dialog.add(accountNumberPickerPanel);

        // save/cancel
        dialog.add(new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (accountDetailModel.getObject() != null
                        && !isEmpty(accountDetailModel.getObject().getAccountNumber())) {
                    try {
                        updateAccountNumber(initialRequest, accountDetailModel.getObject().getAccountNumber(), userOrganizationId);
                        for (Component component : toUpdate) {
                            target.add(component);
                        }
                        closeDialog(target);
                    } catch (Exception e) {
                        error(getString("db_error"));
                        LoggerFactory.getLogger(getClass()).error("", e);
                        target.add(messages);
                    }
                } else {
                    error(getString("account_number_not_chosen"));
                    target.add(messages);
                }
            }
        });

        dialog.add(new AjaxLink<Void>("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        });
    }

    protected void closeDialog(AjaxRequestTarget target) {
        addressSearchComponent.setVisible(false);
        target.add(addressSearchComponent);
        dialog.close(target);
    }

    protected final Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    protected Long getStreetType(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    protected abstract void initInternalAddress(T request, Long cityId, Long streetId, Long streetTypeId, Long buildingId, String apartment);

    protected final void initSearchComponentState(SearchComponentState componentState, Long cityId, Long streetId, Long buildingId) {
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

    protected final DomainObject findObject(Long objectId, String entity) {
        IStrategy strategy = strategyFactory.getStrategy(entity);
        return strategy.findById(objectId, true);
    }

    protected abstract boolean isInternalAddressCorrect(T request);

    public void open(AjaxRequestTarget target, T request, Long cityId, Long streetId, Long buildingId, String apartment,
            String serviceProviderAccountNumber, boolean immediatelySearchByAddress) {
        this.request = CloneUtil.cloneObject(request);
        this.initialRequest = request;

        accountDetailModel.setObject(null);
        accountDetailsModel.setObject(null);
        accountNumberPickerPanel.setVisible(false);
        target.add(accountNumberPickerPanel);

        //lookup by address
        apartmentModel.setObject(apartment);
        initSearchComponentState(addressSearchComponentState, cityId, streetId, buildingId);
        addressSearchComponent.reinitialize(target);
        addressSearchComponent.setVisible(true);

        //lookup by account number
        if (!Strings.isEmpty(serviceProviderAccountNumber) && !"0".equals(serviceProviderAccountNumber)) {
            accountNumberModel.setObject(serviceProviderAccountNumber);
        } else {
            accountNumberModel.setObject(null);
        }

        //set active accordion item
        if (immediatelySearchByAddress) {
            lastAccordionActive = 0;
            target.appendJavaScript("(function(){ $('#lookupByAddress .lookupByAddressButton').click(); })()");
        }
        accordion.setActive(new AccordionActive(lastAccordionActive));

        target.add(accordion);
        target.add(messages);
        dialog.open(target);
    }

    protected final List<AccountDetail> acquireAccountDetailsByAccount(T request, String district, String account,
            long userOrganizationId) throws DBException, UnknownAccountNumberTypeException {
        return lookupBean.acquireAccountDetailsByAccount(request, district, account, userOrganizationId);
    }

    protected abstract void resolveOutgoingAddress(T request, long userOrganizationId);

    protected abstract List<AccountDetail> acquireAccountDetailsByAddress(T request, long userOrganizationId) throws DBException;

    protected abstract void updateAccountNumber(T request, String accountNumber, long userOrganizationId);

    protected abstract String resolveOutgoingDistrict(T request, long userOrganizationId);
}
