/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.image.StaticImage;
import org.complitex.dictionary.web.component.paging.IPagingNavigatorListener;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.OsznSessionBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.AbstractProcessableListPanel;
import org.complitex.osznconnection.file.web.AbstractProcessableListPanel.SelectModelValue;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.IRequestFileLoader;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public abstract class AbstractReferenceBookFileList extends TemplatePage {

    private static final Logger log = LoggerFactory.getLogger(AbstractReferenceBookFileList.class);
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private LogBean logBean;
    @EJB
    private OsznSessionBean osznSessionBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    private final AtomicInteger statusTimerIndex = new AtomicInteger();
    private boolean completedDisplayed = false;
    private RequestFileLoadPanel requestFileLoadPanel;
    private final boolean modificationsAllowed;
    private final boolean hasFieldDescription;

    protected AbstractReferenceBookFileList() {
        this.hasFieldDescription = hasFieldDescription();
        this.modificationsAllowed =
                //- только пользователи, принадлежащие организации или администраторы могут обрабатывать файлы.
                (osznSessionBean.getCurrentUserOrganizationId(getTemplateSession()) != null || osznSessionBean.isAdmin())
                && //можно обрабатывать файлы, только если в базу загружены описания структур для файлов запросов.
                hasFieldDescription;
        init();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        AbstractProcessableListPanel.renderResources(response);
    }

    protected abstract RequestFile.TYPE getRequestFileType();

    protected abstract void load(long userOrganizationId, long osznId, DateParameter dateParameter);

    protected abstract ProcessType getLoadProcessType();

    private boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(getRequestFileType()) != null;
    }

    protected void init() {
        IModel<String> titleModel = new ResourceModel("title");
        add(new Label("title", titleModel));
        add(new Label("header", titleModel));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Фильтр модель
        final RequestFileFilter filter = (RequestFileFilter) getFilterObject(newFilter());
        final IModel<RequestFileFilter> model = new CompoundPropertyModel<>(filter);

        //Фильтр форма
        final Form<RequestFileFilter> form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        add(form);

        AjaxLink<Void> reset = new AjaxLink<Void>("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                model.setObject(newFilter());
                target.add(form);
            }
        };
        form.add(reset);

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
        CheckBox selectAll = new CheckBox("select_all", new Model<Boolean>(false)) {

            @Override
            public boolean isEnabled() {
                return !isGlobalProcessing();
            }

            @Override
            public void updateModel() {
                //skip update model
            }
        };
        selectAll.add(new CssAttributeBehavior("processable-list-panel-select-all"));
        form.add(selectAll);

        //Id
        form.add(new TextField<String>("id"));

        //Дата загрузки
        form.add(new DatePicker<Date>("loaded"));

        //Имя
        form.add(new TextField<String>("name"));

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
        form.add(new DisableAwareDropDownChoice<DomainObject>("organization", osznsModel, organizationRenderer).setNullValid(true));

        // Организация пользователя
        final IModel<List<? extends DomainObject>> userOrganizationsModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                return organizationStrategy.getUserOrganizations(getLocale());
            }
        };
        form.add(new DisableAwareDropDownChoice<DomainObject>("userOrganization", userOrganizationsModel,
                organizationRenderer).setNullValid(true));

        //Год
        form.add(new YearDropDownChoice("year").setNullValid(true));

        //Статус
        form.add(new DropDownChoice<RequestFileStatus>("status", Arrays.asList(RequestFileStatus.values()),
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

        //Модель выбранных элементов списка.
        final Map<Long, SelectModelValue> selectModels = new HashMap<>();

        //Модель данных списка
        final DataProvider<RequestFile> dataProvider = new DataProvider<RequestFile>() {

            @Override
            protected Iterable<? extends RequestFile> getData(int first, int count) {
                final RequestFileFilter filter = model.getObject();

                //store preference, but before clear data order related properties.
                {
                    filter.setAscending(false);
                    filter.setSortProperty(null);
                    setFilterObject(filter);
                }
                setFilterObject(filter);

                //prepare filter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<RequestFile> requestFiles = requestFileBean.getRequestFiles(filter);

                AbstractProcessableListPanel.initializeSelectModels(selectModels, requestFiles);

                return requestFiles;
            }

            @Override
            protected int getSize() {
                return requestFileBean.size(model.getObject());
            }
        };
        dataProvider.setSort("loaded", SortOrder.DESCENDING);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        form.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1) {

            @Override
            protected void populateItem(final Item<RequestFile> item) {
                final Long objectId = item.getModelObject().getId();

                /* for highlighting to work properly */
                AbstractProcessableListPanel.augmentItem(item, objectId);

                //Выбор файлов
                CheckBox checkBox = new CheckBox("selected",
                        AbstractProcessableListPanel.newSelectFileCheckboxModel(objectId, selectModels)) {

                    @Override
                    public boolean isVisible() {
                        return !isProcessing(item.getModelObject()) && !isGlobalWaiting(item.getModelObject());
                    }

                    @Override
                    public boolean isEnabled() {
                        return !isGlobalWaiting(item.getModelObject());
                    }
                };

                checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                    }
                });
                checkBox.add(new CssAttributeBehavior("processable-list-panel-select"));
                item.add(checkBox);

                //Анимация в обработке
                item.add(new StaticImage("processing", new SharedResourceReference(AbstractProcessableListPanel.IMAGE_AJAX_LOADER)) {

                    @Override
                    public boolean isVisible() {
                        return isProcessing(item.getModelObject());
                    }
                });

                //Анимация ожидание
                item.add(new StaticImage("waiting", new SharedResourceReference(AbstractProcessableListPanel.IMAGE_AJAX_WAITING)) {

                    @Override
                    public boolean isVisible() {
                        return isGlobalWaiting(item.getModelObject()) && !isProcessing(item.getModelObject());
                    }
                });

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(objectId)));

                //Дата загрузки
                final Date loaded = item.getModelObject().getLoaded();
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(loaded),
                        DateUtil.isCurrentDay(loaded) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                item.add(new Label("name", item.getModelObject().getFullName()));

                //ОСЗН
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

                item.add(new Label("year", StringUtil.valueOf(item.getModelObject().getYear())));

                item.add(new Label("dbf_record_count", StringUtil.valueOf(item.getModelObject().getDbfRecordCount())));

                //Количество загруженных записей
                item.add(new Label("loaded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(item.getModelObject().getLoadedRecordCount(),
                                item.getModelObject().getDbfRecordCount());
                    }
                }));

                //Статус
                item.add(new Label("status", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        String dots = "";
                        if (isProcessing(item.getModelObject())) {
                            dots += StringUtil.getDots(statusTimerIndex.get() % 5);
                        }

                        final RequestFileStatus status = item.getModelObject().getStatus();
                        return (status != null ? getString(status.name()) : "") + dots;
                    }
                }));
            }
        };
        dataViewContainer.add(dataView);

        //Reuse Strategy
        dataView.setItemReuseStrategy(new ReuseIfLongIdEqualStrategy());

        //Постраничная навигация
        PagingNavigator pagingNavigator = new PagingNavigator("paging", dataView, getPreferencesPage(), form);
        pagingNavigator.addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                AbstractProcessableListPanel.clearSelection(selectModels);
            }
        });
        form.add(pagingNavigator);

        //Сортировка
        form.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, form));

        //Назад
        form.add(new Link<Void>("back") {

            @Override
            public void onClick() {
                setResponsePage(ReferenceBookList.class);
            }
        });

        WebMarkupContainer buttons = new WebMarkupContainer("buttons");
        buttons.setOutputMarkupId(true);
        buttons.setVisibilityAllowed(modificationsAllowed);
        form.add(buttons);

        //Загрузить
        buttons.add(new AjaxLink<Void>("load") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                requestFileLoadPanel.open(target);
            }
        });

        //Удалить
        buttons.add(new AjaxLink<Void>("delete") {

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new AjaxCallDecorator() {

                    @Override
                    public CharSequence decorateScript(Component c, CharSequence script) {
                        return "if(confirm('" + getString("delete_caution") + "')){" + script + "}";
                    }
                };
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                for (long requestFileId : AbstractProcessableListPanel.getSelectedFileIds(selectModels)) {
                    RequestFile requestFile = requestFileBean.findById(requestFileId);
                    try {
                        requestFileBean.delete(requestFile);

                        selectModels.remove(requestFileId);

                        info(getStringFormat("info.deleted", requestFile.getFullName()));
                        logBean.info(Module.NAME, AbstractReferenceBookFileList.this.getClass(), RequestFile.class,
                                null, requestFile.getId(), Log.EVENT.REMOVE, requestFile.getLogChangeList(),
                                "Файл удален успешно. Имя объекта: {0}", requestFile.getLogObjectName());
                    } catch (Exception e) {
                        error(getStringFormat("error.delete", requestFile.getFullName()));
                        logBean.error(Module.NAME, AbstractReferenceBookFileList.this.getClass(), RequestFile.class,
                                null, requestFile.getId(), Log.EVENT.REMOVE, requestFile.getLogChangeList(),
                                "Ошибка удаления. Имя объекта: {0}", requestFile.getLogObjectName());
                        log.error("Couldn't delete request files of type " + getRequestFileType() + ".", e);
                        break;
                    }
                }
                target.add(form);
                target.add(messages);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new IRequestFileLoader() {

                    @Override
                    public void load(long userOrganizationId, long osznId, DateParameter dateParameter, AjaxRequestTarget target) {
                        completedDisplayed = false;
                        AbstractReferenceBookFileList.this.load(userOrganizationId, osznId, dateParameter);

                        addTimer(dataViewContainer, form, messages);
                        target.add(form);
                    }
                }, MonthParameterViewMode.HIDDEN);

        add(requestFileLoadPanel);

        //Если описания структуры для файлов запросов не загружены в базу, сообщить об этом пользователю.
        if (!hasFieldDescription) {
            error(getString("file_description_missing"));
        }

        //Запуск таймера
        if (isGlobalProcessing()) {
            dataViewContainer.add(newTimer(form, messages));
        }

        showMessages();
    }

    private RequestFileFilter newFilter() {
        final RequestFileFilter filter = new RequestFileFilter();
        filter.setType(getRequestFileType());
        return filter;
    }

    private boolean isProcessing(RequestFile file) {
        return file.isProcessing();
    }

    private boolean isGlobalProcessing() {
        return processManagerBean.isGlobalProcessing(getLoadProcessType());
    }

    private boolean isGlobalWaiting(RequestFile requestFile) {
        return processManagerBean.isGlobalWaiting(getLoadProcessType(), requestFile);
    }

    private void showMessages() {
        showMessages(null);
    }

    private void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        List<RequestFile> loadList = processManagerBean.getProcessed(processType, getClass());

        for (RequestFile file : loadList) {
            if (file.getStatus() == RequestFileStatus.SKIPPED) {
                AbstractProcessableListPanel.highlightProcessed(target, file.getId());
                info(getStringFormat(keyPrefix + ".skipped", file.getFullName()));
            } else if (file.getStatus() == processedStatus) {
                AbstractProcessableListPanel.highlightProcessed(target, file.getId());
                info(getStringFormat(keyPrefix + ".processed", file.getFullName()));
            } else if (file.getStatus() == errorStatus) {
                AbstractProcessableListPanel.highlightError(target, file.getId());
                String message = file.getErrorMessage() != null ? ": " + file.getErrorMessage() : "";
                error(getStringFormat(keyPrefix + ".error", file.getFullName()) + message);
            }
        }
    }

    private void addCompetedMessages(String keyPrefix, ProcessType processType) {
        if (!completedDisplayed) {
            //Process completed
            if (processManagerBean.isCompleted(processType)) {
                info(getStringFormat(keyPrefix + ".completed", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));
                completedDisplayed = true;
            }

            //Process canceled
            if (processManagerBean.isCanceled(processType)) {
                info(getStringFormat(keyPrefix + ".canceled", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));
                completedDisplayed = true;
            }

            //Process error
            if (processManagerBean.isCriticalError(processType)) {
                error(getStringFormat(keyPrefix + ".critical_error", processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType), processManagerBean.getErrorCount(processType)));
                completedDisplayed = true;
            }
        }
    }

    private void showMessages(AjaxRequestTarget target) {
        addMessages("load_process", target, getLoadProcessType(), RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
        addCompetedMessages("load_process", getLoadProcessType());
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        final AtomicInteger waitForStopTimer = new AtomicInteger();
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(2)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isGlobalProcessing() && waitForStopTimer.incrementAndGet() > 2) {
                    this.stop();
                    target.add(filterForm);
                } else {
                    //update feedback messages panel
                    target.add(messages);
                }

                statusTimerIndex.incrementAndGet();
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
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList(new LoadButton(id) {

            {
                setVisibilityAllowed(modificationsAllowed);
            }

            @Override
            protected void onClick(AjaxRequestTarget target) {
                requestFileLoadPanel.open(target);
            }
        });
    }
}
