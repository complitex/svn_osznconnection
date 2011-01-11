/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.actualpayment;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.web.component.StatusRenderer;

import javax.ejb.EJB;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.link.Link;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.example.ActualPaymentExample;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.pages.payment.AddressCorrectionPanel;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class ActualPaymentList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";
    @EJB(name = "ActualPaymentBean")
    private ActualPaymentBean actualPaymentBean;
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
    private IModel<ActualPaymentExample> example;
    private long fileId;

    public ActualPaymentList(PageParameters params) {
        this.fileId = params.getAsLong(FILE_ID);
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private ActualPaymentExample newExample() {
        ActualPaymentExample actualPaymentExample = new ActualPaymentExample();
        actualPaymentExample.setRequestFileId(fileId);
        return actualPaymentExample;
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
        example = new Model<ActualPaymentExample>(newExample());

//        StatusDetailPanel<ActualPaymentExample> statusDetailPanel = new StatusDetailPanel<ActualPaymentExample>("statusDetailsPanel",
//                ActualPaymentExample.class, example, new PaymentExampleConfigurator(), content) {
//
//            @Override
//            public List<StatusDetailInfo> loadStatusDetails() {
//                return statusDetailBean.getPaymentStatusDetails(fileId);
//            }
//        };
//        add(statusDetailPanel);

        final SortableDataProvider<ActualPayment> dataProvider = new SortableDataProvider<ActualPayment>() {

            @Override
            public Iterator<? extends ActualPayment> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return actualPaymentBean.find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return actualPaymentBean.count(example.getObject());
            }

            @Override
            public IModel<ActualPayment> model(ActualPayment object) {
                return new Model<ActualPayment>(object);
            }
        };
        dataProvider.setSort("", true);

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
        final AddressCorrectionPanel addressCorrectionPanel = new AddressCorrectionPanel("addressCorrectionPanel", content) {

            @Override
            protected void correctAddress(AbstractRequest request, Long cityId, Long streetId, Long streetTypeId, Long buildingId)
                    throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                ActualPayment actualPayment = ActualPayment.class.cast(request);
                addressService.correctLocalAddress(actualPayment, cityId, streetId, streetTypeId, buildingId);
            }
        };
        add(addressCorrectionPanel);
//
//        //Панель поиска
//        final PaymentLookupPanel lookupPanel = new PaymentLookupPanel("lookupPanel", content, statusDetailPanel);
//        add(lookupPanel);
//
//        //Коррекция личного счета
//        final PaymentAccountNumberCorrectionPanel paymentAccountNumberCorrectionPanel =
//                new PaymentAccountNumberCorrectionPanel("paymentAccountNumberCorrectionPanel", content, statusDetailPanel);
//        add(paymentAccountNumberCorrectionPanel);

        DataView<ActualPayment> data = new DataView<ActualPayment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<ActualPayment> item) {
                final ActualPayment actualPayment = item.getModelObject();

                item.add(new Label("firstName", (String) actualPayment.getField(ActualPaymentDBF.F_NAM)));
                item.add(new Label("middleName", (String) actualPayment.getField(ActualPaymentDBF.M_NAM)));
                item.add(new Label("lastName", (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM)));
                item.add(new Label("city", (String) actualPayment.getField(ActualPaymentDBF.N_NAME)));
                item.add(new Label("street", AddressRenderer.displayStreet((String) actualPayment.getField(ActualPaymentDBF.VUL_CAT),
                        (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME), getLocale())));
                item.add(new Label("building", (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM)));
                item.add(new Label("corp", (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM)));
                item.add(new Label("apartment", (String) actualPayment.getField(ActualPaymentDBF.FLAT)));
                item.add(new Label("status", statusRenderService.displayStatus(actualPayment.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(actualPayment.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, actualPayment, (String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                                (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                                (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT),
                                (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME), (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM),
                                (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM), (String) actualPayment.getField(ActualPaymentDBF.FLAT),
                                actualPayment.getInternalCityId(), actualPayment.getInternalStreetTypeId(), actualPayment.getInternalStreetId(),
                                actualPayment.getInternalBuildingId());
                    }
                };
                addressCorrectionLink.setVisible(actualPayment.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);
//
//                AjaxLink accountCorrectionLink = new IndicatingAjaxLink("accountCorrectionLink") {
//
//                    @Override
//                    public void onClick(AjaxRequestTarget target) {
//                        paymentAccountNumberCorrectionPanel.open(target, actualPayment);
//                    }
//                };
//                accountCorrectionLink.setVisible(actualPayment.getStatus() == RequestStatus.MORE_ONE_ACCOUNTS);
//                item.add(accountCorrectionLink);
//
//                AjaxLink lookup = new IndicatingAjaxLink("lookup") {
//
//                    @Override
//                    public void onClick(AjaxRequestTarget target) {
//                        lookupPanel.open(target, actualPayment);
//                    }
//                };
//                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("firstNameHeader", ActualPaymentBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", ActualPaymentBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", ActualPaymentBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", ActualPaymentBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", ActualPaymentBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", ActualPaymentBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", ActualPaymentBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", ActualPaymentBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", ActualPaymentBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
//                PageParameters params = new PageParameters();
//                params.put(GroupList.SCROLL_PARAMETER, fileId);
//                setResponsePage(GroupList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getClass().getName() + fileId, content));
    }
}

