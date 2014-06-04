/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.actualpayment;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.entity.AddressEntity;
import org.complitex.address.util.AddressRenderer;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.ActualPaymentExample;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.ActualPaymentExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.ActualPaymentStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.ActualPaymentFileList;
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
    private SessionBean sessionBean;

    private IModel<ActualPaymentExample> example;
    private long fileId;

    public ActualPaymentList(PageParameters params) {
        this.fileId = params.get(FILE_ID).toLong();
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
        if (!sessionBean.isAuthorized(actualPaymentFile.getOrganizationId(), actualPaymentFile.getUserOrganizationId())) {
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

        final Form filterForm = new Form("filterForm");
        content.add(filterForm);
        example = new Model<>(newExample());

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
            protected Iterable<? extends ActualPayment> getData(long first, long count) {
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
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<>("ownNumFilter", new PropertyModel<String>(example, "ownNum")));
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
        final AddressCorrectionPanel<ActualPayment> addressCorrectionPanel = new AddressCorrectionPanel<ActualPayment>("addressCorrectionPanel",
                actualPaymentFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(ActualPayment actualPayment, AddressEntity addressEntity, Long cityId,
                                          Long streetTypeId, Long streetId, Long buildingId, Long apartmentId, Long roomId,
                                          Long userOrganizationId)
                    throws DuplicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(actualPayment, addressEntity, cityId, streetTypeId, streetId, buildingId,
                        userOrganizationId);

                actualPaymentBean.markCorrected(actualPayment, addressEntity);
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
            protected void onClose(AjaxRequestTarget target) {
                super.onClose(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(lookupPanel);

        DataView<ActualPayment> data = new DataView<ActualPayment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<ActualPayment> item) {
                final ActualPayment actualPayment = item.getModelObject();

                item.add(new Label("ownNum", actualPayment.getStringField(ActualPaymentDBF.OWN_NUM)));
                item.add(new Label("firstName", actualPayment.getStringField(ActualPaymentDBF.F_NAM)));
                item.add(new Label("middleName", actualPayment.getStringField(ActualPaymentDBF.M_NAM)));
                item.add(new Label("lastName", actualPayment.getStringField(ActualPaymentDBF.SUR_NAM)));
                item.add(new Label("city", actualPayment.getStringField(ActualPaymentDBF.N_NAME)));
                item.add(new Label("street", AddressRenderer.displayStreet(actualPayment.getStringField(ActualPaymentDBF.VUL_CAT),
                        actualPayment.getStringField(ActualPaymentDBF.VUL_NAME), getLocale())));
                item.add(new Label("building", actualPayment.getStringField(ActualPaymentDBF.BLD_NUM)));
                item.add(new Label("corp", actualPayment.getStringField(ActualPaymentDBF.CORP_NUM)));
                item.add(new Label("apartment", actualPayment.getStringField(ActualPaymentDBF.FLAT)));
                item.add(new Label("status", statusRenderService.displayStatus(actualPayment.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(actualPayment.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, actualPayment, actualPayment.getStringField(ActualPaymentDBF.F_NAM),
                                actualPayment.getStringField(ActualPaymentDBF.M_NAM), actualPayment.getStringField(ActualPaymentDBF.SUR_NAM),
                                actualPayment.getStringField(ActualPaymentDBF.N_NAME), actualPayment.getStringField(ActualPaymentDBF.VUL_CAT),
                                actualPayment.getStringField(ActualPaymentDBF.VUL_NAME), actualPayment.getStringField(ActualPaymentDBF.BLD_NUM),
                                actualPayment.getStringField(ActualPaymentDBF.CORP_NUM), actualPayment.getStringField(ActualPaymentDBF.FLAT),
                                actualPayment.getInternalCityId(), actualPayment.getInternalStreetTypeId(), actualPayment.getInternalStreetId(),
                                actualPayment.getInternalBuildingId(), null);
                    }
                };
                addressCorrectionLink.setVisible(actualPayment.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, actualPayment, actualPayment.getInternalCityId(), actualPayment.getInternalStreetId(),
                                actualPayment.getInternalBuildingId(), actualPayment.getStringField(ActualPaymentDBF.FLAT),
                                actualPayment.getStringField(ActualPaymentDBF.OWN_NUM),
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
                params.set(ActualPaymentFileList.SCROLL_PARAMETER, fileId);
                setResponsePage(ActualPaymentFileList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
