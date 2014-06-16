package org.complitex.osznconnection.file.web.pages.account;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.service.PersonAccountBean;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization_type.strategy.OsznOrganizationTypeStrategy;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;

/**
 * Список записей в локальной таблице номеров л/c.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class PersonAccountList extends ScrollListPage {
    @EJB
    private PersonAccountBean personAccountBean;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private OsznOrganizationStrategy organizationStrategy;

    @EJB

    private LocaleBean localeBean;

    public PersonAccountList() {
        init();
    }

    public PersonAccountList(PageParameters params) {
        super(params);
        init();
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);

        final IModel<FilterWrapper<PersonAccount>>  filterModel = newFilterModel(new PersonAccount());

        final DataProvider<PersonAccount> dataProvider = new DataProvider<PersonAccount>() {

            @Override
            protected Iterable<? extends PersonAccount> getData(long first, long count) {
                final FilterWrapper<PersonAccount> filterWrapper = filterModel.getObject();

                filterWrapper.setAscending(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    filterWrapper.setSortProperty(getSort().getProperty());
                }

                filterWrapper.setFirst(first);
                filterWrapper.setCount(count);
                filterWrapper.setLocale(localeBean.convert(getLocale()));

                return personAccountBean.getPersonAccounts(filterWrapper);
            }

            @Override
            protected int getSize() {
                return personAccountBean.getPersonAccountsCount(filterModel.getObject());
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<>("accountNumberFilter", new PropertyModel<String>(filterModel, "object.accountNumber")));
        filterForm.add(new TextField<>("puAccountNumberFilter", new PropertyModel<String>(filterModel, "object.puAccountNumber")));
        filterForm.add(new TextField<>("firstNameFilter", new PropertyModel<String>(filterModel, "object.firstName")));
        filterForm.add(new TextField<>("middleNameFilter", new PropertyModel<String>(filterModel, "object.middleName")));
        filterForm.add(new TextField<>("lastNameFilter", new PropertyModel<String>(filterModel, "object.lastName")));
        filterForm.add(new TextField<>("cityFilter", new PropertyModel<String>(filterModel, "object.city")));
        filterForm.add(new TextField<>("streetTypeFilter", new PropertyModel<String>(filterModel, "object.streetType")));
        filterForm.add(new TextField<>("streetFilter", new PropertyModel<String>(filterModel, "object.street")));
        filterForm.add(new TextField<>("buildingNumberFilter", new PropertyModel<String>(filterModel, "object.buildingNumber")));
        filterForm.add(new TextField<>("buildingCorpFilter", new PropertyModel<String>(filterModel, "object.buildingCorp")));
        filterForm.add(new TextField<>("apartmentFilter", new PropertyModel<String>(filterModel, "object.apartment")));

        filterForm.add(new OrganizationPicker("osznFilter", new PropertyModel<DomainObject>(filterModel, "object.organizationId"),
                OsznOrganizationTypeStrategy.SERVICING_ORGANIZATION_TYPE));

        filterForm.add(new OrganizationPicker("calculationCenterFilter", new PropertyModel<DomainObject>(filterModel, "object.calculationCenterId"),
                OsznOrganizationTypeStrategy.CALCULATION_CENTER_TYPE));

        filterForm.add(new OrganizationPicker("userOrganizationFilter", new PropertyModel<DomainObject>(filterModel, "object.userOrganizationId"),
                OsznOrganizationTypeStrategy.USER_ORGANIZATION_TYPE));


        AjaxLink<Void> reset = new AjaxLink<Void>("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                filterModel.setObject(FilterWrapper.of(new PersonAccount()));

                target.add(content);
            }
        };
        filterForm.add(reset);
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(content);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(submit);

        DataView<PersonAccount> data = new DataView<PersonAccount>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<PersonAccount> item) {
                PersonAccount personAccount = item.getModelObject();
                item.add(new Label("accountNumber", personAccount.getAccountNumber()));
                item.add(new Label("puAccountNumber", personAccount.getPuAccountNumber()));
                item.add(new Label("firstName", personAccount.getFirstName()));
                item.add(new Label("middleName", personAccount.getMiddleName()));
                item.add(new Label("lastName", personAccount.getLastName()));
                item.add(new Label("city", personAccount.getCity()));
                item.add(new Label("streetType", personAccount.getStreetType()));
                item.add(new Label("street", personAccount.getStreet()));
                item.add(new Label("buildingNumber", personAccount.getBuildingNumber()));
                item.add(new Label("buildingCorp", personAccount.getBuildingCorp()));
                item.add(new Label("apartment", personAccount.getApartment()));
                item.add(new Label("oszn", personAccount.getOrganizationName()));
                item.add(new Label("calculationCenter", personAccount.getCalculationCenterName()));

                //user organization
                item.add(new Label("userOrganization", personAccount.getUserOrganizationName()));

                item.add(new ScrollBookmarkablePageLink<PersonAccountEdit>("edit", PersonAccountEdit.class,
                        new PageParameters().set(PersonAccountEdit.CORRECTION_ID, personAccount.getId()),
                        String.valueOf(personAccount.getId())));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountNumberHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("puAccountNumberHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", "last_name", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", "first_name", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", "middle_name", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetTypeHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingNumberHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingCorpHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("osznHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("calculationCenterHeader", "id", dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("userOrganizationHeader", "id",dataProvider, data, content));

        content.add(new PagingNavigator("navigator", data, getPreferencesPage(), content));
    }
}
