/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.account;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.AccountDetail;

import java.util.List;
import java.util.Locale;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 * Панель для показа возможных вариантов выбора л/c по детальной информации,
 * когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public abstract class AccountNumberPickerPanel extends Panel {

    private static final String RESOURCE_BUNDLE = AccountNumberPickerPanel.class.getName();
    private IModel<List<? extends AccountDetail>> accountDetailsModel;
    private IModel<AccountDetail> model;

    public AccountNumberPickerPanel(String id, IModel<List<? extends AccountDetail>> accountDetailsModel) {
        super(id);
        this.accountDetailsModel = accountDetailsModel;
        init();
    }

    private void init() {
        model = new Model<AccountDetail>();
        final RadioGroup<AccountDetail> radioGroup = new RadioGroup<AccountDetail>("radioGroup", model);
        radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateAccountNumber(model.getObject(), target);
            }
        });
        add(radioGroup);

        ListView<AccountDetail> details = new ListView<AccountDetail>("details", accountDetailsModel) {

            @Override
            protected void populateItem(ListItem<AccountDetail> item) {
                AccountDetail detail = item.getModelObject();
                item.add(new Radio<AccountDetail>("radio", item.getModel(), radioGroup).setEnabled(!Strings.isEmpty(detail.getAccountNumber())));
                item.add(new Label("accountNumber", StringUtil.valueOf(detail.getAccountNumber())));
                item.add(new Label("name", StringUtil.valueOf(detail.getOwnerName())));
                item.add(new Label("address", displayAddress(detail, getLocale())));
                item.add(new Label("megabankAccount", StringUtil.valueOf(detail.getMegabankAccountNumber())));
                item.add(new Label("puAccountNumberInfo", StringUtil.valueOf(detail.getPuAccountNumberInfo())));
                item.add(new Label("inn", StringUtil.valueOf(detail.getOwnerINN())));
            }
        };
        radioGroup.add(details);
    }

    private static String displayAddress(AccountDetail detail, Locale locale) {
        return AddressRenderer.displayAddress(detail.getStreetType(), detail.getStreet(), detail.getBuildingNumber(), detail.getBuildingCorp(),
                detail.getApartment(), locale);
    }

    public static String displayAccountDetail(AccountDetail detail, Locale locale) {
        if (detail == null) {
            return null;
        }
        String display = StringUtil.valueOf(detail.getAccountNumber()) + " ";
        display += StringUtil.valueOf(detail.getOwnerName());
        String address = displayAddress(detail, locale);
        if (!Strings.isEmpty(address)) {
            display += ", " + address;
        }
        if (!Strings.isEmpty(detail.getOwnerINN())) {
            display += ", " + ResourceUtil.getString(RESOURCE_BUNDLE, "inn", locale) + " : " + detail.getOwnerINN();
        }
        return display;
    }

    protected abstract void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target);

    public IModel<List<? extends AccountDetail>> getAccountDetailsModel() {
        return accountDetailsModel;
    }

    public void clear() {
        model.setObject(null);
    }
}
