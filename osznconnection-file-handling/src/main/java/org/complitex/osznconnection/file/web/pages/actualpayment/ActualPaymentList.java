/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.actualpayment;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.ActualPaymentExample;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.ActualPaymentExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.ActualPaymentStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.ActualPaymentFileList;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel.CORRECTED_ENTITY;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;
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
public final class ActualPaymentList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";
    @EJB
    private ActualPaymentBean actualPaymentBean;
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
        final RequestFile actualPaymentFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!osznSessionBean.isAuthorized(actualPaymentFile.getOrganizationId(), actualPaymentFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String label = getStringFormat("label", actualPaymentFile.getDirectory(), File.separator, actualPaymentFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);
        example = new Model<ActualPaymentExample>(newExample());

        StatusDetailPanel<ActualPaymentExample> statusDetailPanel = new StatusDetailPanel<ActualPaymentExample>("statusDetailsPanel", example,
                new ActualPaymentExampleConfigurator(), new ActualPaymentStatusDetailRenderer(), content) {

            @Override
            public List<StatusDetailInfo> loadStatusDetails() {
                return statusDetailBean.getActualPaymentStatusDetails(fileId);
            }
        };
        add(statusDetailPanel);

        final DataProvider<ActualPayment> dataProvider = new DataProvider<ActualPayment>() {

            @Override
            protected Iterable<? extends ActualPayment> getData(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return actualPaymentBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return actualPaymentBean.count(example.getObject());
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("ownNumFilter", new PropertyModel<String>(example, "ownNum")));
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
        final AddressCorrectionPanel<ActualPayment> addressCorrectionPanel = new AddressCorrectionPanel<ActualPayment>("addressCorrectionPanel",
                actualPaymentFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(ActualPayment actualPayment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId, Long streetId,
                    Long buildingId, long userOrganizationId)
                    throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(actualPayment, entity, cityId, streetTypeId, streetId, buildingId, userOrganizationId);
            }

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(addressCorrectionPanel);

        //Панель поиска
        final ActualPaymentLookupPanel lookupPanel = new ActualPaymentLookupPanel("lookupPanel", actualPaymentFile.getUserOrganizationId(),
                content, statusDetailPanel) {

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(lookupPanel);

        DataView<ActualPayment> data = new DataView<ActualPayment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<ActualPayment> item) {
                final ActualPayment actualPayment = item.getModelObject();

                item.add(new Label("ownNum", (String) actualPayment.getField(ActualPaymentDBF.OWN_NUM)));
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

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, actualPayment, actualPayment.getInternalCityId(), actualPayment.getInternalStreetId(),
                                actualPayment.getInternalBuildingId(), (String) actualPayment.getField(ActualPaymentDBF.FLAT),
                                (String) actualPayment.getField(ActualPaymentDBF.OWN_NUM),
                                actualPayment.getStatus().isImmediatelySearchByAddress());
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("ownNumHeader", ActualPaymentBean.OrderBy.OWN_NUM.getOrderBy(), dataProvider, data, content));
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
                PageParameters params = new PageParameters();
                params.put(ActualPaymentFileList.SCROLL_PARAMETER, fileId);
                setResponsePage(ActualPaymentFileList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
