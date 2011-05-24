/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.lookup;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.adapter.exception.UnknownAccountNumberTypeException;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.web.component.account.AccountNumberPickerPanel;
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
    private AccountNumberPickerPanel accountNumberPickerPanel;

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

        accountDetailsModel = new WildcardListModel<AccountDetail>();
        accountNumberPickerPanel = new AccountNumberPickerPanel("accountNumberPickerPanel", accountDetailsModel) {

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                AccountNumberLookupPanel.this.updateAccountNumber(accountDetail, target, false);
            }
        };
        detailsContainer.add(accountNumberPickerPanel);

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
                                    accountNumberPickerPanel.clear();
                                    detailsContainer.setVisible(true);
                                }
                            }
                        } catch (DBException e) {
                            error(getString("db_error"));
                            log.error("", e);

                        } catch (UnknownAccountNumberTypeException e) {
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
        accountNumberPickerPanel.clear();
        accountDetailsModel.setObject(null);
        detailsContainer.setVisible(false);
    }

    protected abstract String resolveOutgoingDistrict(T request);

    protected List<AccountDetail> acquireAccountDetailsByAccount(T request, String district, String account) throws DBException,
            UnknownAccountNumberTypeException {
        return lookupBean.acquireAccountDetailsByAccount(request, district, account);
    }

    protected abstract void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target, boolean refresh);
}
