package org.complitex.osznconnection.file.web.pages.dwelling_charact;

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
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.DwellingCharacteristicsExample;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.DwellingCharacteristicsBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service.status.details.DwellingCharacteristicsExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.DwellingCharacteristicsStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.DwellingCharacteristicsFileList;
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

import static org.complitex.dictionary.util.StringUtil.emptyOnNull;
import static org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF.CDUL;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class DwellingCharacteristicsList extends TemplatePage {
    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;

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

    private IModel<DwellingCharacteristicsExample> example;
    private long fileId;

    public DwellingCharacteristicsList(PageParameters params) {
        this.fileId = params.get("request_file_id").toLong();
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private DwellingCharacteristicsExample newExample() {
        final DwellingCharacteristicsExample e = new DwellingCharacteristicsExample();
        e.setRequestFileId(fileId);
        return e;
    }

    private void init() {
        final RequestFile dwellingCharacteristicsFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!sessionBean.isAuthorized(dwellingCharacteristicsFile.getOrganizationId(),
                dwellingCharacteristicsFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String label = getStringFormat("label", dwellingCharacteristicsFile.getDirectory(), File.separator,
                dwellingCharacteristicsFile.getName());

        add(new Label("title", label));
        add(new Label("label", label));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);
        example = new Model<>(newExample());

        StatusDetailPanel<DwellingCharacteristicsExample> statusDetailPanel =
                new StatusDetailPanel<DwellingCharacteristicsExample>("statusDetailsPanel", example,
                new DwellingCharacteristicsExampleConfigurator(), new DwellingCharacteristicsStatusDetailRenderer(), content) {

                    @Override
                    public List<StatusDetailInfo> loadStatusDetails() {
                        return statusDetailBean.getDwellingCharacteristicsStatusDetails(fileId);
                    }
                };
        add(statusDetailPanel);

        final DataProvider<DwellingCharacteristics> dataProvider = new DataProvider<DwellingCharacteristics>() {

            @Override
            protected Iterable<? extends DwellingCharacteristics> getData(long first, long count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return dwellingCharacteristicsBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return dwellingCharacteristicsBean.count(example.getObject());
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<>("idCodeFilter", new PropertyModel<String>(example, "idCode")));
        filterForm.add(new TextField<>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<>("streetReferenceFilter", new PropertyModel<String>(example, "streetReference")));
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
        final AddressCorrectionPanel<DwellingCharacteristics> addressCorrectionPanel =
                new AddressCorrectionPanel<DwellingCharacteristics>("addressCorrectionPanel",
                dwellingCharacteristicsFile.getUserOrganizationId(), content, statusDetailPanel) {

                    @Override
                    protected void correctAddress(DwellingCharacteristics dwellingCharacteristics, AddressEntity entity,
                            Long cityId, Long streetTypeId, Long streetId, Long buildingId, Long apartmentId, Long roomId,
                            Long userOrganizationId) throws DuplicateCorrectionException, MoreOneCorrectionException,
                            NotFoundCorrectionException {

                        addressService.correctLocalAddress(dwellingCharacteristics, entity, cityId, streetTypeId,
                                streetId, buildingId, userOrganizationId);

                        dwellingCharacteristicsBean.markCorrected(dwellingCharacteristics, entity);
                    }

                    @Override
                    protected void closeDialog(AjaxRequestTarget target) {
                        super.closeDialog(target);
                        dataRowHoverBehavior.deactivateDataRow(target);
                    }
                };
        add(addressCorrectionPanel);

        //Панель поиска
        final DwellingCharacteristicsLookupPanel lookupPanel =
                new DwellingCharacteristicsLookupPanel("lookupPanel", dwellingCharacteristicsFile.getUserOrganizationId(),
                content, statusDetailPanel) {

                    @Override
                    protected void onClose(AjaxRequestTarget target) {
                        super.onClose(target);
                        dataRowHoverBehavior.deactivateDataRow(target);
                    }
                };
        add(lookupPanel);

        DataView<DwellingCharacteristics> data = new DataView<DwellingCharacteristics>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<DwellingCharacteristics> item) {
                final DwellingCharacteristics dwellingCharacteristics = item.getModelObject();

                item.add(new Label("idCode", dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE)));
                item.add(new Label("firstName", dwellingCharacteristics.getFirstName()));
                item.add(new Label("middleName", dwellingCharacteristics.getMiddleName()));
                item.add(new Label("lastName", dwellingCharacteristics.getLastName()));
                item.add(new Label("streetReference", emptyOnNull(dwellingCharacteristics.getStreetType()) + " "
                        + emptyOnNull(dwellingCharacteristics.getStreet())));
                item.add(new Label("building", dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE)));
                item.add(new Label("corp", dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD)));
                item.add(new Label("apartment", dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT)));
                item.add(new Label("status", statusRenderService.displayStatus(dwellingCharacteristics.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(dwellingCharacteristics.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new IndicatingAjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        String street = dwellingCharacteristics.getStreet() != null
                                ? dwellingCharacteristics.getStreet()
                                : getString("streetCodePrefix") + " " + dwellingCharacteristics.getStringField(CDUL);

                        String streetType = dwellingCharacteristics.getStreetType() != null
                                ? dwellingCharacteristics.getStreetType()
                                : getString("streetTypeNotFound");

                        addressCorrectionPanel.open(target, dwellingCharacteristics, dwellingCharacteristics.getFirstName(),
                                dwellingCharacteristics.getMiddleName(), dwellingCharacteristics.getLastName(),
                                dwellingCharacteristics.getCity(), streetType, street,
                                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE),
                                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD),
                                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT),
                                dwellingCharacteristics.getInternalCityId(),
                                dwellingCharacteristics.getInternalStreetTypeId(),
                                dwellingCharacteristics.getInternalStreetId(),
                                dwellingCharacteristics.getInternalBuildingId(), null);
                    }
                };
                addressCorrectionLink.setVisible(dwellingCharacteristics.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new IndicatingAjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, dwellingCharacteristics, dwellingCharacteristics.getInternalCityId(),
                                dwellingCharacteristics.getInternalStreetId(), dwellingCharacteristics.getInternalBuildingId(),
                                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT),
                                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE),
                                dwellingCharacteristics.getStatus().isImmediatelySearchByAddress());
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("idCodeHeader", DwellingCharacteristicsBean.OrderBy.IDCODE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", DwellingCharacteristicsBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", DwellingCharacteristicsBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", DwellingCharacteristicsBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetReferenceHeader", DwellingCharacteristicsBean.OrderBy.STREET_REFERENCE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", DwellingCharacteristicsBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", DwellingCharacteristicsBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", DwellingCharacteristicsBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", DwellingCharacteristicsBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.set(DwellingCharacteristicsFileList.SCROLL_PARAMETER, fileId);
                setResponsePage(DwellingCharacteristicsFileList.class, params);
            }
        };
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + fileId, content));
    }
}
