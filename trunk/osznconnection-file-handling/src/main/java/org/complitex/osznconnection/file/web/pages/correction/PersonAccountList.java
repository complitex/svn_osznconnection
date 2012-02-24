/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.osznconnection.file.web.model.OrganizationModel;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.pages.ScrollListPage;

/**
 * Список записей в локальной таблице номеров л/c.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class PersonAccountList extends ScrollListPage {

    @EJB
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private LocaleBean localeBean;
    private IModel<PersonAccountExample> example;

    public PersonAccountList() {
        init();
    }

    public PersonAccountList(PageParameters params) {
        super(params);
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private PersonAccountExample newExample() {
        return new PersonAccountExample();
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        example = new Model<PersonAccountExample>((PersonAccountExample) getFilterObject(newExample()));

        final DataProvider<PersonAccount> dataProvider = new DataProvider<PersonAccount>() {

            @Override
            protected Iterable<? extends PersonAccount> getData(int first, int count) {
                //save preferences to session
                setFilterObject(example.getObject());
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                example.getObject().setLocaleId(localeBean.convert(getLocale()).getId());
                return personAccountLocalBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return personAccountLocalBean.count(example.getObject());
            }
        };
        dataProvider.setSort(getSortProperty(""), getSortOrder(true));

        filterForm.add(new TextField<String>("puAccountNumberFilter", new PropertyModel<String>(example, "puAccountNumber")));
        filterForm.add(new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<String>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<String>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<String>("buildingNumberFilter", new PropertyModel<String>(example, "buildingNumber")));
        filterForm.add(new TextField<String>("buildingCorpFilter", new PropertyModel<String>(example, "buildingCorp")));
        filterForm.add(new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment")));
        filterForm.add(new TextField<String>("accountNumberFilter", new PropertyModel<String>(example, "accountNumber")));

        final IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        final IModel<DomainObject> osznModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return example.getObject().getOsznId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                example.getObject().setOsznId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return osznsModel.getObject();
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("osznFilter",
                osznModel, osznsModel, organizationRenderer).setNullValid(true));

        final IModel<List<DomainObject>> calculationCentresModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllCalculationCentres(getLocale());
            }
        };
        final IModel<DomainObject> calculationCenterModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return example.getObject().getCalculationCenterId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                example.getObject().setCalculationCenterId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return calculationCentresModel.getObject();
            }
        };
        DisableAwareDropDownChoice<DomainObject> calculationCenterFilter = new DisableAwareDropDownChoice<DomainObject>("calculationCenterFilter",
                calculationCenterModel, calculationCentresModel, organizationRenderer);
        filterForm.add(calculationCenterFilter);

        final IModel<List<DomainObject>> allUserOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getUserOrganizations(getLocale());
            }
        };
        final IModel<DomainObject> userOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return example.getObject().getUserOrganizationId();
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                example.getObject().setUserOrganizationId(userOrganizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allUserOrganizationsModel.getObject();
            }
        };

        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("userOrganizationFilter",
                userOrganizationModel, allUserOrganizationsModel, organizationRenderer).setNullValid(true));

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                clearExample();
                target.addComponent(content);
            }
        };
        filterForm.add(reset);
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(content);
            }
        };
        filterForm.add(submit);

        DataView<PersonAccount> data = new DataView<PersonAccount>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<PersonAccount> item) {
                PersonAccount personAccount = item.getModelObject();
                item.add(new Label("puAccountNumber", personAccount.getPuAccountNumber()));
                item.add(new Label("firstName", personAccount.getFirstName()));
                item.add(new Label("middleName", personAccount.getMiddleName()));
                item.add(new Label("lastName", personAccount.getLastName()));
                item.add(new Label("city", personAccount.getCity()));
                item.add(new Label("street", AddressRenderer.displayStreet(personAccount.getStreetType(), personAccount.getStreet(), getLocale())));
                item.add(new Label("buildingNumber", personAccount.getBuildingNumber()));
                item.add(new Label("buildingCorp", !Strings.isEmpty(personAccount.getBuildingCorp()) ? personAccount.getBuildingCorp()
                        : ""));
                item.add(new Label("apartment", personAccount.getApartment()));
                item.add(new Label("accountNumber", personAccount.getAccountNumber()));
                item.add(new Label("oszn", personAccount.getOszn()));
                item.add(new Label("calculationCenter", personAccount.getCalculationCenter()));

                //user organization
                item.add(new Label("userOrganization", personAccount.getUserOrganization()));

                item.add(new ScrollBookmarkablePageLink<PersonAccountEdit>("edit", PersonAccountEdit.class,
                        new PageParameters(ImmutableMap.of(PersonAccountEdit.CORRECTION_ID, personAccount.getId())),
                        String.valueOf(personAccount.getId())));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("puAccountNumberHeader", PersonAccountLocalBean.OrderBy.PU_ACCOUNT_NUMBER.getOrderBy(),
                dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", PersonAccountLocalBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", PersonAccountLocalBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", PersonAccountLocalBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", PersonAccountLocalBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", PersonAccountLocalBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingNumberHeader", PersonAccountLocalBean.OrderBy.BUILDING_NUMBER.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("buildingCorpHeader", PersonAccountLocalBean.OrderBy.BUILDING_CORP.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", PersonAccountLocalBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("accountNumberHeader", PersonAccountLocalBean.OrderBy.ACCOUNT_NUMBER.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("osznHeader", PersonAccountLocalBean.OrderBy.OSZN.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("calculationCenterHeader", PersonAccountLocalBean.OrderBy.CALCULATION_CENTER.getOrderBy(),
                dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("userOrganizationHeader", PersonAccountLocalBean.OrderBy.USER_ORGANIZATION.getOrderBy(),
                dataProvider, data, content));

        content.add(new PagingNavigator("navigator", data, getClass().getName(), content));
    }
}
