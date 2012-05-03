/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.complitex.template.web.template.TemplateSession;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
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
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.*;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.IPagingNavigatorListener;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.ToolbarButton;

import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.service.AbstractFilter;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public abstract class AbstractProcessableListPanel<M extends IExecutorObject, F extends AbstractFilter> extends Panel {

    protected abstract class Column implements Serializable {

        public abstract Component head(ISortStateLocator stateLocator, DataView<?> dataView, Component refresh);

        public abstract Component filter();

        public abstract Component field(Item<M> item);
    }
    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";
    private final static String IMAGE_AJAX_WAITING = "images/ajax-waiting.gif";
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private OsznSessionBean osznSessionBean;
    private int waitForStopTimer;
    private int timerIndex = 0;
    private Map<ProcessType, Boolean> completedDisplayed = new EnumMap<ProcessType, Boolean>(ProcessType.class);
    private final static String ITEM_ID_PREFIX = "item";
    private RequestFileLoadPanel requestFileLoadPanel;
    private WebMarkupContainer buttonContainer;
    private WebMarkupContainer optionContainer;
    private Map<Long, IModel<Boolean>> selectModels;
    private boolean modificationsAllowed;
    private boolean hasFieldDescription;
    private Form<F> filterForm;
    private DataView<M> dataView;
    private DataProvider<M> dataProvider;
    private boolean isPostBack;
    private final List<Column> columns = new ArrayList<Column>();

    public AbstractProcessableListPanel(String id) {
        super(id);
        init();
    }

    protected abstract boolean hasFieldDescription();

    protected abstract ProcessType getLoadProcessType();

    protected abstract ProcessType getBindProcessType();

    protected abstract ProcessType getFillProcessType();

    protected abstract ProcessType getSaveProcessType();

    protected abstract void bind(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters);

    protected abstract void fill(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters);

    protected abstract void save(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters);

    protected abstract void load(long userOrganizationId, long osznId, String districtCode,
            int monthFrom, int monthTo, int year);

    @SuppressWarnings("unchecked")
    protected Class<M> getModelClass() {
        return (Class<M>) (findParameterizedSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    protected Class<F> getFilterClass() {
        return (Class<F>) (findParameterizedSuperclass()).getActualTypeArguments()[1];
    }

    private ParameterizedType findParameterizedSuperclass() {
        Type t = getClass().getGenericSuperclass();
        while (!(t instanceof ParameterizedType)) {
            t = ((Class) t).getGenericSuperclass();
        }
        return (ParameterizedType) t;
    }

    protected abstract String getPreferencePage();

    protected void initFilter(F filter) {
    }

    protected F newFilter() {
        try {
            F filter = getFilterClass().newInstance();
            initFilter(filter);
            return filter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract int getSize(F filter);

    protected abstract List<M> getObjects(F filter);

    protected abstract boolean isProcessing(M object);

    protected abstract RequestFileStatus getStatus(M object);

    protected abstract String getFullName(M object);

    protected abstract Date getLoaded(M object);

    protected abstract long getOsznId(M object);

    protected abstract long getUserOrganizationId(M object);

    protected abstract int getMonth(M object);

    protected abstract int getYear(M object);

    protected abstract int getLoadedRecordCount(M object);

    protected abstract int getBindedRecordCount(M object);

    protected abstract int getFilledRecordCount(M object);

    protected abstract M getById(long id);

    protected abstract void delete(M object);

    protected void logSuccessfulDeletion(M object) {
    }

    protected void logFailDeletion(M object, Exception e) {
    }

    protected final void addColumn(Column column) {
        columns.add(column);
    }

    @Override
    protected void onBeforeRender() {
        if (!isPostBack) {
            isPostBack = true;

            //Дополнительные колонки

            //дополнительные фильтры
            for (Column column : columns) {
                filterForm.add(column.filter());
            }

            //дополнительные заголовки
            for (Column column : columns) {
                filterForm.add(column.head(dataProvider, dataView, filterForm));
            }

            //Отобразить сообщения
            showMessages();

            //Если описания структуры для файлов запросов не загружены в базу, сообщить об этом пользователю.
            if (!hasFieldDescription) {
                error(getString("file_description_missing"));
            }
        }

        super.onBeforeRender();
    }

    private void init() {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));
        add(JavascriptPackageResource.getHeaderContribution(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".js"));

        this.hasFieldDescription = hasFieldDescription();
        this.modificationsAllowed =
                //- только пользователи, принадлежащие организации или администраторы могут обрабатывать файлы.
                (osznSessionBean.getCurrentUserOrganizationId() != null || osznSessionBean.isAdmin())
                && //можно обрабатывать файлы, только если в базу загружены описания структур для файлов запросов.
                hasFieldDescription;

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Preference page
        final String preferencePage = getPreferencePage();

        //Фильтр модель
        F filter = (F) getSession().getPreferenceObject(preferencePage, PreferenceKey.FILTER_OBJECT, newFilter());
        final IModel<F> filterModel = new CompoundPropertyModel<F>(filter);

        //Фильтр форма
        filterForm = new Form<F>("filter_form", filterModel);
        add(filterForm);

        Link<Void> filter_reset = new Link<Void>("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();
                F filterObject = newFilter();
                filterModel.setObject(filterObject);
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

        //Статус
        filterForm.add(new DropDownChoice<RequestFileStatus>("status",
                Arrays.asList(RequestFileStatus.values()),
                new IChoiceRenderer<RequestFileStatus>() {

                    @Override
                    public Object getDisplayValue(RequestFileStatus status) {
                        return getString(status.name());
                    }

                    @Override
                    public String getIdValue(RequestFileStatus object, int index) {
                        return object.name();
                    }
                }).setNullValid(true));




        //Модель выбранных элементов списка
        selectModels = new HashMap<Long, IModel<Boolean>>();

        //Модель данных списка
        dataProvider = new DataProvider<M>() {

            @Override
            protected Iterable<M> getData(int first, int count) {
                final F filter = filterModel.getObject();

                //store preference, but before clear data order related properties.
                {
                    filter.setAscending(false);
                    filter.setSortProperty(null);
                    getSession().putPreferenceObject(preferencePage, PreferenceKey.FILTER_OBJECT, filter);
                }

                //prepare filter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<M> objects = getObjects(filter);

                for (M object : objects) {
                    if (selectModels.get(object.getId()) == null) {
                        selectModels.put(object.getId(), new Model<Boolean>(false));
                    }
                }

                return objects;
            }

            @Override
            protected int getSize() {
                return AbstractProcessableListPanel.this.getSize(filterModel.getObject());
            }
        };
        dataProvider.setSort("loaded", false);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("objects_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        dataView = new DataView<M>("objects", dataProvider, 1) {

            @Override
            protected void populateItem(final Item<M> item) {
                final Long objectId = item.getModelObject().getId();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_ID_PREFIX + objectId);

                //Выбор файлов
                CheckBox checkBox = new CheckBox("selected", selectModels.get(objectId)) {

                    @Override
                    public boolean isVisible() {
                        return (!isProcessing(item.getModelObject()) && !isGlobalWaiting(item.getModelObject()));
                    }

                    @Override
                    public boolean isEnabled() {
                        return !isGlobalWaiting(item.getModelObject());
                    }
                };

                checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update form component model
                    }
                });
                checkBox.setMarkupId("select" + objectId);
                checkBox.setOutputMarkupPlaceholderTag(true);
                item.add(checkBox);

                //Анимация в обработке
                item.add(new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER)) {

                    @Override
                    public boolean isVisible() {
                        return isProcessing(item.getModelObject());
                    }
                });

                //Анимация ожидание
                Image waiting = new Image("waiting", new ResourceReference(IMAGE_AJAX_WAITING)) {

                    @Override
                    public boolean isVisible() {
                        return isGlobalWaiting(item.getModelObject()) && !isProcessing(item.getModelObject());
                    }
                };
                item.add(waiting);

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(objectId)));

                //Дата загрузки
                final Date loaded = getLoaded(item.getModelObject());
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(loaded),
                        DateUtil.isCurrentDay(loaded) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                //ОСЗН
                DomainObject domainObject = organizationStrategy.findById(getOsznId(item.getModelObject()), true);
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                //Организация пользователя
                final Long userOrganizationId = getUserOrganizationId(item.getModelObject());
                String userOrganization = null;
                if (userOrganizationId != null) {
                    DomainObject userOrganizationObject = organizationStrategy.findById(userOrganizationId, true);
                    userOrganization = organizationStrategy.displayDomainObject(userOrganizationObject, getLocale());
                }
                item.add(new Label("userOrganization", userOrganization));

                item.add(new Label("month", DateUtil.displayMonth(getMonth(item.getModelObject()), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(getYear(item.getModelObject()))));

                //Количество загруженных записей
                item.add(new Label("loaded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(getLoadedRecordCount(item.getModelObject()));
                    }
                }));

                //Количество связанных записей
                item.add(new Label("binded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(getBindedRecordCount(item.getModelObject()));
                    }
                }));

                //Количество обработанных записей
                item.add(new Label("filled_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(getFilledRecordCount(item.getModelObject()));
                    }
                }));

                //Статус
                item.add(new Label("status", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        String dots = "";
                        if (isProcessing(item.getModelObject()) && isGlobalProcessing()) {
                            dots += StringUtil.getDots(timerIndex % 5);
                        }

                        final RequestFileStatus status = getStatus(item.getModelObject());
                        return (status != null ? getString(status.name()) : "") + dots;
                    }
                }));

                //Допольнительные поля
                for (Column column : columns) {
                    item.add(column.field(item));
                }
            }
        };
        dataViewContainer.add(dataView);

        //Reuse Strategy
        dataView.setItemReuseStrategy(new ReuseIfLongIdEqualStrategy());

        //Постраничная навигация
        PagingNavigator pagingNavigator = new PagingNavigator("paging", dataView, preferencePage, filterForm);
        pagingNavigator.addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                clearSelect();
            }
        });
        filterForm.add(pagingNavigator);

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

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
                completedDisplayed.put(getBindProcessType(), false);

                bind(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Обработать
        buttonContainer.add(new Button("process") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(getFillProcessType(), false);

                fill(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Выгрузить
        buttonContainer.add(new Button("save") {

            @Override
            public void onSubmit() {
                completedDisplayed.put(getSaveProcessType(), false);

                save(getSelected(), buildCommandParameters());

                clearSelect();
                addTimer(dataViewContainer, filterForm, messages);
            }
        });

        //Удалить
        buttonContainer.add(new Button("delete") {

            @Override
            public void onSubmit() {
                for (long objectId : getSelected()) {
                    final M object = getById(objectId);

                    if (object != null) {
                        final String objectName = getFullName(object);
                        try {
                            delete(object);

                            info(MessageFormat.format(getString("info.deleted"), objectName));
                            logSuccessfulDeletion(object);
                        } catch (Exception e) {
                            error(MessageFormat.format(getString("error.delete"), objectName));
                            logFailDeletion(object, e);
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
                processManagerBean.cancel(getLoadProcessType());
                info(getString("load_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getLoadProcessType());
            }
        });

        //Отменить связывание
        buttonContainer.add(new Button("bind_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(getBindProcessType());
                info(getString("bind_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getBindProcessType());
            }
        });

        //Отменить обработку
        buttonContainer.add(new Button("fill_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(getFillProcessType());
                info(getString("fill_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getFillProcessType());
            }
        });

        //Отменить выгрузку
        buttonContainer.add(new Button("save_cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(getSaveProcessType());
                info(getString("save_process.canceling"));
            }

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getSaveProcessType());
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new RequestFileLoadPanel.ILoader() {

                    @Override
                    public void load(long userOrganizationId, long osznId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed.put(getLoadProcessType(), false);
                        AbstractProcessableListPanel.this.load(userOrganizationId, osznId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                });
        add(requestFileLoadPanel);

        //Запуск таймера
        if (isGlobalProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }
    }

    private Boolean getSessionParameter(Enum<?> key) {
        return getSession().getPreferenceBoolean(TemplateSession.GLOBAL_PAGE, key, false);
    }

    private void putSessionParameter(Enum<?> key, Boolean value) {
        getSession().putPreference(TemplateSession.GLOBAL_PAGE, key, value, false);
    }

    @Override
    public TemplateSession getSession() {
        return (TemplateSession) super.getSession();
    }

    private Map<Enum<?>, Object> buildCommandParameters() {
        Map<Enum<?>, Object> commandParameters = new HashMap<Enum<?>, Object>();
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
        return processManagerBean.isGlobalProcessing(getLoadProcessType())
                || processManagerBean.isGlobalProcessing(getBindProcessType())
                || processManagerBean.isGlobalProcessing(getFillProcessType())
                || processManagerBean.isGlobalProcessing(getSaveProcessType());
    }

    private boolean isGlobalWaiting(M object) {
        return processManagerBean.isGlobalWaiting(getLoadProcessType(), object)
                || processManagerBean.isGlobalWaiting(getBindProcessType(), object)
                || processManagerBean.isGlobalWaiting(getFillProcessType(), object)
                || processManagerBean.isGlobalWaiting(getSaveProcessType(), object);
    }

    private void showMessages() {
        showMessages(null);
    }

    private void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        List<M> loadList = processManagerBean.getProcessed(processType, getClass());

        for (M object : loadList) {
            if (getStatus(object).equals(RequestFileStatus.SKIPPED)) {
                highlightProcessed(target, object.getId());
                info(MessageFormat.format(getString(keyPrefix + ".skipped"), getFullName(object)));
            } else if (getStatus(object).equals(processedStatus)) {
                highlightProcessed(target, object.getId());
                info(MessageFormat.format(getString(keyPrefix + ".processed"), getFullName(object)));
            } else if (getStatus(object).equals(errorStatus)) {
                highlightError(target, object.getId());

                String message = object.getErrorMessage() != null ? ": " + object.getErrorMessage() : "";
                error(MessageFormat.format(getString(keyPrefix + ".error"), getFullName(object)) + message);
            }
        }
    }

    private void addCompetedMessages(String keyPrefix, ProcessType processType) {
        if (completedDisplayed.get(processType) == null || !completedDisplayed.get(processType)) {
            //Process completed
            if (processManagerBean.isCompleted(processType)) {
                info(MessageFormat.format(getString(keyPrefix + ".completed"), processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }

            //Process canceled
            if (processManagerBean.isCanceled(processType)) {
                info(MessageFormat.format(getString(keyPrefix + ".canceled"), processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }

            //Process error
            if (processManagerBean.isCriticalError(processType)) {
                error(MessageFormat.format(getString(keyPrefix + ".critical_error"), processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));

                completedDisplayed.put(processType, true);
            }
        }
    }

    protected void showMessages(AjaxRequestTarget target) {
        addMessages("load_process", target, getLoadProcessType(), RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
        addMessages("bind_process", target, getBindProcessType(), RequestFileStatus.BOUND, RequestFileStatus.BIND_ERROR);
        addMessages("fill_process", target, getFillProcessType(), RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
        addMessages("save_process", target, getSaveProcessType(), RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

        addCompetedMessages("load_process", getLoadProcessType());
        addCompetedMessages("bind_process", getBindProcessType());
        addCompetedMessages("fill_process", getFillProcessType());
        addCompetedMessages("save_process", getSaveProcessType());
    }

    private void highlightProcessed(AjaxRequestTarget target, long objectId) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + objectId + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, long objectId) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + objectId + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        waitForStopTimer = 0;
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(7)) {

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

    public List<ToolbarButton> getToolbarButtons(String id) {
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
