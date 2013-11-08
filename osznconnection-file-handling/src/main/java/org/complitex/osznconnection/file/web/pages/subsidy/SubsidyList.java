/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.subsidy;

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
import org.complitex.osznconnection.file.entity.example.SubsidyExample;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.status.details.SubsidyExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.SubsidyStatusDetailRenderer;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.SubsidyFileList;
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
public final class SubsidyList extends TemplatePage {
    public static final String FILE_ID = "request_file_id";

    @EJB
    private SubsidyBean subsidyBean;

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

    private IModel<SubsidyExample> example;
    private long fileId;

    public SubsidyList(PageParameters params) {
        this.fileId = params.get(FILE_ID).toLong();
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private SubsidyExample newExample() {
        final SubsidyExample e = new SubsidyExample();
        e.setRequestFileId(fileId);
        return e;
    }

    private void init() {
        final RequestFile subsidyFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!sessionBean.isAuthorized(subsidyFile.getOrganizationId(), subsidyFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String label = getStringFormat("label", subsidyFile.getDirectory(), File.separator, subsidyFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);
        example = new Model<SubsidyExample>(newExample());

        StatusDetailPanel<SubsidyExample> statusDetailPanel = new StatusDetailPanel<SubsidyExample>("statusDetailsPanel", example,
                new SubsidyExampleConfigurator(), new SubsidyStatusDetailRenderer(), content) {

            @Override
            public List<StatusDetailInfo> loadStatusDetails() {
                return statusDetailBean.getSubsidyStatusDetails(fileId);
            }
        };
        add(statusDetailPanel);

        final DataProvider<Subsidy> dataProvider = new DataProvider<Subsidy>() {

            @Override
            protected Iterable<? extends Subsidy> getData(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return subsidyBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return subsidyBean.count(example.getObject());
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<String>("rashFilter", new PropertyModel<String>(example, "rash")));
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
        final AddressCorrectionPanel<Subsidy> addressCorrectionPanel = new AddressCorrectionPanel<Subsidy>("addressCorrectionPanel",
                subsidyFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(Subsidy subsidy, AddressEntity entity, Long cityId, Long streetTypeId, Long streetId,
                    Long buildingId, Long userOrganizationId)
                    throws DuplicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                addressService.correctLocalAddress(subsidy, entity, cityId, streetTypeId, streetId, buildingId, userOrganizationId);
                subsidyBean.markCorrected(subsidy, entity);
            }

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(addressCorrectionPanel);

        //Панель поиска
        final SubsidyLookupPanel lookupPanel = new SubsidyLookupPanel("lookupPanel", subsidyFile.getUserOrganizationId(),
                content, statusDetailPanel) {

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(lookupPanel);

        DataView<Subsidy> data = new DataView<Subsidy>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Subsidy> item) {
                final Subsidy subsidy = item.getModelObject();

                item.add(new Label("rash", subsidy.getStringField(SubsidyDBF.RASH)));
                item.add(new Label("firstName", subsidy.getFirstName()));
                item.add(new Label("middleName", subsidy.getMiddleName()));
                item.add(new Label("lastName", subsidy.getLastName()));
                item.add(new Label("city", subsidy.getStringField(SubsidyDBF.NP_NAME)));
                item.add(new Label("street", AddressRenderer.displayStreet(subsidy.getStringField(SubsidyDBF.CAT_V),
                        subsidy.getStringField(SubsidyDBF.NAME_V), getLocale())));
                item.add(new Label("building", subsidy.getStringField(SubsidyDBF.BLD)));
                item.add(new Label("corp", subsidy.getStringField(SubsidyDBF.CORP)));
                item.add(new Label("apartment", subsidy.getStringField(SubsidyDBF.FLAT)));
                item.add(new Label("status", statusRenderService.displayStatus(subsidy.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(subsidy.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, subsidy, subsidy.getFirstName(),
                                subsidy.getMiddleName(), subsidy.getLastName(),
                                subsidy.getStringField(SubsidyDBF.NP_NAME), subsidy.getStringField(SubsidyDBF.CAT_V),
                                subsidy.getStringField(SubsidyDBF.NAME_V), subsidy.getStringField(SubsidyDBF.BLD),
                                subsidy.getStringField(SubsidyDBF.CORP), subsidy.getStringField(SubsidyDBF.FLAT),
                                subsidy.getInternalCityId(), subsidy.getInternalStreetTypeId(), subsidy.getInternalStreetId(),
                                subsidy.getInternalBuildingId());
                    }
                };
                addressCorrectionLink.setVisible(subsidy.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, subsidy, subsidy.getInternalCityId(), subsidy.getInternalStreetId(),
                                subsidy.getInternalBuildingId(), subsidy.getStringField(SubsidyDBF.FLAT),
                                subsidy.getStringField(SubsidyDBF.RASH),
                                subsidy.getStatus().isImmediatelySearchByAddress());
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("rashHeader", SubsidyBean.OrderBy.RASH.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", SubsidyBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", SubsidyBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", SubsidyBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", SubsidyBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", SubsidyBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", SubsidyBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", SubsidyBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", SubsidyBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", SubsidyBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.set(SubsidyFileList.SCROLL_PARAMETER, fileId);
                setResponsePage(SubsidyFileList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
