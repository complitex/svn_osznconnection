/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.PaymentLookupBean;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public abstract class PaymentLookupPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(PaymentLookupPanel.class);

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "PaymentLookupBean")
    private PaymentLookupBean paymentLookupBean;

    private IModel<Payment> paymentModel;

    private Dialog dialog;

    private Label accountNumber;

    private IModel<String> accountNumberModel;

    private IModel<String> apartmentModel;

    private TextField<String> apartment;

    private SearchComponentState componentState;

    private IModel<List<? extends AccountDetail>> accountsModel;

    private IModel<AccountDetail> accountModel;

    private WebMarkupContainer chooseAccountNumberContainer;

    private SearchComponent searchComponent;

    private static class FakeSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
        }
    }

    public PaymentLookupPanel(String id, Payment payment) {
        super(id);
        this.paymentModel = new Model<Payment>(payment);
        init();
    }

    private void init() {
        log.info("init");
        add(CSSPackageResource.getHeaderContribution(PaymentLookupPanel.class, PaymentLookupPanel.class.getSimpleName() + ".css"));

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
        add(dialog);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        dialog.add(messages);

        componentState = new SearchComponentState();
        initSearchComponentState(componentState, paymentModel.getObject());
        accountNumberModel = new Model<String>();
        accountNumber = new Label("accountNumber", accountNumberModel);
        accountNumber.setOutputMarkupId(true);
        dialog.add(accountNumber);

        apartmentModel = new Model<String>();
        Label apartmentLabel = new Label("apartmentLabel", new ResourceModel("apartment_label"));
        dialog.add(apartmentLabel);
        apartment = new TextField<String>("apartment", apartmentModel);
        apartment.setOutputMarkupId(true);
        apartment.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        dialog.add(apartment);

        AjaxLink lookup = new AjaxLink("lookup") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                initInternalAddress(componentState);
                initApartment(apartmentModel.getObject());
                log.info("city: [{}], street: [{}], building: [{}], apartment: [{}]", new Object[]{paymentModel.getObject().getInternalCityId(),
                            paymentModel.getObject().getInternalStreetId(), paymentModel.getObject().getInternalBuildingId(),
                            paymentModel.getObject().getField(PaymentDBF.FLAT)});
                if (validateInternalAddress()) {
                    paymentLookupBean.resolveOutgoingAddress(paymentModel.getObject());
                    if (paymentModel.getObject().getStatus() == Status.ACCOUNT_NUMBER_NOT_FOUND) {
                        List<AccountDetail> accountList = paymentLookupBean.getAccounts(paymentModel.getObject());
                        log.info("Accounts : {}", accountList);

                        if (paymentModel.getObject().getStatus() == Status.ACCOUNT_NUMBER_NOT_FOUND) {
                            accountNumberModel.setObject(StatusRenderer.displayValue(Status.ACCOUNT_NUMBER_NOT_FOUND));
                            accountModel.setObject(null);
                            target.addComponent(accountNumber);
                        } else {
                            if (accountList.size() == 1) {
                                accountModel.setObject(accountList.get(0));
                                accountNumberModel.setObject(accountList.get(0).getAccountNumber());
                                target.addComponent(accountNumber);
                                chooseAccountNumberContainer.setVisible(false);
                                target.addComponent(chooseAccountNumberContainer);
                            } else {
                                accountModel.setObject(null);
                                accountNumberModel.setObject(null);
                                target.addComponent(accountNumber);
                                accountsModel.setObject(accountList);
                                chooseAccountNumberContainer.setVisible(true);
                                target.addComponent(chooseAccountNumberContainer);
                            }
                        }
                    } else {
                        error(StatusRenderer.displayValue(paymentModel.getObject().getStatus()));
                    }
                }
                target.addComponent(messages);
            }
        };
        dialog.add(lookup);
        searchComponent = new SearchComponent("searchComponent", componentState, ImmutableList.of("city", "street", "building"),
                new FakeSearchCallback(), true);
        searchComponent.setOutputMarkupId(true);
        dialog.add(searchComponent);

        chooseAccountNumberContainer = new WebMarkupContainer("chooseAccountNumberContainer");
        chooseAccountNumberContainer.setOutputMarkupPlaceholderTag(true);
        chooseAccountNumberContainer.setVisible(false);
        dialog.add(chooseAccountNumberContainer);

        IChoiceRenderer<AccountDetail> renderer = new IChoiceRenderer<AccountDetail>() {

            @Override
            public Object getDisplayValue(AccountDetail object) {
                StringBuilder displayValueBuilder = new StringBuilder().append(object.getAccountNumber());

                if (!Strings.isEmpty(object.getOwnerName())) {
                    displayValueBuilder.append(", ").append(object.getOwnerName());
                }
                if (!Strings.isEmpty(object.getOwnerINN())) {
                    displayValueBuilder.append(", ").append(getString("INN")).append(" : ").append(object.getOwnerINN());
                }
                return displayValueBuilder.toString();
            }

            @Override
            public String getIdValue(AccountDetail object, int index) {
                return object.getAccountNumber();
            }
        };
        accountModel = new Model<AccountDetail>();
        accountsModel = new WildcardListModel<AccountDetail>();
        RadioChoice<AccountDetail> accounts = new RadioChoice<AccountDetail>("accountDetails", accountModel,
                accountsModel, renderer);
        accounts.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                log.info("Account number changed to [{}]", accountModel.getObject());
                accountNumberModel.setObject(accountModel.getObject().getAccountNumber());
                target.addComponent(accountNumber);
            }
        });
        chooseAccountNumberContainer.add(accounts);

        dialog.add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                log.info("account : {}", accountModel.getObject());
                if (validate()) {
                    updateAccountNumber(accountModel.getObject().getAccountNumber());
                    dialog.close(target);
                } else {
                    target.addComponent(messages);
                }
            }
        });
        dialog.add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    private boolean validate() {
        boolean validated = accountModel.getObject() != null;
        if (!validated) {
            error(getString("account_number_not_chosen"));
        }
        return validated;
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private static Long getObjectTypeId(DomainObject object) {
        return object == null ? null : object.getEntityTypeId();
    }

    private void initApartment(String apartment) {
        paymentModel.getObject().setField(PaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    private void initInternalAddress(SearchComponentState componentState) {
        paymentModel.getObject().setInternalCityId(getObjectId(componentState.get("city")));
        paymentModel.getObject().setInternalStreetId(getObjectId(componentState.get("street")));
        paymentModel.getObject().setInternalStreetTypeId(getObjectTypeId(componentState.get("street")));
        paymentModel.getObject().setInternalBuildingId(getObjectId(componentState.get("building")));
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
        DomainObject object = null;
        DomainObjectExample example = new DomainObjectExample();
        example.setId(objectId);
        strategyFactory.getStrategy(entity).configureExample(example, ids, null);
        List<DomainObject> objects = strategyFactory.getStrategy(entity).find(example);
        if (objects != null && !objects.isEmpty()) {
            object = objects.get(0);
        }
        return object;
    }

    private boolean validateInternalAddress() {
        boolean validated = paymentModel.getObject().getInternalCityId() != null && paymentModel.getObject().getInternalCityId() > 0
                && paymentModel.getObject().getInternalStreetId() != null && paymentModel.getObject().getInternalStreetId() > 0
                && paymentModel.getObject().getInternalBuildingId() != null && paymentModel.getObject().getInternalBuildingId() > 0;
        if (!validated) {
            error(getString("address_required"));
        }
        return validated;
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        log.info("open");
        this.paymentModel.setObject(payment);
        accountModel.setObject(null);
        accountNumberModel.setObject(null);
        target.addComponent(accountNumber);
        apartmentModel.setObject((String) payment.getField(PaymentDBF.FLAT));
        target.addComponent(apartment);
        initSearchComponentState(componentState, payment);
        target.addComponent(searchComponent);
        dialog.open(target);
    }

    protected abstract void updateAccountNumber(String accountNumber);
}
