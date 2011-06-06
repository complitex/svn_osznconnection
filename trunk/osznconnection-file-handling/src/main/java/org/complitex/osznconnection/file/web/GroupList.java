package org.complitex.osznconnection.file.web;

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
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.*;
import org.complitex.dictionary.web.component.datatable.DataProvider;

import static org.complitex.osznconnection.file.service.process.ProcessType.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class GroupList extends ScrollListPage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";
    private final static String IMAGE_AJAX_WAITING = "images/ajax-waiting.gif";
    @EJB
    private RequestFileGroupBean requestFileGroupBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private LogBean logBean;
    private int waitForStopTimer;
    private int timerIndex = 0;
    private Map<ProcessType, Boolean> completedDisplayed = new HashMap<ProcessType, Boolean>();
    private final static String ITEM_GROUP_ID_PREFIX = "item";
    private RequestFileLoadPanel requestFileLoadPanel;
    private WebMarkupContainer buttonContainer;
    private PagingNavigator pagingNavigator;
    private Map<Long, IModel<Boolean>> selectModels;

    public GroupList(PageParameters params) {
        super(params);
        init(params.getAsLong("group_id"));
    }

    public GroupList() {
        super();
        init(null);
    }

    private void init(Long filterGroupId) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileGroupFilter groupFilterObject = (RequestFileGroupFilter) getFilterObject(null);
        if (groupFilterObject == null) {
            groupFilterObject = new RequestFileGroupFilter();
            setFilterObject(groupFilterObject);
        }

        groupFilterObject.setId(filterGroupId);

        final IModel<RequestFileGroupFilter> filterModel = new CompoundPropertyModel<RequestFileGroupFilter>(groupFilterObject);

        //Фильтр форма
        final Form<RequestFileGroupFilter> filterForm = new Form<RequestFileGroupFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();

                RequestFileGroupFilter groupFilterObject = new RequestFileGroupFilter();

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

        //Организация
        IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("organization", osznsModel, renderer));

        //Номер реестра
        filterForm.add(new TextField<String>("registry"));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Директория
        filterForm.add(new TextField<String>("directory"));

        //Имя файла начислений
        filterForm.add(new TextField<String>("paymentName"));

        //Имя файла льгот
        filterForm.add(new TextField<String>("benefitName"));

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
                }));

        //Модель выбранных элементов списка
        selectModels = new HashMap<Long, IModel<Boolean>>();

        //Модель данных списка
        final DataProvider<RequestFileGroup> dataProvider = new DataProvider<RequestFileGroup>() {

            @Override
            protected Iterable<? extends RequestFileGroup> getData(int first, int count) {
                RequestFileGroupFilter groupFilter = filterModel.getObject();

                //save preferences to session
                setFilterObject(groupFilter);
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                //prepare groupFilter object
                groupFilter.setFirst(first);
                groupFilter.setCount(count);
                groupFilter.setSortProperty(getSort().getProperty());
                groupFilter.setAscending(getSort().isAscending());

                List<RequestFileGroup> requestFileGroups = requestFileGroupBean.getRequestFileGroups(groupFilter);

                for (RequestFileGroup group : requestFileGroups) {
                    if (selectModels.get(group.getId()) == null) {
                        selectModels.put(group.getId(), new Model<Boolean>(false));
                    }
                }

                return requestFileGroups;
            }

            @Override
            protected int getSize() {
                return requestFileGroupBean.getRequestFileGroupsCount(filterModel.getObject());
            }
        };
        dataProvider.setSort(getSortProperty("id"), getSortOrder(false));

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_groups_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFileGroup> dataView = new DataView<RequestFileGroup>("request_files_groups", dataProvider, 1) {

            @Override
            protected void populateItem(final Item<RequestFileGroup> item) {
                Long groupId = item.getModelObject().getId();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_GROUP_ID_PREFIX + groupId);

                //Выбор файла
                CheckBox checkBox = new CheckBox("selected", selectModels.get(groupId)) {

                    @Override
                    public boolean isVisible() {
                        RequestFileGroup group = item.getModelObject();
                        return (!group.isProcessing() && !isWaiting(group));
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
                checkBox.setMarkupId("select" + groupId);
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
                item.add(new Label("id", StringUtil.valueOf(groupId)));

                //Дата загрузки
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(item.getModelObject().getLoaded()),
                        DateUtil.isCurrentDay(item.getModelObject().getLoaded()) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                //Организация
                DomainObject domainObject = organizationStrategy.findById(item.getModelObject().getOrganizationId(), true);
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                //Номер реестра (день), месяц, год
                item.add(new Label("registry", StringUtil.valueOf(item.getModelObject().getRegistry())));
                item.add(new Label("month", DateUtil.displayMonth(item.getModelObject().getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(item.getModelObject().getYear())));

                //Директория
                item.add(new Label("directory", item.getModelObject().getDirectory()));

                //Название и ссылка на записи начислений
                item.add(new Label("paymentName", "") {

                    @Override
                    protected void onBeforeRender() {
                        if (item.getModelObject().getPaymentFile().getId() != null) {
                            this.replaceWith(new BookmarkablePageLinkPanel<RequestFile>("paymentName",
                                    item.getModelObject().getPaymentFile().getName(),
                                    ScrollListBehavior.SCROLL_PREFIX + String.valueOf(item.getModelObject().getPaymentFile().getId()),
                                    PaymentList.class,
                                    new PageParameters("request_file_id=" + item.getModelObject().getPaymentFile().getId())));
                        }

                        super.onBeforeRender();
                    }
                });


                //Название и ссылка на записи льгот
                item.add(new Label("benefitName", "") {

                    @Override
                    protected void onBeforeRender() {
                        if (item.getModelObject().getBenefitFile().getId() != null) {
                            this.replaceWith(new BookmarkablePageLinkPanel<RequestFile>("benefitName",
                                    item.getModelObject().getBenefitFile().getName(),
                                    ScrollListBehavior.SCROLL_PREFIX + String.valueOf(item.getModelObject().getBenefitFile().getId()),
                                    BenefitList.class,
                                    new PageParameters("request_file_id=" + item.getModelObject().getBenefitFile().getId())));
                        }

                        super.onBeforeRender();
                    }
                });

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
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.registry", "registry", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.directory", "directory", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.paymentName", "paymentName", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.benefitName", "benefitName", dataProvider, dataView, filterForm));
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

        //Контейнер кнопок для ajax
        buttonContainer = new WebMarkupContainer("buttons");
        buttonContainer.setOutputMarkupId(true);
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
                completedDisplayed.put(BIND_GROUP, false);

                processManagerBean.bindGroup(getSelected());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Обработать
        buttonContainer.add(new Button("process") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(FILL_GROUP, false);

                processManagerBean.fillGroup(getSelected());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Выгрузить
        buttonContainer.add(new Button("save") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(SAVE_GROUP, false);

                processManagerBean.saveGroup(getSelected());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Удалить
        buttonContainer.add(new Button("delete") {

            @Override
            public void onSubmit() {
                for (Long id : getSelected()) {
                    RequestFileGroup group = requestFileGroupBean.getRequestFileGroup(id);

                    if (group != null) {
                        try {
                            requestFileGroupBean.delete(group);

                            info(getStringFormat("group.deleted", group.getFullName()));

                            logBean.info(Module.NAME, GroupList.class, RequestFileGroup.class, null, group.getId(),
                                    Log.EVENT.REMOVE, group.getLogChangeList(), "Файлы удалены успешно. Имя объекта: {0}",
                                    group.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("group.delete_error", group.getFullName()));

                            logBean.error(Module.NAME, GroupList.class, RequestFileGroup.class, null, group.getId(),
                                    Log.EVENT.REMOVE, group.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    group.getLogObjectName());
                            break;
                        }
                    }
                }

                clearSelect();
            }
        });

        //Отменить загрузку
        buttonContainer.add(new Button("load_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(LOAD_GROUP);

                info(getStringOrKey("load_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(LOAD_GROUP);
            }
        });

        //Отменить связывание
        buttonContainer.add(new Button("bind_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(BIND_GROUP);

                info(getStringOrKey("bind_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(BIND_GROUP);
            }
        });

        //Отменить связывание
        buttonContainer.add(new Button("fill_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(FILL_GROUP);

                info(getStringOrKey("fill_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(FILL_GROUP);
            }
        });

        //Отменить связывание
        buttonContainer.add(new Button("save_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(SAVE_GROUP);

                info(getStringOrKey("save_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(SAVE_GROUP);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                getString("load_panel_title"),
                new RequestFileLoadPanel.ILoader() {

                    @Override
                    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed.put(LOAD_GROUP, false);
                        processManagerBean.loadGroup(organizationId, districtCode, monthFrom, monthTo, year);
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
        return processManagerBean.isGlobalProcessing(LOAD_GROUP)
                || processManagerBean.isGlobalProcessing(BIND_GROUP)
                || processManagerBean.isGlobalProcessing(FILL_GROUP)
                || processManagerBean.isGlobalProcessing(SAVE_GROUP);
    }

    private boolean isWaiting(RequestFileGroup group) {
        return processManagerBean.isGlobalWaiting(LOAD_GROUP, group)
                || processManagerBean.isGlobalWaiting(BIND_GROUP, group)
                || processManagerBean.isGlobalWaiting(FILL_GROUP, group)
                || processManagerBean.isGlobalWaiting(SAVE_GROUP, group);
    }

    private void showMessages() {
        showMessages(null);
    }

    private void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        List<RequestFileGroup> loadList = processManagerBean.getProcessed(processType, GroupList.class);

        for (RequestFileGroup group : loadList) {
            if (group.getStatus().equals(RequestFileStatus.SKIPPED)) {
                highlightProcessed(target, group);
                info(getStringFormat(keyPrefix + ".skipped", group.getFullName()));
            } else if (group.getStatus().equals(processedStatus)) {
                highlightProcessed(target, group);
                info(getStringFormat(keyPrefix + ".processed", group.getFullName()));
            } else if (group.getStatus().equals(errorStatus)) {
                highlightError(target, group);

                String message = group.getErrorMessage() != null ?  ": " + group.getErrorMessage() : "";
                error(getStringFormat(keyPrefix + ".error", group.getFullName()) + message);
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
        addMessages("load_process", target, LOAD_GROUP, RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
        addMessages("bind_process", target, BIND_GROUP, RequestFileStatus.BOUND, RequestFileStatus.BIND_ERROR);
        addMessages("fill_process", target, FILL_GROUP, RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
        addMessages("save_process", target, SAVE_GROUP, RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

        for (RequestFile rf : processManagerBean.getLinkError(LOAD_GROUP, true)) {
            error(getStringFormat("request_file.link_error", rf.getFullName()));
        }

        addCompetedMessages("load_process", LOAD_GROUP);
        addCompetedMessages("bind_process", BIND_GROUP);
        addCompetedMessages("fill_process", FILL_GROUP);
        addCompetedMessages("save_process", SAVE_GROUP);
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFileGroup group) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + group.getId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFileGroup group) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + group.getId() + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
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

    @Override
    protected List<ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList((ToolbarButton) new LoadButton(id) {

            @Override
            protected void onClick() {
                requestFileLoadPanel.open();
            }
        });
    }
}
