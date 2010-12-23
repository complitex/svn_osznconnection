/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment.component.account;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.AccountDetail;

import java.util.List;
import org.complitex.dictionary.util.StringUtil;

/**
 * Панель для показа возможных вариантов выбора л/c по детальной информации,
 * когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public abstract class AccountNumberCorrectionPanel extends Panel {

    private static final String RESOURCE_BUNDLE = AccountNumberCorrectionPanel.class.getName();
    private IModel<List<? extends AccountDetail>> accountDetailsModel;
    private IModel<AccountDetail> model;

    public AccountNumberCorrectionPanel(String id, IModel<List<? extends AccountDetail>> accountDetailsModel) {
        super(id);
        this.accountDetailsModel = accountDetailsModel;
        init();
    }

    private void init() {
        model = new Model<AccountDetail>();
        IChoiceRenderer<AccountDetail> renderer = new IChoiceRenderer<AccountDetail>() {

            @Override
            public Object getDisplayValue(AccountDetail object) {
                return displayAccountDetail(object);
            }

            @Override
            public String getIdValue(AccountDetail object, int index) {
                return object.getAccountNumber();
            }
        };

        RadioChoice<AccountDetail> accounts = new RadioChoice<AccountDetail>("accounts", model, accountDetailsModel, renderer) {

            @Override
            protected boolean isDisabled(AccountDetail object, int index, String selected) {
                return Strings.isEmpty(object.getAccountNumber());
            }
        };
        accounts.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                AccountNumberCorrectionPanel.this.updateAccountNumber(model.getObject(), target);
            }
        });
        add(accounts);
    }

    public static String displayAccountDetail(AccountDetail accountDetail) {
        if(accountDetail == null){
            return null;
        }
        
        String display = StringUtil.valueOf(accountDetail.getAccountNumber()) + " ";
        display += StringUtil.valueOf(accountDetail.getOwnerName()) + " ";
        if (accountDetail.getOwnerINN() != null) {
            display += getStringResource("INN") + " : " + StringUtil.valueOf(accountDetail.getOwnerINN());
        }
        return display;
    }

    private static String getStringResource(String resourceKey) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, resourceKey, Session.get().getLocale());
    }

    protected abstract void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target);

    public IModel<List<? extends AccountDetail>> getAccountDetailsModel() {
        return accountDetailsModel;
    }

    public void clear(){
        model.setObject(null);
    }
}
