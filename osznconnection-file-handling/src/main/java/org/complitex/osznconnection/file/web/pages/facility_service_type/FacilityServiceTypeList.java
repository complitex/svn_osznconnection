/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility_service_type;

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
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.FacilityServiceTypeExample;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.FacilityServiceTypeBean.OrderBy;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.FacilityServiceTypeExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.FacilityServiceTypeStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.FacilityServiceTypeFileList;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.complitex.osznconnection.file.entity.FacilityServiceTypeDBF.CDUL;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class FacilityServiceTypeList extends TemplatePage {
    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;

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

    private IModel<FacilityServiceTypeExample> example;
    private long fileId;

    public FacilityServiceTypeList(PageParameters params) {
        this.fileId = params.get("request_file_id").toLong();
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private FacilityServiceTypeExample newExample() {
        FacilityServiceTypeExample e = new FacilityServiceTypeExample();
        e.setRequestFileId(fileId);

        return e;
    }

    private void init() {
        final RequestFile facilityServiceTypeFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!osznSessionBean.isAuthorized(facilityServiceTypeFile.getOrganizationId(),
                facilityServiceTypeFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String label = getStringFormat("label", facilityServiceTypeFile.getDirectory(), File.separator,
                facilityServiceTypeFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form filterForm = new Form("filterForm");
        content.add(filterForm);
        example = new Model<>(newExample());

        StatusDetailPanel<FacilityServiceTypeExample> statusDetailPanel =
                new StatusDetailPanel<FacilityServiceTypeExample>("statusDetailsPanel", example,
                new FacilityServiceTypeExampleConfigurator(), new FacilityServiceTypeStatusDetailRenderer(), content) {

                    @Override
                    public List<StatusDetailInfo> loadStatusDetails() {
                        return statusDetailBean.getFacilityServiceTypeStatusDetails(fileId);
                    }
                };
        add(statusDetailPanel);

        final DataProvider<FacilityServiceType> dataProvider = new DataProvider<FacilityServiceType>() {

            @Override
            protected Iterable<? extends FacilityServiceType> getData(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return facilityServiceTypeBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return facilityServiceTypeBean.count(example.getObject());
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<>("accountFilter", new PropertyModel<String>(example, "account")));
        filterForm.add(new TextField<>("idCodeFilter", new PropertyModel<String>(example, "idCode")));
        filterForm.add(new TextField<>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<>("streetReferenceFilter", new PropertyModel<String>(example, "streetReference")));
        filterForm.add(new TextField<>("streetCodeFilter", new PropertyModel<String>(example, "streetCode")));
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
        final AddressCorrectionPanel<FacilityServiceType> addressCorrectionPanel =
                new AddressCorrectionPanel<FacilityServiceType>("addressCorrectionPanel",
                facilityServiceTypeFile.getUserOrganizationId(), content, statusDetailPanel) {

                    @Override
                    protected void correctAddress(FacilityServiceType facilityServiceType, CORRECTED_ENTITY entity,
                            Long cityId, Long streetTypeId, Long streetId, Long buildingId, long userOrganizationId)
                            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
                        addressService.correctLocalAddress(facilityServiceType, entity, cityId, streetTypeId, streetId, buildingId, userOrganizationId);
                    }

                    @Override
                    protected void closeDialog(AjaxRequestTarget target) {
                        super.closeDialog(target);
                        dataRowHoverBehavior.deactivateDataRow(target);
                    }
                };
        add(addressCorrectionPanel);

        //Панель поиска
        final FacilityServiceTypeLookupPanel lookupPanel =
                new FacilityServiceTypeLookupPanel("lookupPanel", facilityServiceTypeFile.getUserOrganizationId(),
                content, statusDetailPanel) {

                    @Override
                    protected void closeDialog(AjaxRequestTarget target) {
                        super.closeDialog(target);
                        dataRowHoverBehavior.deactivateDataRow(target);
                    }
                };
        add(lookupPanel);

        DataView<FacilityServiceType> data = new DataView<FacilityServiceType>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<FacilityServiceType> item) {
                final FacilityServiceType facilityServiceType = item.getModelObject();

                item.add(new Label("account", facilityServiceType.getStringField(FacilityServiceTypeDBF.RAH)));
                item.add(new Label("idCode", facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE)));
                item.add(new Label("firstName", facilityServiceType.getFirstName()));
                item.add(new Label("middleName", facilityServiceType.getMiddleName()));
                item.add(new Label("lastName", facilityServiceType.getLastName()));
                item.add(new Label("streetCode", facilityServiceType.getStringField(FacilityServiceTypeDBF.CDUL)));
                item.add(new Label("streetReference", facilityServiceType.getStreetReference()));
                item.add(new Label("building", facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE)));
                item.add(new Label("corp", facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD)));
                item.add(new Label("apartment", facilityServiceType.getStringField(FacilityServiceTypeDBF.APT)));
                item.add(new Label("status", statusRenderService.displayStatus(facilityServiceType.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(facilityServiceType.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        String street = facilityServiceType.getStreet() != null
                                ? facilityServiceType.getStreet()
                                : facilityServiceType.getStreetReference() != null
                                ? facilityServiceType.getStreetReference()
                                : getString("streetCodePrefix") + " " + facilityServiceType.getStringField(CDUL);

                        String streetType = facilityServiceType.getStreetType() != null
                                ? facilityServiceType.getStreetType()
                                : facilityServiceType.getStreetTypeReference() != null
                                ? facilityServiceType.getStreetTypeReference()
                                : getString("streetTypeNotFound");


                        addressCorrectionPanel.open(target, facilityServiceType, facilityServiceType.getFirstName(),
                                facilityServiceType.getMiddleName(), facilityServiceType.getLastName(),
                                facilityServiceType.getCity(), streetType, street,
                                facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE),
                                facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD),
                                facilityServiceType.getStringField(FacilityServiceTypeDBF.APT),
                                facilityServiceType.getInternalCityId(),
                                facilityServiceType.getInternalStreetTypeId(),
                                facilityServiceType.getInternalStreetId(),
                                facilityServiceType.getInternalBuildingId());
                    }
                };
                addressCorrectionLink.setVisible(facilityServiceType.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, facilityServiceType, facilityServiceType.getInternalCityId(),
                                facilityServiceType.getInternalStreetId(), facilityServiceType.getInternalBuildingId(),
                                facilityServiceType.getStringField(FacilityServiceTypeDBF.APT),
                                facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE),
                                facilityServiceType.getStatus().isImmediatelySearchByAddress());
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountHeader", OrderBy.RAH.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("idCodeHeader", OrderBy.IDCODE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetCodeHeader", OrderBy.STREET_CODE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetReferenceHeader", OrderBy.STREET_REFERENCE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.set(FacilityServiceTypeFileList.SCROLL_PARAMETER, fileId);
                setResponsePage(FacilityServiceTypeFileList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
