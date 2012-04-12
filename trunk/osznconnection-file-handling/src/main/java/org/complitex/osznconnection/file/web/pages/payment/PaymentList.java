/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
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
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.PaymentBenefitStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.PaymentExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel.CORRECTED_ENTITY;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PaymentList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";
    @EJB
    private PaymentBean paymentBean;
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private StatusRenderService statusRenderService;
    @EJB
    private WebWarningRenderer webWarningRenderer;
    @EJB
    private StatusDetailBean statusDetailBean;
    @EJB
    private AddressService addressService;
    @EJB
    private OsznSessionBean osznSessionBean;
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
        final RequestFile paymentFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!osznSessionBean.isAuthorized(paymentFile.getOrganizationId(), paymentFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String label = getStringFormat("label", paymentFile.getDirectory(), File.separator, paymentFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
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

        final DataProvider<Payment> dataProvider = new DataProvider<Payment>() {

            @Override
            protected Iterable<? extends Payment> getData(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return paymentBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return paymentBean.count(example.getObject());
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
                Arrays.asList(RequestStatus.values()), new StatusRenderer()).setNullValid(true));

        AjaxLink<Void> reset = new AjaxLink<Void>("reset") {

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
                paymentFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(Payment payment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId, Long streetId,
                    Long buildingId, long userOrganizationId)
                    throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(payment, entity, cityId, streetTypeId, streetId, buildingId, userOrganizationId);
            }

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(addressCorrectionPanel);

        //Панель поиска
        final PaymentLookupPanel lookupPanel = new PaymentLookupPanel("lookupPanel", paymentFile.getUserOrganizationId(),
                content, statusDetailPanel) {

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(lookupPanel);

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
                                (String) payment.getField(PaymentDBF.N_NAME), (String) payment.getField(PaymentDBF.VUL_NAME),
                                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                                (String) payment.getField(PaymentDBF.FLAT), payment.getInternalCityId(),
                                payment.getInternalStreetId(), payment.getInternalBuildingId());
                    }
                };
                addressCorrectionLink.setVisible(payment.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, payment, payment.getInternalCityId(), payment.getInternalStreetId(),
                                payment.getInternalBuildingId(), (String) payment.getField(PaymentDBF.FLAT),
                                payment.getStatus().isImmediatelySearchByAddress());
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

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
