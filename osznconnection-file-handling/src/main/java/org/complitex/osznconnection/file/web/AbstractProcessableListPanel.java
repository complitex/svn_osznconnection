package org.complitex.osznconnection.file.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.service.AbstractFilter;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.component.ajax.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.DataRowHoverBehavior;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.RequestFileHistoryPanel;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.file.web.component.process.*;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.template.TemplateSession;
import org.odlabs.wiquery.ui.effects.HighlightEffectJavaScriptResourceReference;

import javax.ejb.EJB;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

import static org.complitex.osznconnection.file.entity.RequestFileStatus.*;

public abstract class AbstractProcessableListPanel<M extends IExecutorObject, F extends AbstractFilter> extends Panel {

    protected abstract class Column implements Serializable {

        public abstract Component head(ISortStateLocator stateLocator, DataView<?> dataView, Component refresh);

        public abstract Component filter();

        public abstract Component field(Item<M> item);
    }
    private static final int AJAX_TIMER = 1;
    @EJB
    private ProcessManagerBean processManagerBean;
    private RequestFileLoadPanel requestFileLoadPanel;
    private RequestFileHistoryPanel requestFileHistoryPanel;
    private final ModificationManager modificationManager;
    private final ProcessingManager processingManager;
    private final MessagesManager messagesManager;
    private Form<F> form;
    private ProcessDataView<M> dataView;
    private DataProvider<M> dataProvider;
    private final List<Column> columns = new ArrayList<Column>();
    private WebMarkupContainer dataViewContainer;
    private AjaxFeedbackPanel messages;

    private SelectManager selectManager;
    private TimerManager timerManager;

    public AbstractProcessableListPanel(String id) {
        super(id);

        add(new DataRowHoverBehavior());

        this.modificationManager = new ModificationManager(this, hasFieldDescription());

        this.processingManager = new ProcessingManager(getLoadProcessType(), getBindProcessType(),
                getFillProcessType(),getSaveProcessType());

        this.messagesManager = new MessagesManager(this) {

            @Override
            public void showMessages(AjaxRequestTarget target) {
                addMessages("load_process", target, getLoadProcessType(), LOADED, LOAD_ERROR);
                addMessages("bind_process", target, getBindProcessType(), BOUND, BIND_ERROR);
                addMessages("fill_process", target, getFillProcessType(), FILLED, FILL_ERROR);
                addMessages("save_process", target, getSaveProcessType(), SAVED, SAVE_ERROR);
                addMessages("export_process", target, getExportProcessType(), EXPORTED, EXPORT_ERROR);

                addCompetedMessages("load_process", getLoadProcessType());
                addCompetedMessages("bind_process", getBindProcessType());
                addCompetedMessages("fill_process", getFillProcessType());
                addCompetedMessages("save_process", getSaveProcessType());
                addCompetedMessages("export_process", getExportProcessType());

                AbstractProcessableListPanel.this.showMessages(target);
            }
        };

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

    protected abstract void load(long userOrganizationId, long osznId, DateParameter dateParameter);

    protected void export(AjaxRequestTarget target, List<Long> selectedFileIds){
        //override me
    }

    protected boolean isExportVisible(){
        return false;
    }

    protected ProcessType getExportProcessType(){
        return ProcessType.EXPORT_SUBSIDY;
    }

    protected abstract MonthParameterViewMode getLoadMonthParameterViewMode();

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
            filter.setSortProperty("loaded");

            initFilter(filter);

            return filter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract int getSize(F filter);

    protected abstract List<M> getObjects(F filter);

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

    protected abstract RequestFile getRequestFile(M object);

    protected void logSuccessfulDeletion(M object) {
    }

    protected void logFailDeletion(M object, Exception e) {
    }

    protected final void addColumn(Column column) {
        columns.add(column);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        //Дополнительные колонки

        //дополнительные фильтры
        for (Column column : columns) {
            form.add(column.filter());
        }

        //дополнительные заголовки
        for (Column column : columns) {
            form.add(column.head(dataProvider, dataView, form));
        }

        //Отобразить сообщения
        messagesManager.showMessages();
        
        //Отобразить сообщения об отсутствии описания файлов запросов если необходимо
        modificationManager.reportErrorIfNecessary();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        renderResources(response);
    }

    public static void renderResources(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(HighlightEffectJavaScriptResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".js")));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".css")));
    }

    private void init() {
        messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Фильтр модель
        F filter = getSession().getPreferenceObject(getPreferencePage(), PreferenceKey.FILTER_OBJECT, newFilter());
        final IModel<F> model = new CompoundPropertyModel<>(filter);

        //Фильтр форма
        form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        add(form);

        AjaxLink<Void> filter_reset = new AjaxLink<Void>("filter_reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                F filterObject = newFilter();
                model.setObject(filterObject);
                target.add(form);
            }
        };
        form.add(filter_reset);

        AjaxButton find = new AjaxButton("find", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        form.add(find);

        //Select all checkbox
        form.add(new SelectAllCheckBoxPanel("selectAllCheckBoxPanel", processingManager));

        //Id
        form.add(new TextField<String>("id"));

        //Дата загрузки
        form.add(new DatePicker<Date>("loaded"));

        //ОСЗН
        form.add(new OsznFilter("organization"));

        // Организация пользователя
        form.add(new OrganizationPicker("userOrganization", OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));

        //Месяц
        form.add(new MonthDropDownChoice("month").setNullValid(true));

        //Год
        form.add(new YearDropDownChoice("year").setNullValid(true));

        //Статус
        form.add(new RequestFileStatusFilter("status"));

        //Модель выбранных элементов списка.
        selectManager = new SelectManager();

        //Модель данных списка
        dataProvider = new DataProvider<M>() {

            @Override
            protected Iterable<M> getData(long first, long count) {
                final F filter = model.getObject();

                getSession().putPreferenceObject(getPreferencePage(), PreferenceKey.FILTER_OBJECT, filter);

                //prepare filter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<M> objects = getObjects(filter);

                selectManager.initializeSelectModels(objects);

                return objects;
            }

            @Override
            protected int getSize() {
                return AbstractProcessableListPanel.this.getSize(model.getObject());
            }
        };
        dataProvider.setSort(filter.getSortProperty(), SortOrder.DESCENDING);

        //Контейнер для ajax
        dataViewContainer = new WebMarkupContainer("objects_container");
        dataViewContainer.setOutputMarkupId(true);
        form.add(dataViewContainer);

        timerManager = new TimerManager(AJAX_TIMER, messagesManager, processingManager, form, dataViewContainer);
        timerManager.addUpdateComponent(messages);

        //Таблица файлов запросов
        dataView = new ProcessDataView<M>("objects", dataProvider) {

            @Override
            protected void populateItem(final Item<M> item) {
                final Long objectId = item.getModelObject().getId();

                item.add(new ItemCheckBoxPanel<M>("itemCheckBoxPanel", processingManager, selectManager));

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(objectId)));

                //Дата загрузки
                item.add(new ItemDateLoadedLabel("loaded", getLoaded(item.getModelObject())));

                //ОСЗН
                item.add(new ItemOrganizationLabel("organization", getOsznId(item.getModelObject())));

                //Организация пользователя
                item.add(new ItemOrganizationLabel("userOrganization", getUserOrganizationId(item.getModelObject())));

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
                AjaxLink history = new AjaxLink("history") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        requestFileHistoryPanel.open(target, getRequestFile(item.getModelObject()));

                    }
                };
                item.add(history);

                history.add(new ItemStatusLabel("status", processingManager, timerManager));

                //Дополнительные поля
                for (Column column : columns) {
                    item.add(column.field(item));
                }
            }
        };
        dataViewContainer.add(dataView);

        //Постраничная навигация
        dataViewContainer.add(new ProcessPagingNavigator("paging", dataView, getPreferencePage(), selectManager, form));

        //Сортировка
        form.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, form));

        //Контейнер чекбокса "Переписать л/с ПУ" для ajax
        WebMarkupContainer optionContainer = new WebMarkupContainer("options");
        optionContainer.setVisibilityAllowed(modificationManager.isModificationsAllowed());
        form.add(optionContainer);

        optionContainer.add(new CheckBox("update_pu_account", new Model<Boolean>(
                getSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT))).add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                putSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT, !getSessionParameter(GlobalOptions.UPDATE_PU_ACCOUNT));
            }
        }));

        //Контейнер кнопок для ajax
        WebMarkupContainer buttons = new WebMarkupContainer("buttons");
        buttons.setOutputMarkupId(true);
        buttons.setVisibilityAllowed(modificationManager.isModificationsAllowed());
        form.add(buttons);

        timerManager.addUpdateComponent(buttons);

        //Загрузить
        buttons.add(new AjaxLink<Void>("load") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                requestFileLoadPanel.open(target);
            }
        });

        //Связать
        buttons.add(new AjaxLink<Void>("bind") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                bind(selectManager.getSelectedFileIds(), buildCommandParameters());

                startTimer(target, getBindProcessType());
            }
        });

        //Обработать
        buttons.add(new AjaxLink<Void>("process") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                fill(selectManager.getSelectedFileIds(), buildCommandParameters());

                startTimer(target, getFillProcessType());
            }
        });

        //Выгрузить
        buttons.add(new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                save(selectManager.getSelectedFileIds(), buildCommandParameters());

                startTimer(target, getSaveProcessType());
            }
        });

        //Экспортировать
        buttons.add(new AjaxLink<Void>("export") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                export(target, selectManager.getSelectedFileIds());
            }

            @Override
            public boolean isVisible() {
                return isExportVisible();
            }
        });

        //Удалить
        buttons.add(new DeleteButton("delete") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                for (long objectId : selectManager.getSelectedFileIds()) {
                    final M object = getById(objectId);

                    if (object != null) {
                        try {
                            delete(object);

                            selectManager.remove(objectId);

                            info(MessageFormat.format(getString("info.deleted"), object.getObjectName()));
                            logSuccessfulDeletion(object);
                        } catch (Exception e) {
                            error(MessageFormat.format(getString("error.delete"), object.getObjectName()));
                            logFailDeletion(object, e);
                            break;
                        }
                    }
                }
                target.add(form);
                target.add(messages);
            }
        });

        //Отменить загрузку
        buttons.add(new AjaxLink<Void>("load_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getLoadProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getLoadProcessType());
                info(getString("load_process.canceling"));
                target.add(form);
            }
        });

        //Отменить связывание
        buttons.add(new AjaxLink<Void>("bind_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getBindProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getBindProcessType());
                info(getString("bind_process.canceling"));
                target.add(form);
            }
        });

        //Отменить обработку
        buttons.add(new AjaxLink<Void>("fill_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getFillProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getFillProcessType());
                info(getString("fill_process.canceling"));
                target.add(form);
            }
        });

        //Отменить выгрузку
        buttons.add(new AjaxLink<Void>("save_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getSaveProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getSaveProcessType());
                info(getString("save_process.canceling"));
                target.add(form);
            }
        });

        //Отменить экспорт
        buttons.add(new AjaxLink<Void>("export_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getExportProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getExportProcessType());
                info(getString("export_process.canceling"));
                target.add(form);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel", new ResourceModel("load_panel_title"),
                getLoadMonthParameterViewMode()) {
            @Override
            protected void load(Long userOrganizationId, Long osznId, DateParameter dateParameter, AjaxRequestTarget target) {
                AbstractProcessableListPanel.this.load(userOrganizationId, osznId, dateParameter);

                startTimer(target, getLoadProcessType());
            }
        };
        add(requestFileLoadPanel);

        add(requestFileHistoryPanel = new RequestFileHistoryPanel("history_panel"));

        //Запуск таймера
        timerManager.startTimer();
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

    protected void showMessages(AjaxRequestTarget target) {
    }

    public List<? extends ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList(new LoadButton(id) {

            {
                setVisibilityAllowed(modificationManager.isModificationsAllowed());
            }

            @Override
            protected void onClick(AjaxRequestTarget target) {
                requestFileLoadPanel.open(target);
            }
        });
    }

    protected void startTimer(AjaxRequestTarget target, ProcessType processType){
        messagesManager.resetCompletedStatus(processType);

        selectManager.clearSelection();
        timerManager.addTimer();
        target.add(form);

    }

    public WebMarkupContainer getDataViewContainer() {
        return dataViewContainer;
    }

    public AjaxFeedbackPanel getMessages() {
        return messages;
    }
}
