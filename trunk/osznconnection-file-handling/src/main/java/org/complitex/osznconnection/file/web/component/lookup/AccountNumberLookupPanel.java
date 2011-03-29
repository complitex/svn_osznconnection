/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.lookup;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.adapter.exception.UnknownAccountNumberTypeException;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public abstract class AccountNumberLookupPanel<T extends AbstractRequest> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AccountNumberLookupPanel.class);

    @EJB
    private StatusRenderService statusRenderService;
    @EJB
    private LookupBean lookupBean;
    private IModel<String> accountModel;
    private IModel<List<? extends AccountDetail>> accountDetailsModel;
    private T request;
    private FeedbackPanel messages;
    private IModel<String> accountRequiredModel;
    private WebMarkupContainer detailsContainer;
    private IModel<AccountDetail> accountDetailModel;

    public AccountNumberLookupPanel(String id, IModel<String> accountLabelModel, IModel<String> accountRequiredModel, FeedbackPanel messages) {
        super(id);
        this.messages = messages;
        this.accountRequiredModel = accountRequiredModel;
        init(accountLabelModel);
    }

    private void init(IModel<String> accountLabelModel) {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);
        container.add(new Label("accountLabel", accountLabelModel));

        accountModel = new Model<String>();
        TextField<String> account = new TextField<String>("account", accountModel);
        account.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        container.add(account);

        detailsContainer = new WebMarkupContainer("detailsContainer");
        detailsContainer.setOutputMarkupPlaceholderTag(true);
        detailsContainer.setVisible(false);
        container.add(detailsContainer);

        accountDetailModel = new Model<AccountDetail>();
        final RadioGroup<AccountDetail> radioGroup = new RadioGroup<AccountDetail>("radioGroup", accountDetailModel);
        radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateAccountNumber(accountDetailModel.getObject(), target, false);
            }
        });
        detailsContainer.add(radioGroup);

        accountDetailsModel = new WildcardListModel<AccountDetail>();
        ListView<AccountDetail> details = new ListView<AccountDetail>("details", accountDetailsModel) {

            @Override
            protected void populateItem(ListItem<AccountDetail> item) {
                AccountDetail detail = item.getModelObject();

                item.add(new Radio<AccountDetail>("radio", item.getModel(), radioGroup).setEnabled(!Strings.isEmpty(detail.getAccountNumber())));
                item.add(new Label("accountNumber", StringUtil.valueOf(detail.getAccountNumber())));
                item.add(new Label("name", StringUtil.valueOf(detail.getOwnerName())));
                item.add(new Label("address", displayAddress(detail)));
                item.add(new Label("megabankAccount", StringUtil.valueOf(detail.getMegabankAccountNumber())));
                item.add(new Label("ownNumSrAccount", StringUtil.valueOf(detail.getOwnNumSr())));
            }
        };
        radioGroup.add(details);

        IndicatingAjaxLink<Void> lookup = new IndicatingAjaxLink<Void>("lookup") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                AccountDetail detail = null;
                boolean visible = detailsContainer.isVisible();
                detailsContainer.setVisible(false);
                if (validateAccount()) {
                    String outgoingDistrict = resolveOutgoingDistrict(request);
                    if (!Strings.isEmpty(outgoingDistrict)) {
                        try {
                            List<AccountDetail> accountDetails = acquireAccountDetailsByAccount(request, outgoingDistrict, accountModel.getObject());
                            if (accountDetails == null || accountDetails.isEmpty()) {
                                error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                                accountDetailsModel.setObject(null);
                            } else {
                                if (accountDetails.size() == 1 && !Strings.isEmpty(accountDetails.get(0).getAccountNumber())) {
                                    accountDetailsModel.setObject(null);
                                    detail = accountDetails.get(0);
                                } else {
                                    accountDetailsModel.setObject(accountDetails);
                                    accountDetailModel.setObject(null);
                                    detailsContainer.setVisible(true);
                                }
                            }
                        } catch (DBException e) {
                            error(getString("db_error"));
                            log.error("", e);

                        } catch (UnknownAccountNumberTypeException e){
                            error(getString("unknown_account_number_type"));
                        }
                    } else {
                        error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
                    }
                }

                target.addComponent(messages);
                updateAccountNumber(detail, target, visible || detailsContainer.isVisible());
            }
        };
        container.add(lookup);
    }

    protected boolean validateAccount() {
        boolean validated = !Strings.isEmpty(accountModel.getObject());
        if (!validated) {
            error(accountRequiredModel.getObject());
        }
        return validated;
    }

    public void initialize(T request, String account) {
        this.request = CloneUtil.cloneObject(request);
        accountModel.setObject(Strings.isEmpty(account) ? null : account);
        accountDetailModel.setObject(null);
        accountDetailsModel.setObject(null);
        detailsContainer.setVisible(false);
    }

    private static String displayAddress(AccountDetail detail) {
        return AddressRenderer.displayAddress(detail.getStreetType(), detail.getStreet(), detail.getBuildingNumber(), detail.getBuildingCorp(),
                detail.getApartment(), Session.get().getLocale());
    }

    public static String displayAccountDetail(AccountDetail detail) {
        if (detail == null) {
            return null;
        }
        String display = StringUtil.valueOf(detail.getAccountNumber()) + " ";
        display += StringUtil.valueOf(detail.getOwnerName());
        String address = displayAddress(detail);
        if (!Strings.isEmpty(address)) {
            display += ", " + address;
        }
        return display;
    }

    protected abstract String resolveOutgoingDistrict(T request);

    protected List<AccountDetail> acquireAccountDetailsByAccount(T request, String district, String account) throws DBException,
            UnknownAccountNumberTypeException{
         return lookupBean.acquireAccountDetailsByAccount(request, district, account);
    }

    protected abstract void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target, boolean refresh);
}
