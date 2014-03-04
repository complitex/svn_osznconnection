package org.complitex.osznconnection.file.web.pages.payment;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.PaymentBenefitStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.PaymentExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.correction.web.component.AddressCorrectionPanel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.io.File;
import java.util.Arrays;
import java.util.List;

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
    private SessionBean osznSessionBean;

    private IModel<PaymentExample> example;
    private long fileId;

    public PaymentList(PageParameters params) {
        this.fileId = params.get(FILE_ID).toLong();
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

        final Form<Void> filterForm = new Form<>("filterForm");
        content.add(filterForm);
        example = new Model<>(newExample());

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
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<>("accountFilter", new PropertyModel<String>(example, "account")));
        filterForm.add(new TextField<>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<>("buildingFilter", new PropertyModel<String>(example, "building")));
        filterForm.add(new TextField<>("corpFilter", new PropertyModel<String>(example, "corp")));
        filterForm.add(new TextField<>("apartmentFilter", new PropertyModel<String>(example, "apartment")));
        filterForm.add(new DropDownChoice<>("statusFilter", new PropertyModel<RequestStatus>(example, "status"),
                Arrays.asList(RequestStatus.values()), new StatusRenderer()).setNullValid(true));

        AjaxLink<Void> reset = new AjaxLink<Void>("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                clearExample();
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

        //Панель коррекции адреса
        final AddressCorrectionPanel<Payment> addressCorrectionPanel = new AddressCorrectionPanel<Payment>("addressCorrectionPanel",
                paymentFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(Payment payment, AddressEntity entity, Long cityId, Long streetTypeId, Long streetId,
                    Long buildingId, Long apartmentId, Long roomId, Long userOrganizationId)
                    throws DuplicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(payment, entity, cityId, streetTypeId, streetId, buildingId, userOrganizationId);
                paymentBean.markCorrected(payment, entity);
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

                item.add(new Label("account", payment.getStringField(PaymentDBF.OWN_NUM_SR)));
                item.add(new Label("firstName", payment.getStringField(PaymentDBF.F_NAM)));
                item.add(new Label("middleName", payment.getStringField(PaymentDBF.M_NAM)));
                item.add(new Label("lastName", payment.getStringField(PaymentDBF.SUR_NAM)));
                item.add(new Label("city", payment.getStringField(PaymentDBF.N_NAME)));
                item.add(new Label("street", payment.getStringField(PaymentDBF.VUL_NAME)));
                item.add(new Label("building", payment.getStringField(PaymentDBF.BLD_NUM)));
                item.add(new Label("corp", payment.getStringField(PaymentDBF.CORP_NUM)));
                item.add(new Label("apartment", payment.getStringField(PaymentDBF.FLAT)));
                item.add(new Label("status", statusRenderService.displayStatus(payment.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(payment.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, payment, payment.getStringField(PaymentDBF.F_NAM),
                                payment.getStringField(PaymentDBF.M_NAM), payment.getStringField(PaymentDBF.SUR_NAM),
                                payment.getStringField(PaymentDBF.N_NAME), payment.getStringField(PaymentDBF.VUL_NAME),
                                payment.getStringField(PaymentDBF.BLD_NUM), payment.getStringField(PaymentDBF.CORP_NUM),
                                payment.getStringField(PaymentDBF.FLAT), payment.getInternalCityId(),
                                payment.getInternalStreetId(), payment.getInternalBuildingId(), null);
                    }
                };
                addressCorrectionLink.setVisible(payment.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, payment, payment.getInternalCityId(), payment.getInternalStreetId(),
                                payment.getInternalBuildingId(), payment.getStringField(PaymentDBF.FLAT),
                                payment.getStringField(PaymentDBF.OWN_NUM_SR),
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
                params.set(GroupList.SCROLL_PARAMETER, fileId);
                setResponsePage(GroupList.class, params);
            }
        };
        back.setDefaultFormProcessing(false);
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
