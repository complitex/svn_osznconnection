/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.web.component.account.AccountNumberCorrectionPanel;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel;
import java.util.List;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.StatusDetailInfo;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;

import javax.ejb.EJB;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.status.details.PaymentBenefitStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.PaymentExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PaymentList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";
    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;
    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;
    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;
    @EJB(name = "WebWarningRenderer")
    private WebWarningRenderer webWarningRenderer;
    @EJB(name = "StatusDetailBean")
    private StatusDetailBean statusDetailBean;
    @EJB(name = "AddressService")
    private AddressService addressService;
    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;
    private IModel<PaymentExample> example;
    private long fileId;

    public PaymentList(PageParameters params) {
        this.fileId = params.getAsLong(FILE_ID);
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private PaymentExample newExample() {
        PaymentExample paymentExample = new PaymentExample();
        paymentExample.setRequestFileId(fileId);
        return paymentExample;
    }

    private void init() {
        RequestFile requestFile = requestFileBean.findById(fileId);

        String label = getStringFormat("label", requestFile.getDirectory(), File.separator, requestFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form filterForm = new Form("filterForm");
        content.add(filterForm);
        example = new Model<PaymentExample>(newExample());

        StatusDetailPanel<PaymentExample> statusDetailPanel = new StatusDetailPanel<PaymentExample>("statusDetailsPanel", example,
                new PaymentExampleConfigurator(), new PaymentBenefitStatusDetailRenderer(), content) {

            @Override
            public List<StatusDetailInfo> loadStatusDetails() {
                return statusDetailBean.getPaymentStatusDetails(fileId);
            }
        };
        add(statusDetailPanel);

        final SortableDataProvider<Payment> dataProvider = new SortableDataProvider<Payment>() {

            @Override
            public Iterator<? extends Payment> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return paymentBean.find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return paymentBean.count(example.getObject());
            }

            @Override
            public IModel<Payment> model(Payment object) {
                return new Model<Payment>(object);
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("accountFilter", new PropertyModel<String>(example, "account")));
        filterForm.add(new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<String>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<String>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<String>("buildingFilter", new PropertyModel<String>(example, "building")));
        filterForm.add(new TextField<String>("corpFilter", new PropertyModel<String>(example, "corp")));
        filterForm.add(new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment")));
        filterForm.add(new DropDownChoice<RequestStatus>("statusFilter", new PropertyModel<RequestStatus>(example, "status"),
                Arrays.asList(RequestStatus.values()), new StatusRenderer()));

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

        //Панель коррекции адреса
        final AddressCorrectionPanel<Payment> addressCorrectionPanel = new AddressCorrectionPanel<Payment>("addressCorrectionPanel",
                content, statusDetailPanel) {

            @Override
            protected void correctAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId)
                    throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(payment, cityId, streetId, streetTypeId, buildingId);
            }
        };
        add(addressCorrectionPanel);

        //Панель поиска
        final PaymentLookupPanel lookupPanel = new PaymentLookupPanel("lookupPanel", content, statusDetailPanel);
        add(lookupPanel);

        //Коррекция личного счета
        final AccountNumberCorrectionPanel<Payment> accountNumberCorrectionPanel =
                new AccountNumberCorrectionPanel<Payment>("accountNumberCorrectionPanel", content, statusDetailPanel) {

                    @Override
                    protected void correctAccountNumber(Payment payment, String accountNumber) {
                        personAccountService.updateAccountNumber(payment, accountNumber);
                    }

                    @Override
                    protected List<AccountDetail> acquireAccountDetailsByAddress(Payment request) throws DBException {
                        return lookupPanel.acquireAccountDetailsByAddress(request);
                    }
                };
        add(accountNumberCorrectionPanel);

        DataView<Payment> data = new DataView<Payment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Payment> item) {
                final Payment payment = item.getModelObject();

                item.add(new Label("account", (String) payment.getField(PaymentDBF.OWN_NUM_SR)));
                item.add(new Label("firstName", (String) payment.getField(PaymentDBF.F_NAM)));
                item.add(new Label("middleName", (String) payment.getField(PaymentDBF.M_NAM)));
                item.add(new Label("lastName", (String) payment.getField(PaymentDBF.SUR_NAM)));
                item.add(new Label("city", (String) payment.getField(PaymentDBF.N_NAME)));
                item.add(new Label("street", (String) payment.getField(PaymentDBF.VUL_NAME)));
                item.add(new Label("building", (String) payment.getField(PaymentDBF.BLD_NUM)));
                item.add(new Label("corp", (String) payment.getField(PaymentDBF.CORP_NUM)));
                item.add(new Label("apartment", (String) payment.getField(PaymentDBF.FLAT)));
                item.add(new Label("status", statusRenderService.displayStatus(payment.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(payment.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, payment, (String) payment.getField(PaymentDBF.F_NAM),
                                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                                (String) payment.getField(PaymentDBF.N_NAME), null, (String) payment.getField(PaymentDBF.VUL_NAME),
                                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                                (String) payment.getField(PaymentDBF.FLAT), payment.getInternalCityId(),
                                payment.getInternalStreetId(), payment.getInternalBuildingId());
                    }
                };
                addressCorrectionLink.setVisible(payment.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink accountCorrectionLink = new IndicatingAjaxLink("accountCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        accountNumberCorrectionPanel.open(target, payment);
                    }
                };
                accountCorrectionLink.setVisible(payment.getStatus() == RequestStatus.MORE_ONE_ACCOUNTS);
                item.add(accountCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, payment, payment.getInternalCityId(), payment.getInternalStreetId(), payment.getInternalBuildingId(),
                                (String) payment.getField(PaymentDBF.FLAT), (String) payment.getField(PaymentDBF.OWN_NUM_SR));
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountHeader", PaymentBean.OrderBy.ACCOUNT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", PaymentBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", PaymentBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", PaymentBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", PaymentBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", PaymentBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", PaymentBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", PaymentBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", PaymentBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", PaymentBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Button back = new Button("back") {

            @Override
            public void onSubmit() {
                PageParameters params = new PageParameters();
                params.put(GroupList.SCROLL_PARAMETER, fileId);
                setResponsePage(GroupList.class, params);
            }
        };
        back.setDefaultFormProcessing(false);
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getClass().getName() + fileId, content));
    }
}
