/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.lookup;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardListModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.accordion.Accordion;
import org.odlabs.wiquery.ui.accordion.AccordionAnimated;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.web.component.account.AccountNumberPickerPanel;
import org.odlabs.wiquery.ui.accordion.AccordionActive;

/**
 * 
 * @author Artem
 */
public abstract class AbstractLookupPanel<T extends AbstractRequest> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AbstractLookupPanel.class);
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StatusRenderService statusRenderService;
    private IModel<String> accountInfoModel;
    private IModel<String> apartmentModel;
    private IModel<List<? extends AccountDetail>> accountsModel;
    private IModel<AccountDetail> accountModel;
    private AccountNumberPickerPanel accountNumberPickerPanel;
    private FeedbackPanel messages;
    private Dialog dialog;
    private Accordion accordion;
    private Label accountInfo;
    private SearchComponentState addressSearchComponentState;
    private SearchComponent addressSearchComponent;
    private T request;
    private T initialRequest;
    private AccountNumberLookupPanel<T> accountLookupPanel;

    private class SimpleResourceModel extends AbstractReadOnlyModel<String> {

        private String resourceKey;

        public SimpleResourceModel(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        @Override
        public String getObject() {
            return AbstractLookupPanel.this.getString(resourceKey);
        }
    }

    public AbstractLookupPanel(String id, Component... toUpdate) {
        super(id);
        init(toUpdate);
    }

    private void init(final Component... toUpdate) {
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        dialog.add(messages);

        accordion = new Accordion("accordion");
        accordion.setAnimationEffect(new AccordionAnimated(false));
        accordion.setOutputMarkupPlaceholderTag(true);
        accordion.setAutoHeight(false);
        dialog.add(accordion);

        //lookup by address
        addressSearchComponentState = new SearchComponentState();

        accountInfoModel = new Model<String>();
        accountInfo = new Label("accountInfo", accountInfoModel);
        accountInfo.setOutputMarkupId(true);
        dialog.add(accountInfo);

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
                initInternalAddress(request, getObjectId(addressSearchComponentState.get("city")),
                        getObjectId(addressSearchComponentState.get("street")), getStreetType(addressSearchComponentState.get("street")),
                        getObjectId(addressSearchComponentState.get("building")), apartmentModel.getObject());

                boolean visible = accountNumberPickerPanel.isVisible();
                AccountDetail detail = null;
                accountNumberPickerPanel.setVisible(false);
                if (validateInternalAddress(request)) {
                    if (resolveOutgoingAddressSafely(request, target)) {
                        if (request.getStatus() == RequestStatus.ACCOUNT_NUMBER_NOT_FOUND) {
                            try {
                                List<AccountDetail> accountList = acquireAccountDetailsByAddress(request);
                                if (accountList == null || accountList.isEmpty()) {
                                    error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                                } else {
                                    if (accountList.size() == 1 && !Strings.isEmpty(accountList.get(0).getAccountNumber())) {
                                        detail = accountList.get(0);
                                    } else {
                                        accountsModel.setObject(accountList);
                                        accountNumberPickerPanel.clear();
                                        accountNumberPickerPanel.setVisible(true);
                                    }
                                }
                            } catch (DBException e) {
                                error(getString("remote_db_error"));
                                log.error("", e);
                            }
                        } else {
                            error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                        }
                    }
                }

                accountModel.setObject(detail);
                accountInfoModel.setObject(AccountNumberPickerPanel.displayAccountDetail(detail));
                target.addComponent(accountInfo);
                target.addComponent(messages);
                if (accountNumberPickerPanel.isVisible() || visible) {
                    accordion.setActive(new AccordionActive(0));
                    target.addComponent(accordion);
                }
            }
        };
        accordion.add(lookupByAddress);
        addressSearchComponent = new SearchComponent("addressSearchComponent", addressSearchComponentState,
                ImmutableList.of("city", "street", "building"), null, ShowMode.ACTIVE, true);
        addressSearchComponent.setOutputMarkupPlaceholderTag(true);
        addressSearchComponent.setVisible(false);
        accordion.add(addressSearchComponent);

        accountModel = new Model<AccountDetail>();
        accountsModel = new WildcardListModel<AccountDetail>();
        accountNumberPickerPanel = new AccountNumberPickerPanel("accountNumberPickerPanel", accountsModel) {

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                accountModel.setObject(accountDetail);
                accountInfoModel.setObject(AccountNumberPickerPanel.displayAccountDetail(accountDetail));
                target.addComponent(accountInfo);
            }
        };
        accountNumberPickerPanel.setVisible(false);
        accordion.add(accountNumberPickerPanel);

        //lookup by account number
        accountLookupPanel = new AccountNumberLookupPanel<T>("lookupByAccount",
                new SimpleResourceModel("lookup_by_account_label"), new SimpleResourceModel("lookup_by_account_required"), messages) {

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target, boolean refresh) {
                accountModel.setObject(accountDetail);
                accountInfoModel.setObject(AccountNumberLookupPanel.displayAccountDetail(accountDetail));
                target.addComponent(accountInfo);
                if (refresh) {
                    accordion.setActive(new AccordionActive(1));
                    target.addComponent(accordion);
                }
            }

            @Override
            protected String resolveOutgoingDistrict(T request) {
                return AbstractLookupPanel.this.resolveOutgoingDistrict(request);
            }
        };
        accordion.add(accountLookupPanel);

        // save/cancel
        dialog.add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    try {
                        updateAccountNumber(initialRequest, accountModel.getObject().getAccountNumber());
                        for (Component component : toUpdate) {
                            target.addComponent(component);
                        }
                        closeDialog(target);
                    } catch (Exception e) {
                        error(getString("db_error"));
                        log.error("", e);
                        target.addComponent(messages);
                    }
                } else {
                    target.addComponent(messages);
                }
            }
        });

        dialog.add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        });
    }

    protected void closeDialog(AjaxRequestTarget target) {
        addressSearchComponent.setVisible(false);
        target.addComponent(addressSearchComponent);
        dialog.close(target);
    }

    protected boolean validate() {
        AccountDetail accountDetail = accountModel.getObject();
        boolean validated = (accountDetail != null) && !Strings.isEmpty(accountDetail.getAccountNumber());
        if (!validated) {
            error(getString("account_number_not_chosen"));
        }
        return validated;
    }

    protected Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    protected Long getStreetType(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    protected abstract void initInternalAddress(T request, Long cityId, Long streetId, Long streetTypeId, Long buildingId, String apartment);

    protected void initSearchComponentState(SearchComponentState componentState, Long cityId, Long streetId, Long buildingId) {
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

    protected DomainObject findObject(Long objectId, String entity) {
        IStrategy strategy = strategyFactory.getStrategy(entity);
        return strategy.findById(objectId, true);
    }

    protected boolean validateInternalAddress(T request) {
        boolean validated = isInternalAddressCorrect(request);
        if (!validated) {
            error(getString("address_required"));
        }
        return validated;
    }

    protected abstract boolean isInternalAddressCorrect(T request);

    public void open(AjaxRequestTarget target, T request, Long cityId, Long streetId, Long buildingId, String apartment,
            String ownNumSr) {
        this.request = CloneUtil.cloneObject(request);
        this.initialRequest = request;

        accountModel.setObject(null);
        accountsModel.setObject(null);
        accountInfoModel.setObject(null);
        target.addComponent(accountInfo);

        //lookup by address
        apartmentModel.setObject(apartment);
        initSearchComponentState(addressSearchComponentState, cityId, streetId, buildingId);
        addressSearchComponent.reinitialize(target);
        addressSearchComponent.setVisible(true);

        if (accountNumberPickerPanel.isVisible()) {
            accountNumberPickerPanel.setVisible(false);
        }
        accountNumberPickerPanel.clear();

        //lookup by account number
        accountLookupPanel.initialize(request, null);

        target.addComponent(accordion);
        target.addComponent(messages);
        dialog.open(target);
    }

    public void open(AjaxRequestTarget target, T request, Long cityId, Long streetId, Long buildingId, String apartment) {
        open(target, request, cityId, streetId, buildingId, apartment, null);
    }

    private boolean resolveOutgoingAddressSafely(T request, AjaxRequestTarget target) {
        try {
            resolveOutgoingAddress(request);
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
            target.addComponent(messages);
            return false;
        }
        return true;
    }

    protected abstract void resolveOutgoingAddress(T request);

    protected abstract List<AccountDetail> acquireAccountDetailsByAddress(T request) throws DBException;

    protected abstract void updateAccountNumber(T request, String accountNumber);

    protected abstract String resolveOutgoingDistrict(T request);
}
