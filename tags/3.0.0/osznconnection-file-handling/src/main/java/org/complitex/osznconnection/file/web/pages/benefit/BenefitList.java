/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.benefit;

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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.BenefitExample;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.status.details.BenefitExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.PaymentBenefitStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;
import org.complitex.osznconnection.file.web.component.StatusDetailPanel;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class BenefitList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private StatusRenderService statusRenderService;

    @EJB
    private WebWarningRenderer webWarningRenderer;

    @EJB
    private StatusDetailBean statusDetailBean;

    @EJB
    private SessionBean sessionBean;

    private IModel<BenefitExample> example;
    private long fileId;

    public BenefitList(PageParameters params) {
        this.fileId = params.get(FILE_ID).toLong();
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private BenefitExample newExample() {
        BenefitExample benefitExample = new BenefitExample();
        benefitExample.setRequestFileId(fileId);
        return benefitExample;
    }

    private void init() {
        RequestFile requestFile = requestFileBean.findById(fileId);

        //Проверка доступа к данным
        if (!sessionBean.isAuthorized(requestFile.getOrganizationId(), requestFile.getUserOrganizationId())) {
            throw new UnauthorizedInstantiationException(this.getClass());
        }

        final DataRowHoverBehavior dataRowHoverBehavior = new DataRowHoverBehavior();
        add(dataRowHoverBehavior);

        String fileName = requestFile.getName();
        String directory = requestFile.getDirectory();
        IModel<String> labelModel = new StringResourceModel("label", this, null, new Object[]{fileName, directory});
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);
        example = new Model<BenefitExample>(newExample());

        StatusDetailPanel<BenefitExample> statusDetailPanel = new StatusDetailPanel<BenefitExample>("statusDetailsPanel", example,
                new BenefitExampleConfigurator(), new PaymentBenefitStatusDetailRenderer(), content) {

            @Override
            public List<StatusDetailInfo> loadStatusDetails() {
                return statusDetailBean.getBenefitStatusDetails(fileId);
            }
        };
        add(statusDetailPanel);

        final DataProvider<Benefit> dataProvider = new DataProvider<Benefit>() {

            @Override
            protected Iterable<? extends Benefit> getData(long first, long count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return benefitBean.find(example.getObject());
            }

            @Override
            protected int getSize() {
                example.getObject().setAsc(getSort().isAscending());
                return benefitBean.count(example.getObject());
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        filterForm.add(new TextField<String>("accountFilter", new PropertyModel<String>(example, "account")));
        filterForm.add(new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<String>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<String>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<String>("buildingFilter", new PropertyModel<String>(example, "building")));
        filterForm.add(new TextField<String>("corpFilter", new PropertyModel<String>(example, "corp")));
        filterForm.add(new TextField<String>("privFilter", new PropertyModel<String>(example, "privilege")));
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

        final BenefitConnectPanel benefitConnectPanel = new BenefitConnectPanel("benefitConnectPanel",
                content, statusDetailPanel) {

            @Override
            protected void closeDialog(AjaxRequestTarget target) {
                super.closeDialog(target);
                dataRowHoverBehavior.deactivateDataRow(target);
            }
        };
        add(benefitConnectPanel);

        DataView<Benefit> data = new DataView<Benefit>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Benefit> item) {
                final Benefit benefit = item.getModelObject();

                item.add(new Label("account", benefit.getStringField(BenefitDBF.OWN_NUM_SR)));
                item.add(new Label("firstName", benefit.getStringField(BenefitDBF.F_NAM)));
                item.add(new Label("middleName", benefit.getStringField(BenefitDBF.M_NAM)));
                item.add(new Label("lastName", benefit.getStringField(BenefitDBF.SUR_NAM)));
                item.add(new Label("city", benefit.getCity()));
                item.add(new Label("street", benefit.getStreet()));
                item.add(new Label("building", benefit.getBuildingNumber()));
                item.add(new Label("corp", benefit.getBuildingCorp()));
                item.add(new Label("apartment", benefit.getApartment()));
                item.add(new Label("priv", StringUtil.valueOf(benefit.getStringField(BenefitDBF.PRIV_CAT))));
                item.add(new Label("status", statusRenderService.displayStatus(benefit.getStatus(), getLocale())));
                item.add(new Label("statusDetails", webWarningRenderer.display(benefit.getWarnings(), getLocale())));

                item.add(new IndicatingAjaxLink("connect") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        benefitConnectPanel.open(target, benefit);
                    }

                    @Override
                    public boolean isVisible() {
                        return !benefit.hasPriv()
                                && (benefit.getStatus() == RequestStatus.BENEFIT_OWNER_NOT_ASSOCIATED);
                    }
                });
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountHeader", BenefitBean.OrderBy.ACCOUNT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", BenefitBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", BenefitBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", BenefitBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", BenefitBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", BenefitBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", BenefitBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", BenefitBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", BenefitBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("privHeader", BenefitBean.OrderBy.PRIVILEGE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", BenefitBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

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

        content.add(new PagingNavigator("navigator", data, getPreferencesPage(), content));
    }
}
