package org.complitex.osznconnection.file.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.*;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.resources.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

import static org.complitex.osznconnection.file.service.process.ProcessType.LOAD_TARIF;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class TarifFileList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";
    @EJB
    private RequestFileBean requestFileBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private LogBean logBean;
    @EJB
    private OsznSessionBean osznSessionBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    private int waitForStopTimer;
    private boolean completedDisplayed = false;
    private final static String ITEM_ID_PREFIX = "item";
    private RequestFileLoadPanel requestFileLoadPanel;
    private boolean modificationsAllowed;
    private boolean hasFieldDescription;

    public TarifFileList(PageParameters parameters) {
        super();
        init(parameters.getAsLong("request_file_id"));
    }

    public TarifFileList() {
        super();
        init(null);
    }

    private boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(RequestFile.TYPE.TARIF) != null;
    }

    private RequestFileFilter newFilter(Long requestFileId) {
        final RequestFileFilter filter = new RequestFileFilter();
        filter.setType(RequestFile.TYPE.TARIF);
        filter.setId(requestFileId);
        return filter;
    }

    private void init(Long requestFileId) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        this.hasFieldDescription = hasFieldDescription();
        this.modificationsAllowed =
                //- только пользователи, принадлежащие организации или администраторы могут обрабатывать файлы.
                (osznSessionBean.getCurrentUserOrganizationId() != null || osznSessionBean.isAdmin())
                && //можно обрабатывать файлы, только если в базу загружены описания структур для файлов запросов.
                hasFieldDescription;

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        final RequestFileFilter filter = (RequestFileFilter) getFilterObject(newFilter(requestFileId));
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filter);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();

                RequestFileFilter filterObject = newFilter(null);
                filterModel.setObject(filterObject);
            }
        };
        filterForm.add(filter_reset);

        //Id
        filterForm.add(new TextField<String>("id"));

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

        //Имя
        filterForm.add(new TextField<String>("name"));

        //Организация
        final IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("organization", osznsModel, organizationRenderer).setNullValid(true));

        // Организация пользователя
        final IModel<List<? extends DomainObject>> userOrganizationsModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                return organizationStrategy.getUserOrganizations(getLocale());
            }
        };
        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("userOrganization", userOrganizationsModel,
                organizationRenderer).setNullValid(true));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month").setNullValid(true));

        //Год
        filterForm.add(new YearDropDownChoice("year").setNullValid(true));

        //Модель выбранных элементов списка
        final Map<RequestFile, IModel<Boolean>> selectModels = new HashMap<RequestFile, IModel<Boolean>>();

        //Модель данных списка
        final DataProvider<RequestFile> dataProvider = new DataProvider<RequestFile>() {

            @Override
            protected Iterable<? extends RequestFile> getData(int first, int count) {
                final RequestFileFilter filter = filterModel.getObject();

                //store preference
                setFilterObject(filter);

                //prepare filter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<RequestFile> requestFiles = requestFileBean.getRequestFiles(filter);

                selectModels.clear();
                for (RequestFile rf : requestFiles) {
                    selectModels.put(rf, new Model<Boolean>(false));
                }

                return requestFiles;
            }

            @Override
            protected int getSize() {
                return requestFileBean.size(filterModel.getObject());
            }
        };
        dataProvider.setSort("loaded", false);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1) {

            @Override
            protected void populateItem(Item<RequestFile> item) {
                RequestFile rf = item.getModelObject();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_ID_PREFIX + rf.getId());

                CheckBox checkBox = new CheckBox("selected", selectModels.get(rf));
                checkBox.setVisible(!isLoading(rf) || !isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(isLoading(rf));
                item.add(processing);

                item.add(new Label("id", StringUtil.valueOf(rf.getId())));

                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("name", rf.getFullName()));

                DomainObject domainObject = organizationStrategy.findById(rf.getOrganizationId(), true);
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                //Организация пользователя
                final Long userOrganizationId = item.getModelObject().getUserOrganizationId();
                String userOrganization = null;
                if (userOrganizationId != null) {
                    DomainObject userOrganizationObject = organizationStrategy.findById(userOrganizationId, true);
                    userOrganization = organizationStrategy.displayDomainObject(userOrganizationObject, getLocale());
                }
                item.add(new Label("userOrganization", userOrganization));

                item.add(new Label("month", DateUtil.displayMonth(rf.getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(rf.getYear())));
                item.add(new Label("dbf_record_count", StringUtil.valueOf(rf.getDbfRecordCount())));
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount(), rf.getDbfRecordCount())));

                String status = "";

                if (isLoaded(rf)) {
                    status = getStringOrKey("status.loaded");
                } else if (isLoading(rf)) {
                    status = getStringOrKey("status.loading");
                } else if (isLoadError(rf)) {
                    status = getStringOrKey("status.load_error");
                }

                item.add(new Label("status", status));

                Class<? extends Page> page = null;
                if (rf.isPayment()) {
                    page = PaymentList.class;
                } else if (rf.isBenefit()) {
                    page = BenefitList.class;
                }

                if (page != null) {
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("action_list", getString("action_list"),
                            page, new PageParameters("request_file_id=" + rf.getId())));
                } else {
                    item.add(new EmptyPanel("action_list"));
                }
            }
        };
        dataViewContainer.add(dataView);

        showMessages();

        if (isProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getPreferencesPage(), filterForm));

        //Удалить
        final Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        try {
                            requestFileBean.delete(requestFile);

                            info(getStringFormat("info.deleted", requestFile.getFullName()));

                            logBean.info(Module.NAME, TarifFileList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("error.delete", requestFile.getFullName()));

                            logBean.error(Module.NAME, TarifFileList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                            break;
                        }
                    }
                }
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        delete.setVisibilityAllowed(modificationsAllowed);
        filterForm.add(delete);

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new RequestFileLoadPanel.ILoader() {

                    @Override
                    public void load(long userOrganizationId, long osznId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed = false;
                        processManagerBean.loadTarif(userOrganizationId, osznId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                }, false);

        add(requestFileLoadPanel);

        //Если описания структуры для файлов запросов не загружены в базу, сообщить об этом пользователю.
        if (!hasFieldDescription) {
            error(getString("file_description_missing"));
        }
    }

    private boolean isProcessing() {
        return processManagerBean.isProcessing(LOAD_TARIF);
    }

    private boolean isLoading(RequestFile requestFile) {
        return processManagerBean.isProcessing(LOAD_TARIF)
                && requestFile.getLoadedRecordCount() < requestFile.getDbfRecordCount();
    }

    private boolean isLoaded(RequestFile requestFile) {
        return requestFile.getLoadedRecordCount().equals(requestFile.getDbfRecordCount())
                && requestFile.getDbfRecordCount() != 0;
    }

    private boolean isLoadError(RequestFile requestFile) {
        return !processManagerBean.isProcessing(LOAD_TARIF) && !isLoaded(requestFile);
    }

    private void showMessages() {

        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        List<RequestFile> list = processManagerBean.getProcessed(LOAD_TARIF, TarifFileList.class);

        for (RequestFile rf : list) {

            if (rf.getLoadedRecordCount().equals(rf.getDbfRecordCount()) && rf.getDbfRecordCount() != 0) {
                info(getStringFormat("tarif.loaded", rf.getFullName()));
                highlightProcessed(target, rf);
            } else {
                error(getStringFormat("tarif.load_error", rf.getFullName()));
                highlightError(target, rf);
            }
        }

        //Process completed
        if (processManagerBean.isCompleted(LOAD_TARIF) && !completedDisplayed) {
            info(getStringFormat("process.done", processManagerBean.getSuccessCount(LOAD_TARIF),
                    processManagerBean.getSkippedCount(LOAD_TARIF), processManagerBean.getErrorCount(LOAD_TARIF)));

            completedDisplayed = true;
        }

        //Process error
        if (processManagerBean.isCriticalError(LOAD_TARIF) && !completedDisplayed) {
            error(getStringFormat("process.critical_error", processManagerBean.getSuccessCount(LOAD_TARIF),
                    processManagerBean.getSkippedCount(LOAD_TARIF), processManagerBean.getErrorCount(LOAD_TARIF)));

            completedDisplayed = true;
        }
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFile requestFile) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFile requestFile) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        waitForStopTimer = 0;
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(4)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isProcessing() && ++waitForStopTimer > 2) {
                    this.stop();
                    target.addComponent(filterForm);
                } else {
                    //update feedback messages panel
                    target.addComponent(messages);
                }
            }
        };
    }

    private void addTimer(WebMarkupContainer dataViewContainer, Form<?> filterForm, AjaxFeedbackPanel messages) {
        boolean needCreateNewTimer = true;

        List<AjaxSelfUpdatingTimerBehavior> timers = Lists.newArrayList(Iterables.filter(dataViewContainer.getBehaviors(),
                AjaxSelfUpdatingTimerBehavior.class));
        if (timers != null && !timers.isEmpty()) {
            for (AjaxSelfUpdatingTimerBehavior timer : timers) {
                if (!timer.isStopped()) {
                    needCreateNewTimer = false;
                    break;
                }
            }
        }
        if (needCreateNewTimer) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }
    }

    @Override
    protected List<ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList((ToolbarButton) new LoadButton(id) {

            {
                setVisibilityAllowed(modificationsAllowed);
            }

            @Override
            protected void onClick() {
                requestFileLoadPanel.open();
            }
        });
    }
}
