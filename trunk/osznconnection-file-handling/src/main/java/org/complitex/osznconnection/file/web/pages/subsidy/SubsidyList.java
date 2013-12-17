package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.entity.AddressEntity;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.correction.web.component.AddressCorrectionPanel;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.web.component.AjaxCancelEventBubbleCallDecorator;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.SubsidyExample;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.status.details.SubsidyExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.SubsidyStatusDetailRenderer;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.SubsidyFileList;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.complitex.dictionary.util.StringUtil.decimal;

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

        content.add(new FeedbackPanel("messages"));

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);
        example = new Model<>(newExample());

        StatusDetailPanel<SubsidyExample> statusDetailPanel = new StatusDetailPanel<SubsidyExample>("statusDetailsPanel", example,
                new SubsidyExampleConfigurator(), new SubsidyStatusDetailRenderer(), content) {

            @Override
            public List<StatusDetailInfo> loadStatusDetails() {
                return statusDetailBean.getSubsidyStatusDetails(fileId);
            }
        };
        content.add(statusDetailPanel);

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

        filterForm.add(new TextField<>("rashFilter", new PropertyModel<String>(example, "rash")));
        filterForm.add(new TextField<>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<>("buildingFilter", new PropertyModel<String>(example, "building")));
        filterForm.add(new TextField<>("corpFilter", new PropertyModel<String>(example, "corp")));
        filterForm.add(new TextField<>("apartmentFilter", new PropertyModel<String>(example, "apartment")));

        filterForm.add(new TextField<>("DAT1", new PropertyModel<Date>(example, "DAT1")));
        filterForm.add(new TextField<>("DAT2", new PropertyModel<Date>(example, "DAT2")));
        filterForm.add(new TextField<>("NUMM", new PropertyModel<Integer>(example, "NUMM")));
        filterForm.add(new TextField<>("NM_PAY", new PropertyModel<BigDecimal>(example, "NM_PAY")));
        filterForm.add(new TextField<>("SUMMA", new PropertyModel<BigDecimal>(example, "SUMMA")));
        filterForm.add(new TextField<>("SUBS", new PropertyModel<BigDecimal>(example, "SUBS")));

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
        final AddressCorrectionPanel<Subsidy> addressCorrectionPanel = new AddressCorrectionPanel<Subsidy>("addressCorrectionPanel",
                subsidyFile.getUserOrganizationId(), content, statusDetailPanel) {

            @Override
            protected void correctAddress(Subsidy subsidy, AddressEntity entity, Long cityId, Long streetTypeId, Long streetId,
                    Long buildingId, Long apartmentId, Long userOrganizationId)
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

        final  SubsidyEditPanel editPanel = new SubsidyEditPanel("edit_panel", content);
        add(editPanel);

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
                item.add(new Label("DAT1", subsidy.getStringField(SubsidyDBF.DAT1)));
                item.add(new Label("DAT2", subsidy.getStringField(SubsidyDBF.DAT2)));
                item.add(new Label("NUMM", subsidy.getStringField(SubsidyDBF.NUMM)));
                item.add(new Label("NM_PAY", decimal(subsidy.getStringField(SubsidyDBF.NM_PAY))));
                item.add(new Label("SUMMA", decimal(subsidy.getStringField(SubsidyDBF.SUMMA))));
                item.add(new Label("SUBS", decimal(subsidy.getStringField(SubsidyDBF.SUBS))));

                item.add(new Label("status", statusRenderService.displayStatus(subsidy.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(subsidy.getWarnings(), getLocale())));

                AjaxLink addressCorrectionLink = new AjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target, subsidy, subsidy.getFirstName(),
                                subsidy.getMiddleName(), subsidy.getLastName(),
                                subsidy.getStringField(SubsidyDBF.NP_NAME), subsidy.getStringField(SubsidyDBF.CAT_V),
                                subsidy.getStringField(SubsidyDBF.NAME_V), subsidy.getStringField(SubsidyDBF.BLD),
                                subsidy.getStringField(SubsidyDBF.CORP), subsidy.getStringField(SubsidyDBF.FLAT),
                                subsidy.getInternalCityId(), subsidy.getInternalStreetTypeId(), subsidy.getInternalStreetId(),
                                subsidy.getInternalBuildingId(), null);
                    }

                    @Override
                    protected IAjaxCallDecorator getAjaxCallDecorator() {
                        return new AjaxCancelEventBubbleCallDecorator();
                    }
                };
                addressCorrectionLink.setVisible(subsidy.getStatus().isAddressCorrectable());
                item.add(addressCorrectionLink);

                AjaxLink lookup = new AjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, subsidy, subsidy.getInternalCityId(), subsidy.getInternalStreetId(),
                                subsidy.getInternalBuildingId(), subsidy.getStringField(SubsidyDBF.FLAT),
                                subsidy.getStringField(SubsidyDBF.RASH),
                                subsidy.getStatus().isImmediatelySearchByAddress());
                    }

                    @Override
                    protected IAjaxCallDecorator getAjaxCallDecorator() {
                        return new AjaxCancelEventBubbleCallDecorator();
                    }
                };
                item.add(lookup);

                item.add(new AjaxEventBehavior("onclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        editPanel.open(target, subsidy);
                    }
                });
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

        filterForm.add(new ArrowOrderByBorder("DAT1_header", SubsidyBean.OrderBy.DAT1.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("DAT2_header", SubsidyBean.OrderBy.DAT2.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("NUMM_header", SubsidyBean.OrderBy.NUMM.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("NM_PAY_header", SubsidyBean.OrderBy.NM_PAY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("SUMMA_header", SubsidyBean.OrderBy.SUMMA.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("SUBS_header", SubsidyBean.OrderBy.SUBS.getOrderBy(), dataProvider, data, content));

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
