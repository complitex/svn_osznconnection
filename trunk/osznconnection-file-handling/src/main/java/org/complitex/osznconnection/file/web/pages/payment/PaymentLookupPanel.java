/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.complitex.osznconnection.file.web.pages.payment.component.account.AccountNumberLookupPanel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
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
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.PaymentLookupBean;
import org.complitex.osznconnection.file.web.pages.payment.component.account.AccountNumberCorrectionPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.accordion.Accordion;
import org.odlabs.wiquery.ui.accordion.AccordionAnimated;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;
import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.odlabs.wiquery.ui.accordion.AccordionActive;

/**
 * Панель для поиска номера л/c по различным параметрам: по адресу, по номеру лиц. счета, по номеру в мегабанке.
 * @author Artem
 */
public class PaymentLookupPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(PaymentLookupPanel.class);

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "PaymentLookupBean")
    private PaymentLookupBean paymentLookupBean;

    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;

    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;
    
    private IModel<String> accountInfoModel;
    private IModel<String> apartmentModel;
    private IModel<List<? extends AccountDetail>> accountsModel;
    private IModel<AccountDetail> accountModel;
    private AccountNumberCorrectionPanel accountNumberCorrectionPanel;
    private FeedbackPanel messages;
    private Dialog dialog;
    private Accordion accordion;
    private Label accountInfo;
    private SearchComponentState addressSearchComponentState;
    private SearchComponent addressSearchComponent;
    private Payment payment;
    private Payment initialPayment;
    private AccountNumberLookupPanel ownNumSrAccountLookupPanel;
    private AccountNumberLookupPanel megabankAccountLookupPanel;

    public PaymentLookupPanel(String id, Component... toUpdate) {
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
        apartment.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        accordion.add(apartment);

        AjaxLink lookupByAddress = new AjaxLink("lookupByAddress") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                initInternalAddress(addressSearchComponentState);
                initApartment(apartmentModel.getObject());

                boolean visible = accountNumberCorrectionPanel.isVisible();
                AccountDetail detail = null;
                accountNumberCorrectionPanel.setVisible(false);
                if (validateInternalAddress()) {
                    paymentLookupBean.resolveOutgoingAddress(payment);

                    if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_NOT_FOUND) {
                        try {
                            List<AccountDetail> accountList = paymentLookupBean.acquireAccountDetailsByAddress(payment);
                            if (accountList == null || accountList.isEmpty()) {
                                error(statusRenderService.displayStatus(payment.getStatus(), getLocale()));
                            } else {
                                if (accountList.size() == 1 && !Strings.isEmpty(accountList.get(0).getAccountNumber())) {
                                    detail = accountList.get(0);
                                } else {
                                    accountsModel.setObject(accountList);
                                    accountNumberCorrectionPanel.clear();
                                    accountNumberCorrectionPanel.setVisible(true);
                                }
                            }
                        } catch (DBException e) {
                            error(getString("db_error"));
                        }
                    } else {
                        error(statusRenderService.displayStatus(payment.getStatus(), getLocale()));
                    }
                }

                accountModel.setObject(detail);
                accountInfoModel.setObject(AccountNumberCorrectionPanel.displayAccountDetail(detail));
                target.addComponent(accountInfo);
                target.addComponent(messages);
                if (accountNumberCorrectionPanel.isVisible() || visible) {
                    accordion.setActive(new AccordionActive(0));
                    target.addComponent(accordion);
                }
            }
        };
        accordion.add(lookupByAddress);
        addressSearchComponent = new SearchComponent("addressSearchComponent", addressSearchComponentState,
                ImmutableList.of("city", "street", "building"), null, true);
        addressSearchComponent.setOutputMarkupPlaceholderTag(true);
        addressSearchComponent.setVisible(false);
        accordion.add(addressSearchComponent);

        accountModel = new Model<AccountDetail>();
        accountsModel = new WildcardListModel<AccountDetail>();
        accountNumberCorrectionPanel = new AccountNumberCorrectionPanel("accountNumberCorrectionPanel", accountsModel) {

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                accountModel.setObject(accountDetail);
                accountInfoModel.setObject(AccountNumberCorrectionPanel.displayAccountDetail(accountDetail));
                target.addComponent(accountInfo);
            }
        };
        accountNumberCorrectionPanel.setVisible(false);
        accordion.add(accountNumberCorrectionPanel);

        class SimpleResourceModel extends AbstractReadOnlyModel<String> {

            private String resourceKey;

            public SimpleResourceModel(String resourceKey) {
                this.resourceKey = resourceKey;
            }

            @Override
            public String getObject() {
                return PaymentLookupPanel.this.getString(resourceKey);
            }
        }

        //lookup by OWN_NUM_SR
        ownNumSrAccountLookupPanel = new AccountNumberLookupPanel("ownNumSrAccountLookupPanel",
                new SimpleResourceModel("own_num_sr_label"), new SimpleResourceModel("own_num_sr_required"), messages) {

            @Override
            protected List<AccountDetail> acquireAccountDetailsByAccCode(Payment payment, String account) throws DBException {
                payment.setField(PaymentDBF.OWN_NUM_SR, account);
                return paymentLookupBean.acquireAccountDetailsByOsznAccount(payment);
            }

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
        };
        accordion.add(ownNumSrAccountLookupPanel);

        //lookup by megabank
        megabankAccountLookupPanel = new AccountNumberLookupPanel("megabankAccountLookupPanel",
                new SimpleResourceModel("megabank_label"), new SimpleResourceModel("megabank_required"), messages) {

            @Override
            protected List<AccountDetail> acquireAccountDetailsByAccCode(Payment payment, String account) throws DBException {
                return paymentLookupBean.acquireAccountDetailsByMegabankAccount(payment, account);
            }

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target, boolean refresh) {
                accountModel.setObject(accountDetail);
                accountInfoModel.setObject(AccountNumberLookupPanel.displayAccountDetail(accountDetail));
                target.addComponent(accountInfo);
                if (refresh) {
                    accordion.setActive(new AccordionActive(2));
                    target.addComponent(accordion);
                }
            }
        };
        accordion.add(megabankAccountLookupPanel);

        // save/cancel
        dialog.add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    updateAccountNumber(target);

                    for (Component component : toUpdate) {
                        target.addComponent(component);
                    }
                    closeDialog(target);
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

    private void closeDialog(AjaxRequestTarget target) {
        addressSearchComponent.setVisible(false);
        target.addComponent(addressSearchComponent);
        dialog.close(target);
    }

    private boolean validate() {
        boolean validated = accountModel.getObject() != null;
        if (!validated) {
            error(getString("account_number_not_chosen"));
        }
        return validated;
    }

    private Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private void initApartment(String apartment) {
        payment.setField(PaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    private Long getStreetType(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    private void initInternalAddress(SearchComponentState componentState) {
        payment.setInternalCityId(getObjectId(componentState.get("city")));
        payment.setInternalStreetId(getObjectId(componentState.get("street")));
        payment.setInternalStreetTypeId(getStreetType(componentState.get("street")));
        payment.setInternalBuildingId(getObjectId(componentState.get("building")));
    }

    private void initSearchComponentState(SearchComponentState componentState, Payment payment) {
        componentState.clear();
        Map<String, Long> ids = Maps.newHashMap();

        if (payment.getInternalCityId() != null) {
            ids.put("city", payment.getInternalCityId());
            componentState.put("city", findObject(payment.getInternalCityId(), "city", ids));
        }

        if (payment.getInternalStreetId() != null) {
            ids.put("street", payment.getInternalStreetId());
            componentState.put("street", findObject(payment.getInternalStreetId(), "street", ids));
        }

        if (payment.getInternalBuildingId() != null) {
            ids.put("building", payment.getInternalBuildingId());
            componentState.put("building", findObject(payment.getInternalBuildingId(), "building", ids));
        }
    }

    private DomainObject findObject(Long objectId, String entity, Map<String, Long> ids) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        strategyFactory.getStrategy(entity).configureExample(example, ids, null);
        List<? extends DomainObject> objects = strategyFactory.getStrategy(entity).find(example);
        if (objects.size() == 1) {
            return objects.get(0);
        }
        return null;
    }

    private boolean validateInternalAddress() {
        boolean validated = payment.getInternalCityId() != null && payment.getInternalCityId() > 0
                && payment.getInternalStreetId() != null && payment.getInternalStreetId() > 0
                && payment.getInternalBuildingId() != null && payment.getInternalBuildingId() > 0;
        if (!validated) {
            error(getString("address_required"));
        }
        return validated;
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        this.payment = CloneUtil.cloneObject(payment);
        this.initialPayment = payment;

        accountModel.setObject(null);
        accountsModel.setObject(null);
        accountInfoModel.setObject(null);
        target.addComponent(accountInfo);

        //lookup by address
        apartmentModel.setObject((String) payment.getField(PaymentDBF.FLAT));
        initSearchComponentState(addressSearchComponentState, payment);
        addressSearchComponent.reinitialize(target);
        addressSearchComponent.setVisible(true);

        if (accountNumberCorrectionPanel.isVisible()) {
            accountNumberCorrectionPanel.setVisible(false);
        }
        accountNumberCorrectionPanel.clear();

        //lookup by OWN_NUM_SR
        ownNumSrAccountLookupPanel.initialize(payment, (String) initialPayment.getField(PaymentDBF.OWN_NUM_SR));

        //lookup by megabank
        megabankAccountLookupPanel.initialize(payment, null);

        target.addComponent(accordion);
        target.addComponent(messages);
        dialog.open(target);
    }

    protected void updateAccountNumber(AjaxRequestTarget target) {
        initialPayment.setAccountNumber(accountModel.getObject().getAccountNumber());
        initialPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        paymentBean.updateAccountNumber(initialPayment);
    }
}
