/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumMap;
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
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
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
import org.complitex.osznconnection.file.web.AbstractProcessableListPanel.SelectModelValue;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class FacilityForm2FileList extends ScrollListPage {

    private static final Logger log = LoggerFactory.getLogger(FacilityForm2FileList.class);
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private OsznSessionBean osznSessionBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LogBean logBean;
    private final AtomicInteger statusTimerIndex = new AtomicInteger();
    private Map<ProcessType, Boolean> completedDisplayed = new EnumMap<ProcessType, Boolean>(ProcessType.class);
    private WebMarkupContainer buttonContainer;
    private Map<Long, SelectModelValue> selectModels;
    private final boolean modificationsAllowed;
    private final boolean hasFieldDescription;
    private Form<RequestFileFilter> filterForm;
    private DataView<RequestFile> dataView;
    private DataProvider<RequestFile> dataProvider;

    public FacilityForm2FileList() {
        this.hasFieldDescription = hasFieldDescription();
        this.modificationsAllowed =
                //- только пользователи, принадлежащие организации или администраторы могут обрабатывать файлы.
                (osznSessionBean.getCurrentUserOrganizationId(getSession()) != null || osznSessionBean.isAdmin())
                && //можно обрабатывать файлы, только если в базу загружены описания структур для файлов запросов.
                hasFieldDescription;
        init();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        AbstractProcessableListPanel.renderResources(response);
    }

    private boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(RequestFile.TYPE.FACILITY_FORM2) != null;
    }

    private RequestFileFilter newFilter() {
        RequestFileFilter filter = new RequestFileFilter();
        filter.setType(RequestFile.TYPE.FACILITY_FORM2);
        return filter;
    }

    private String getPreferencePage() {
        return FacilityForm2FileList.class.getName();
    }

    @Override
    public TemplateSession getSession() {
        return (TemplateSession) super.getSession();
    }

    private void init() {
        add(new Label("title", new ResourceModel("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Preference page
        final String preferencePage = getPreferencePage();

        //Фильтр модель
        RequestFileFilter filter = (RequestFileFilter) getSession().getPreferenceObject(preferencePage,
                PreferenceKey.FILTER_OBJECT, newFilter());
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filter);

        //Фильтр форма
        filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        AjaxLink<Void> filter_reset = new AjaxLink<Void>("filter_reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                RequestFileFilter filterObject = newFilter();
                filterModel.setObject(filterObject);
                target.add(filterForm);
            }
        };
        filterForm.add(filter_reset);

        AjaxButton find = new AjaxButton("find", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(filterForm);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(find);

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
        filterForm.add(selectAll);

        //Id
        filterForm.add(new TextField<String>("id"));

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
        filterForm.add(new DropDownChoice<RequestFileStatus>("status", Arrays.asList(RequestFileStatus.values()),
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
        selectModels = new HashMap<>();

        //Модель данных списка
        dataProvider = new DataProvider<RequestFile>() {

            @Override
            protected Iterable<RequestFile> getData(int first, int count) {
                final RequestFileFilter filter = filterModel.getObject();

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

                List<RequestFile> objects = requestFileBean.getRequestFiles(filter);

                AbstractProcessableListPanel.initializeSelectModels(selectModels, objects);

                return objects;
            }

            @Override
            protected int getSize() {
                return requestFileBean.size(filterModel.getObject());
            }
        };

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("objects_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        dataView = new DataView<RequestFile>("objects", dataProvider, 1) {

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
                        return !item.getModelObject().isProcessing() && !isGlobalWaiting(item.getModelObject());
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
                        return item.getModelObject().isProcessing();
                    }
                });

                //Анимация ожидание
                item.add(new StaticImage("waiting", new SharedResourceReference(AbstractProcessableListPanel.IMAGE_AJAX_WAITING)) {

                    @Override
                    public boolean isVisible() {
                        return isGlobalWaiting(item.getModelObject()) && !item.getModelObject().isProcessing();
                    }
                });

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(objectId)));

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

                item.add(new Label("month", DateUtil.displayMonth(item.getModelObject().getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(item.getModelObject().getYear())));

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
        PagingNavigator pagingNavigator = new PagingNavigator("paging", dataView, preferencePage, filterForm);
        pagingNavigator.addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                AbstractProcessableListPanel.clearSelection(selectModels);
            }
        });
        filterForm.add(pagingNavigator);

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Контейнер кнопок для ajax
        buttonContainer = new WebMarkupContainer("buttons");
        buttonContainer.setOutputMarkupId(true);
        buttonContainer.setVisibilityAllowed(modificationsAllowed);
        filterForm.add(buttonContainer);


        //Обработать
        buttonContainer.add(new AjaxLink<Void>("process") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                //TODO: add start processing dialog
            }
        });

        //Выгрузить
        buttonContainer.add(new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                completedDisplayed.put(getSaveProcessType(), false);

                processManagerBean.saveFacilityForm2(AbstractProcessableListPanel.getSelectedFileIds(selectModels), null);

                AbstractProcessableListPanel.clearSelection(selectModels);
                addTimer(dataViewContainer, filterForm, messages);
                target.add(filterForm);
            }
        });

        //Удалить
        buttonContainer.add(new AjaxLink<Void>("delete") {

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
                    final RequestFile requestFile = requestFileBean.findById(requestFileId);

                    if (requestFile != null) {
                        final String fullName = requestFile.getFullName();
                        try {
                            requestFileBean.delete(requestFile);

                            selectModels.remove(requestFileId);

                            info(MessageFormat.format(getString("info.deleted"), fullName));
                            log.info("Request file (ID : {}, full name: '{}') has been deleted.", requestFileId, fullName);
                            logBean.info(Module.NAME, getWebPage().getClass(), RequestFile.class, null, requestFileId,
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                        } catch (Exception e) {
                            error(MessageFormat.format(getString("error.delete"), fullName));
                            log.error("Cannot delete request file (ID : " + requestFileId + ", full name: '" + fullName + "').", e);
                            logBean.error(Module.NAME, getWebPage().getClass(), RequestFile.class, null, requestFileId,
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                            break;
                        }
                    }
                }
                target.add(filterForm);
                target.add(messages);
            }
        });

        //Отменить обработку
        buttonContainer.add(new AjaxLink<Void>("fill_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getFillProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getFillProcessType());
                info(getString("fill_process.canceling"));
                target.add(filterForm);
            }
        });

        //Отменить выгрузку
        buttonContainer.add(new AjaxLink<Void>("save_cancel") {

            @Override
            public boolean isVisible() {
                return processManagerBean.isProcessing(getSaveProcessType());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                processManagerBean.cancel(getSaveProcessType());
                info(getString("save_process.canceling"));
                target.add(filterForm);
            }
        });

        //Если описания структуры для файлов запросов не загружены в базу, сообщить об этом пользователю.
        if (!hasFieldDescription) {
            error(getString("file_description_missing"));
        }

        //Запуск таймера
        if (isGlobalProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        showMessages();
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        final AtomicInteger waitForStopTimer = new AtomicInteger();
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(7)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isGlobalProcessing() && waitForStopTimer.incrementAndGet() > 2) {
                    this.stop();
                    target.add(filterForm);
                } else {
                    //update feedback messages panel
                    target.add(messages);
                    target.add(buttonContainer);
                }

                statusTimerIndex.incrementAndGet();
            }
        };
    }

    private void addTimer(WebMarkupContainer dataViewContainer, Form<?> filterForm, AjaxFeedbackPanel messages) {
        boolean needCreateNewTimer = true;

        List<AjaxSelfUpdatingTimerBehavior> timers = newArrayList(filter(dataViewContainer.getBehaviors(),
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

    private void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        List<RequestFile> list = processManagerBean.getProcessed(processType, getClass());

        for (RequestFile requestFile : list) {
            if (requestFile.getStatus() == RequestFileStatus.SKIPPED) {
                AbstractProcessableListPanel.highlightProcessed(target, requestFile.getId());
                info(MessageFormat.format(getString(keyPrefix + ".skipped"), requestFile.getFullName()));
            } else if (requestFile.getStatus() == processedStatus) {
                AbstractProcessableListPanel.highlightProcessed(target, requestFile.getId());
                info(MessageFormat.format(getString(keyPrefix + ".processed"), requestFile.getFullName()));
            } else if (requestFile.getStatus() == errorStatus) {
                AbstractProcessableListPanel.highlightError(target, requestFile.getId());
                String message = requestFile.getErrorMessage() != null ? ": " + requestFile.getErrorMessage() : "";
                error(MessageFormat.format(getString(keyPrefix + ".error"), requestFile.getFullName()) + message);
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

    private void showMessages() {
        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        addMessages("fill_process", target, getFillProcessType(), RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
        addMessages("save_process", target, getSaveProcessType(), RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

        addCompetedMessages("fill_process", getFillProcessType());
        addCompetedMessages("save_process", getSaveProcessType());
    }

    private boolean isGlobalProcessing() {
        return processManagerBean.isGlobalProcessing(getFillProcessType())
                || processManagerBean.isGlobalProcessing(getSaveProcessType());
    }

    private boolean isGlobalWaiting(RequestFile requestFile) {
        return processManagerBean.isGlobalWaiting(getFillProcessType(), requestFile)
                || processManagerBean.isGlobalWaiting(getSaveProcessType(), requestFile);
    }

    private ProcessType getFillProcessType() {
        return ProcessType.FILL_FACILITY_FORM2;
    }

    private ProcessType getSaveProcessType() {
        return ProcessType.SAVE_FACILITY_FORM2;
    }
}
