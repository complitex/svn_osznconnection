/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.account;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.util.AddressRenderer;
import org.complitex.osznconnection.file.entity.AccountDetail;

import java.util.List;

import static org.complitex.dictionary.util.StringUtil.valueOf;

/**
 * Панель для показа возможных вариантов выбора л/c по детальной информации,
 * когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public class AccountNumberPickerPanel extends Panel {

    private IModel<List<? extends AccountDetail>> accountDetailsModel;
    private IModel<AccountDetail> accountDetailModel;

    public AccountNumberPickerPanel(String id, IModel<List<? extends AccountDetail>> accountDetailsModel,
            IModel<AccountDetail> accountDetailModel) {
        super(id);
        this.accountDetailsModel = accountDetailsModel;
        this.accountDetailModel = accountDetailModel;
        init();
    }

    private void init() {
        final RadioGroup<AccountDetail> radioGroup = new RadioGroup<AccountDetail>("radioGroup", accountDetailModel);
        radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        add(radioGroup);

        ListView<AccountDetail> accountDetails = new ListView<AccountDetail>("accountDetails", accountDetailsModel) {

            @Override
            protected void populateItem(ListItem<AccountDetail> item) {
                AccountDetail detail = item.getModelObject();
                item.add(new Radio<>("radio", item.getModel(), radioGroup).setEnabled(!Strings.isEmpty(detail.getAccountNumber())));
                item.add(new Label("accountNumber", valueOf(detail.getAccountNumber())));
                item.add(new Label("serviceProviderCode", valueOf(detail.getServiceProviderCode())));
                item.add(new Label("name", valueOf(detail.getOwnerName())));
                item.add(new Label("address", displayAddress(detail)));
                item.add(new Label("megabankAccount", valueOf(detail.getMegabankAccountNumber())));
                item.add(new Label("puAccountNumberInfo", valueOf(detail.getServiceProviderAccountNumberInfo())));
                item.add(new Label("inn", valueOf(detail.getOwnerINN())));
            }
        };
        radioGroup.add(accountDetails);
    }

    private String displayAddress(AccountDetail detail) {
        return AddressRenderer.displayAddress(detail.getStreetType(), detail.getStreet(), detail.getBuildingNumber(), detail.getBuildingCorp(),
                detail.getApartment(), getLocale());
    }
}
