package org.complitex.osznconnection.file.web;

import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.complitex.template.web.template.TemplateSession;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
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
import org.complitex.dictionary.web.component.paging.IPagingNavigatorListener;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollListBehavior;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.complitex.osznconnection.file.web.pages.actualpayment.ActualPaymentList;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.*;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.pages.ScrollListPage;

import static org.complitex.osznconnection.file.service.process.ProcessType.*;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 13.01.11 19:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ActualPaymentFileList extends ScrollListPage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";
    private final static String IMAGE_AJAX_WAITING = "images/ajax-waiting.gif";
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
    private int waitForStopTimer;
    private int timerIndex = 0;
    private Map<ProcessType, Boolean> completedDisplayed = new EnumMap<ProcessType, Boolean>(ProcessType.class);
    private final static String ITEM_ID_PREFIX = "item";
    private RequestFileLoadPanel requestFileLoadPanel;
    private WebMarkupContainer buttonContainer;
    private WebMarkupContainer optionContainer;
    private PagingNavigator pagingNavigator;
    private Map<Long, IModel<Boolean>> selectModels;
    private final boolean modificationsAllowed;

    public ActualPaymentFileList(PageParameters parameters) {
        super(parameters);
        this.modificationsAllowed = osznSessionBean.getCurrentUserOrganizationId() != null || osznSessionBean.isAdmin();
        init(parameters.getAsLong("request_file_id"));
    }

    public ActualPaymentFileList() {
        super();
        this.modificationsAllowed = osznSessionBean.getCurrentUserOrganizationId() != null || osznSessionBean.isAdmin();
        init(null);
    }

    private void init(Long filterRequestFileId) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileFilter filterObject = (RequestFileFilter) getFilterObject(null);
        if (filterObject == null) {
            filterObject = new RequestFileFilter();
            filterObject.setType(RequestFile.TYPE.ACTUAL_PAYMENT);

            setFilterObject(filterObject);
        }

        filterObject.setId(filterRequestFileId);

        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filterObject);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();

                RequestFileFilter groupFilterObject = new RequestFileFilter();
                groupFilterObject.setType(RequestFile.TYPE.ACTUAL_PAYMENT);

                setFilterObject(groupFilterObject);
                filterModel.setObject(groupFilterObject);
            }
        };
        filterForm.add(filter_reset);

        //Select all checkbox
        filterForm.add(new CheckBox("select_all", new Model<Boolean>(false)) {

            @Override
            public boolean isEnabled() {
                return !isGlobalProcessing();
            }

            @Override
            public void updateModel() {
                //skip update model
            }
        });

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

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", Integer.class));

        //Связано записей
        filterForm.add(new TextField<Integer>("bindedRecordCount", Integer.class));

        //Обработано записей
        filterForm.add(new TextField<Integer>("filledRecordCount", Integer.class));

        //Статус
        filterForm.add(new DropDownChoice<RequestFileStatus>("status",
                Arrays.asList(RequestFileStatus.values()),
                new IChoiceRenderer<RequestFileStatus>() {

                    @Override
                    public Object getDisplayValue(RequestFileStatus object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(RequestFileStatus object, int index) {
                        return object.name();
                    }
                }).setNullValid(true));

        //Модель выбранных элементов списка
        selectModels = new HashMap<Long, IModel<Boolean>>();

        //Модель данных списка
        final DataProvider<RequestFile> dataProvider = new DataProvider<RequestFile>() {

            @Override
            protected Iterable<? extends RequestFile> getData(int first, int count) {
                RequestFileFilter filter = filterModel.getObject();

                //save preferences to session
                setFilterObject(filter);
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                //prepare groupFilter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<RequestFile> requestFiles = requestFileBean.getRequestFiles(filter);

                for (RequestFile rf : requestFiles) {
                    if (selectModels.get(rf.getId()) == null) {
                        selectModels.put(rf.getId(), new Model<Boolean>(false));
                    }
                }

                return requestFiles;
            }

            @Override
            protected int getSize() {
                return requestFileBean.size(filterModel.getObject());
            }
        };
        dataProvider.setSort(getSortProperty("id"), getSortOrder(false));

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1) {

            @Override
            protected void populateItem(final Item<RequestFile> item) {
                Long requestFileId = item.getModelObject().getId();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_ID_PREFIX + requestFileId);

                //Выбор файлов
                CheckBox checkBox = new CheckBox("selected", selectModels.get(requestFileId)) {

                    @Override
                    public boolean isVisible() {
                        RequestFile requestFile = item.getModelObject();
                        return (!requestFile.isProcessing() && !isWaiting(requestFile));
                    }

                    @Override
                    public boolean isEnabled() {
                        return !isWaiting(item.getModelObject());
                    }
                };

                checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update form component model
                    }
                });
                checkBox.setMarkupId("select" + requestFileId);
                checkBox.setOutputMarkupPlaceholderTag(true);
                item.add(checkBox);

                //Анимация в обработке
                item.add(new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER)) {

                    @Override
                    public boolean isVisible() {
                        return item.getModelObject().isProcessing();
                    }
                });

                //Анимация ожидание
                Image waiting = new Image("waiting", new ResourceReference(IMAGE_AJAX_WAITING)) {

                    @Override
                    public boolean isVisible() {
                        return isWaiting(item.getModelObject()) && !item.getModelObject().isProcessing();
                    }
                };
                item.add(waiting);

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(requestFileId)));

                //Дата загрузки
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(item.getModelObject().getLoaded()),
                        DateUtil.isCurrentDay(item.getModelObject().getLoaded()) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                //Организация
                DomainObject domainObject = organizationStrategy.findById(item.getModelObject().getOrganizationId(), true);
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

                item.add(new Label("month", DateUtil.displayMonth(item.getModelObject().getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(item.getModelObject().getYear())));

                //Название
                item.add(new BookmarkablePageLinkPanel<RequestFile>("name", item.getModelObject().getFullName(),
                        ScrollListBehavior.SCROLL_PREFIX + String.valueOf(item.getModelObject().getId()), ActualPaymentList.class,
                        new PageParameters("request_file_id=" + item.getModelObject().getId())));

                //Количество загруженных записей
                item.add(new Label("loaded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(item.getModelObject().getLoadedRecordCount());
                    }
                }));

                //Количество связанных записей
                item.add(new Label("binded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(item.getModelObject().getBindedRecordCount());
                    }
                }));

                //Количество обработанных записей
                item.add(new Label("filled_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(item.getModelObject().getFilledRecordCount());
                    }
                }));

                //Статус
                item.add(new Label("status", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        String dots = "";
                        if (item.getModelObject().isProcessing() && isGlobalProcessing()) {
                            dots += StringUtil.getDots(timerIndex % 5);
                        }

                        return getStringOrKey(item.getModelObject().getStatus()) + dots;
                    }
                }));
            }
        };
        dataViewContainer.add(dataView);

        //Reuse Strategy
        dataView.setItemReuseStrategy(new ReuseIfLongIdEqualStrategy());

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.filled_record_count", "filled_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        pagingNavigator = new PagingNavigator("paging", dataView, getClass().getName(), filterForm);
        pagingNavigator.addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                clearSelect();
            }
        });
        filterForm.add(pagingNavigator);

        //Контейнер чекбокса "Переписать л/с ПУ" для ajax
        optionContainer = new WebMarkupContainer("options");
        optionContainer.setOutputMarkupId(true);
        optionContainer.setVisibilityAllowed(modificationsAllowed);
        filterForm.add(optionContainer);

        optionContainer.add(new CheckBox("update_pu_account", new Model<Boolean>(
                getSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT))).add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                putSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT, !getSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT));
            }
        }));

        //Контейнер кнопок для ajax
        buttonContainer = new WebMarkupContainer("buttons");
        buttonContainer.setOutputMarkupId(true);
        buttonContainer.setVisibilityAllowed(modificationsAllowed);
        filterForm.add(buttonContainer);

        //Загрузить
        buttonContainer.add(new Button("load") {

            @Override
            public void onSubmit() {
                requestFileLoadPanel.open();
            }
        });

        //Связать
        buttonContainer.add(new Button("bind") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(BIND_ACTUAL_PAYMENT, false);

                processManagerBean.bindActualPayment(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Обработать
        buttonContainer.add(new Button("process") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(FILL_ACTUAL_PAYMENT, false);

                processManagerBean.fillActualPayment(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Выгрузить
        buttonContainer.add(new Button("save") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(SAVE_ACTUAL_PAYMENT, false);

                processManagerBean.saveActualPayment(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Удалить
        buttonContainer.add(new Button("delete") {

            @Override
            public void onSubmit() {
                for (Long requestFileId : getSelected()) {
                    RequestFile requestFile = requestFileBean.findById(requestFileId);

                    if (requestFile != null) {
                        try {
                            requestFileBean.delete(requestFile);

                            info(getStringFormat("info.deleted", requestFile.getFullName()));

                            logBean.info(Module.NAME, ActualPaymentFileList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("error.delete", requestFile.getFullName()));

                            logBean.error(Module.NAME, ActualPaymentFileList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                            break;
                        }
                    }
                }
            }
        });

        //Отменить загрузку
        buttonContainer.add(new Button("load_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(LOAD_ACTUAL_PAYMENT);

                info(getStringOrKey("load_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(LOAD_ACTUAL_PAYMENT);
            }
        });

        //Отменить связывание
        buttonContainer.add(new Button("bind_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(BIND_ACTUAL_PAYMENT);

                info(getStringOrKey("bind_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(BIND_ACTUAL_PAYMENT);
            }
        });

        //Отменить обработку
        buttonContainer.add(new Button("fill_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(FILL_ACTUAL_PAYMENT);

                info(getStringOrKey("fill_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(FILL_ACTUAL_PAYMENT);
            }
        });

        //Отменить выгрузку
        buttonContainer.add(new Button("save_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(SAVE_ACTUAL_PAYMENT);

                info(getStringOrKey("save_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(SAVE_ACTUAL_PAYMENT);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                getString("load_panel_title"),
                new RequestFileLoadPanel.ILoader() {

                    @Override
                    public void load(long userOrganizationId, long osznId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed.put(LOAD_ACTUAL_PAYMENT, false);
                        processManagerBean.loadActualPayment(userOrganizationId, osznId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                });
        add(requestFileLoadPanel);

        //Отобразить сообщения
        showMessages();

        //Запуск таймера
        if (isGlobalProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }
    }

    private Boolean getSessionParameter(Enum key) {
        return getTemplateSession().getPreferenceBoolean(TemplateSession.GLOBAL_PAGE, key, false);
    }

    private void putSessionParameter(Enum key, Boolean value) {
        getTemplateSession().putPreference(TemplateSession.GLOBAL_PAGE, key, value, false);
    }

    private Map<Enum, Object> buildCommandParameters() {
        Map<Enum, Object> commandParameters = new HashMap<Enum, Object>();
        commandParameters.put(GlobalOptions.UPDATE_PU_ACCOUNT, getSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT));
        return commandParameters;
    }

    private List<Long> getSelected() {
        List<Long> ids = new ArrayList<Long>();

        for (Long id : selectModels.keySet()) {
            if (selectModels.get(id).getObject()) {
                ids.add(id);
            }
        }

        return ids;
    }

    private void clearSelect() {
        for (IModel<Boolean> model : selectModels.values()) {
            model.setObject(false);
        }
    }

    private boolean isGlobalProcessing() {
        return processManagerBean.isGlobalProcessing(LOAD_ACTUAL_PAYMENT)
                || processManagerBean.isGlobalProcessing(BIND_ACTUAL_PAYMENT)
                || processManagerBean.isGlobalProcessing(FILL_ACTUAL_PAYMENT)
                || processManagerBean.isGlobalProcessing(SAVE_ACTUAL_PAYMENT);
    }

    private boolean isWaiting(RequestFile requestFile) {
        return processManagerBean.isGlobalWaiting(LOAD_ACTUAL_PAYMENT, requestFile)
                || processManagerBean.isGlobalWaiting(BIND_ACTUAL_PAYMENT, requestFile)
                || processManagerBean.isGlobalWaiting(FILL_ACTUAL_PAYMENT, requestFile)
                || processManagerBean.isGlobalWaiting(SAVE_ACTUAL_PAYMENT, requestFile);
    }

    private void showMessages() {
        showMessages(null);
    }

    private void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        List<RequestFile> loadList = processManagerBean.getProcessed(processType, ActualPaymentFileList.class);

        for (RequestFile requestFile : loadList) {
            if (requestFile.getStatus().equals(RequestFileStatus.SKIPPED)) {
                highlightProcessed(target, requestFile);
                info(getStringFormat(keyPrefix + ".skipped", requestFile.getFullName()));
            } else if (requestFile.getStatus().equals(processedStatus)) {
                highlightProcessed(target, requestFile);
                info(getStringFormat(keyPrefix + ".processed", requestFile.getFullName()));
            } else if (requestFile.getStatus().equals(errorStatus)) {
                highlightError(target, requestFile);

                String message = requestFile.getErrorMessage() != null ? ": " + requestFile.getErrorMessage() : "";
                error(getStringFormat(keyPrefix + ".error", requestFile.getFullName()) + message);
            }
        }
    }

    private void addCompetedMessages(String keyPrefix, ProcessType processType) {
        if (completedDisplayed.get(processType) == null || !completedDisplayed.get(processType)) {
            //Process completed
            if (processManagerBean.isCompleted(processType)) {
                info(getStringFormat(keyPrefix + ".completed", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }

            //Process canceled
            if (processManagerBean.isCanceled(processType)) {
                info(getStringFormat(keyPrefix + ".canceled", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }

            //Process error
            if (processManagerBean.isCriticalError(processType)) {
                error(getStringFormat(keyPrefix + ".critical_error", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }
        }
    }

    private void showMessages(AjaxRequestTarget target) {
        addMessages("load_process", target, LOAD_ACTUAL_PAYMENT, RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
        addMessages("bind_process", target, BIND_ACTUAL_PAYMENT, RequestFileStatus.BOUND, RequestFileStatus.BIND_ERROR);
        addMessages("fill_process", target, FILL_ACTUAL_PAYMENT, RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
        addMessages("save_process", target, SAVE_ACTUAL_PAYMENT, RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

        addCompetedMessages("load_process", LOAD_ACTUAL_PAYMENT);
        addCompetedMessages("bind_process", BIND_ACTUAL_PAYMENT);
        addCompetedMessages("fill_process", FILL_ACTUAL_PAYMENT);
        addCompetedMessages("save_process", SAVE_ACTUAL_PAYMENT);
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
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isGlobalProcessing() && ++waitForStopTimer > 2) {
                    this.stop();
                    target.addComponent(filterForm);
                } else {
                    //update feedback messages panel
                    target.addComponent(messages);
                    target.addComponent(buttonContainer);
                    target.addComponent(pagingNavigator);
                }

                timerIndex++;
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
