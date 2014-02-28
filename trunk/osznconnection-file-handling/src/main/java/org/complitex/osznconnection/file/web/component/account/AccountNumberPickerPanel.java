/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.account;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.AccountDetail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.wicket.model.Model.of;

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

        setOutputMarkupId(true);

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

        final IModel<AccountDetail> filterModel = Model.of(new AccountDetail());

        add(new TextField<>("accCodeFilter", new PropertyModel<>(filterModel, "accCode")));
        add(new TextField<>("zheuFilter", new PropertyModel<>(filterModel, "zheu")));
        add(new TextField<>("zheuCodeFilter", new PropertyModel<>(filterModel, "zheuCode")));
        add(new TextField<>("ownerFioFilter", new PropertyModel<>(filterModel, "ownerFio")));
        add(new TextField<>("addressFilter", new PropertyModel<>(filterModel, "address")));
        add(new TextField<>("ownerInnFilter", new PropertyModel<>(filterModel, "ownerINN")));
        add(new TextField<>("ercCodeFilter", new PropertyModel<>(filterModel, "ercCode")));

        add(new AjaxLink("find") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(AccountNumberPickerPanel.this);
            }
        });

        add(new AjaxLink("filter_reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                filterModel.setObject(new AccountDetail());
            }
        });

        DataProvider<AccountDetail> dataProvider = new DataProvider<AccountDetail>() {
            @Override
            protected Iterable<? extends AccountDetail> getData(int first, int count) {
                Iterable<? extends AccountDetail> it = Iterables.filter(accountDetailsModel.getObject(),
                        new Predicate<AccountDetail>() {
                            private boolean apply(String f, String input){
                                return f == null || (input != null && input.contains(f));
                            }

                            @Override
                            public boolean apply(AccountDetail input) {
                                AccountDetail f = filterModel.getObject();

                                return apply(f.getAccCode(), input.getAccCode())
                                        && apply(f.getAccCode(), input.getAccCode())
                                        && apply(f.getZheu(), input.getZheu())
                                        && apply(f.getZheuCode(), input.getZheuCode())
                                        && apply(f.getOwnerFio(), input.getOwnerFio())
                                        && apply(f.getAddress(), input.displayAddress(getLocale()))
                                        && apply(f.getOwnerINN(), input.getOwnerINN())
                                        && apply(f.getErcCode(), input.getErcCode());
                            }
                        });

                List<AccountDetail> list = Lists.newArrayList(it).subList(first, count);

                Collections.sort(list, new Comparator<AccountDetail>() {
                    @Override
                    public int compare(AccountDetail o1, AccountDetail o2) {
                        boolean asc = getSort().isAscending();

                        switch (getSort().getProperty()){
                            case "accCode": return StringUtil.compare(o1.getAccCode(), o2.getAccCode(), asc);
                            case "zheu": return StringUtil.compare(o1.getZheu(), o2.getZheu(), asc);
                            case "zheuCode": return StringUtil.compare(o1.getZheuCode(), o2.getZheuCode(), asc);
                            case "ownerFio": return StringUtil.compare(o1.getOwnerFio(), o2.getOwnerFio(), asc);
                            case "address": return StringUtil.compare(o1.displayAddress(getLocale()), o2.displayAddress(getLocale()), asc);
                            case "ownerInn": return StringUtil.compare(o1.getOwnerINN(), o2.getOwnerINN(), asc);
                            case "ercCode": return StringUtil.compare(o1.getErcCode(), o2.getErcCode(), asc);
                        }

                        return 0;
                    }
                });

                return list;
            }

            @Override
            protected int getSize() {
                return accountDetailsModel.getObject().size();
            }
        };

        DataView<AccountDetail> accountDetails = new DataView<AccountDetail>("accountDetails", dataProvider) {

            @Override
            protected void populateItem(Item<AccountDetail> item) {
                AccountDetail detail = item.getModelObject();
                item.add(new Radio<>("radio", item.getModel(), radioGroup).setEnabled(!Strings.isEmpty(detail.getAccCode())));
                item.add(new Label("accCode", of(detail.getAccCode())));
                item.add(new Label("zheu", of(detail.getZheu())));
                item.add(new Label("zheuCode", of(detail.getZheuCode())));
                item.add(new Label("name", of(detail.getOwnerFio())));
                item.add(new Label("address", of(detail.displayAddress(getLocale()))));
                item.add(new Label("ownerInn", of(detail.getOwnerINN())));
                item.add(new Label("ercCode", of(detail.getErcCode())));
            }
        };
        radioGroup.add(accountDetails);

        add(new ArrowOrderByBorder("accCodeHeader", "accCode", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("zheuHeader", "zheu", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("zheuCodeHeader", "zheuCode", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("ownerFioHeader", "ownerFio", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("addressHeader", "address", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("ownerInnHeader", "ownerInn", dataProvider, accountDetails, this));
        add(new ArrowOrderByBorder("ercCodeHeader", "ercCode", dataProvider, accountDetails, this));

        radioGroup.add(new PagingNavigator("navigator", accountDetails, AccountNumberPickerPanel.class.getName(), this));
    }
}
