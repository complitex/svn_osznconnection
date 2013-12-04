package org.complitex.osznconnection.file.web;

import com.google.common.collect.ImmutableSet;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.IHeaderResponse;
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
import org.complitex.dictionary.web.component.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.organization.web.component.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.file.web.component.process.*;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.template.TemplateSession;

import javax.ejb.EJB;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

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
    private static final int AJAX_TIMER = 1;
    @EJB
    private ProcessManagerBean processManagerBean;
    private RequestFileLoadPanel requestFileLoadPanel;
    private final ModificationManager modificationManager;
    private final ProcessingManager<M> processingManager;
    private final MessagesManager<M> messagesManager;
    private Form<F> form;
    private ProcessDataView<M> dataView;
    private DataProvider<M> dataProvider;
    private final List<Column> columns = new ArrayList<Column>();

    public AbstractProcessableListPanel(String id) {
        super(id);
        this.modificationManager = new ModificationManager(this, hasFieldDescription());
        this.processingManager = new ProcessingManager<M>() {

            @Override
            public boolean isProcessing(M object) {
                return AbstractProcessableListPanel.this.isProcessing(object);
            }

            @Override
            protected Set<ProcessType> getSupportedProcessTypes() {
                return ImmutableSet.of(getLoadProcessType(), getBindProcessType(), getFillProcessType(),
                        getSaveProcessType());
            }
        };
        this.messagesManager = new MessagesManager<M>(this) {

            @Override
            protected RequestFileStatus getStatus(M object) {
                return AbstractProcessableListPanel.this.getStatus(object);
            }

            @Override
            protected String getFullName(M object) {
                return AbstractProcessableListPanel.this.getFullName(object);
            }

            @Override
            public void showMessages(AjaxRequestTarget target) {
                addMessages("load_process", target, getLoadProcessType(),
                        RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
                addMessages("bind_process", target, getBindProcessType(),
                        RequestFileStatus.BOUND, RequestFileStatus.BIND_ERROR);
                addMessages("fill_process", target, getFillProcessType(),
                        RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
                addMessages("save_process", target, getSaveProcessType(),
                        RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

                addCompetedMessages("load_process", getLoadProcessType());
                addCompetedMessages("bind_process", getBindProcessType());
                addCompetedMessages("fill_process", getFillProcessType());
                addCompetedMessages("save_process", getSaveProcessType());

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

    protected abstract MonthParameterViewMode getLoadMonthParameterViewMode();

    protected Class<M> getModelClass() {
        return (Class<M>) (findParameterizedSuperclass()).getActualTypeArguments()[0];
    }

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
        response.renderJavaScriptReference(WebCommonResourceInitializer.HIGHLIGHT_JS);
        response.renderJavaScriptReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".js"));
        response.renderCSSReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".css"));
    }

    private void init() {
        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Preference page
        final String preferencePage = getPreferencePage();

        //Фильтр модель
        F filter = (F) getSession().getPreferenceObject(preferencePage, PreferenceKey.FILTER_OBJECT, newFilter());
        final IModel<F> model = new CompoundPropertyModel<F>(filter);

        //Фильтр форма
        form = new Form<F>("form", model);
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
        form.add(new OrganizationPicker("userOrganization", null, OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));

        //Месяц
        form.add(new MonthDropDownChoice("month").setNullValid(true));

        //Год
        form.add(new YearDropDownChoice("year").setNullValid(true));

        //Статус
        form.add(new RequestFileStatusFilter("status"));

        //Модель выбранных элементов списка.
        final SelectManager selectManager = new SelectManager();

        //Модель данных списка
        dataProvider = new DataProvider<M>() {

            @Override
            protected Iterable<M> getData(int first, int count) {
                final F filter = model.getObject();

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

                selectManager.initializeSelectModels(objects);

                return objects;
            }

            @Override
            protected int getSize() {
                return AbstractProcessableListPanel.this.getSize(model.getObject());
            }
        };
        dataProvider.setSort("loaded", SortOrder.DESCENDING);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("objects_container");
        dataViewContainer.setOutputMarkupId(true);
        form.add(dataViewContainer);

        final TimerManager timerManager = new TimerManager(AJAX_TIMER, messagesManager, processingManager, form, dataViewContainer);
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
                item.add(new ItemStatusLabel<M>("status", processingManager, timerManager) {

                    @Override
                    protected RequestFileStatus getStatus(M object) {
                        return AbstractProcessableListPanel.this.getStatus(object);
                    }
                });

                //Дополнительные поля
                for (Column column : columns) {
                    item.add(column.field(item));
                }
            }
        };
        dataViewContainer.add(dataView);

        //Постраничная навигация
        ProcessPagingNavigator pagingNavigator = new ProcessPagingNavigator("paging", dataView, preferencePage,
                selectManager, form);
        form.add(pagingNavigator);

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
                messagesManager.resetCompletedStatus(getBindProcessType());

                bind(selectManager.getSelectedFileIds(), buildCommandParameters());

                selectManager.clearSelection();
                timerManager.addTimer();
                target.add(form);
            }
        });

        //Обработать
        buttons.add(new AjaxLink<Void>("process") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                messagesManager.resetCompletedStatus(getFillProcessType());

                fill(selectManager.getSelectedFileIds(), buildCommandParameters());

                selectManager.clearSelection();
                timerManager.addTimer();
                target.add(form);
            }
        });

        //Выгрузить
        buttons.add(new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                messagesManager.resetCompletedStatus(getSaveProcessType());

                save(selectManager.getSelectedFileIds(), buildCommandParameters());

                selectManager.clearSelection();
                timerManager.addTimer();
                target.add(form);
            }
        });

        //Удалить
        buttons.add(new DeleteButton("delete") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                for (long objectId : selectManager.getSelectedFileIds()) {
                    final M object = getById(objectId);

                    if (object != null) {
                        final String objectName = getFullName(object);
                        try {
                            delete(object);

                            selectManager.remove(objectId);

                            info(MessageFormat.format(getString("info.deleted"), objectName));
                            logSuccessfulDeletion(object);
                        } catch (Exception e) {
                            error(MessageFormat.format(getString("error.delete"), objectName));
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

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new RequestFileLoader(messagesManager, timerManager, getLoadProcessType(), form) {

                    @Override
                    public void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
                        AbstractProcessableListPanel.this.load(userOrganizationId, osznId, dateParameter);
                    }
                }, getLoadMonthParameterViewMode());
        add(requestFileLoadPanel);

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
}
