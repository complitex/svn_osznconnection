package org.complitex.osznconnection.file.web;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.complitex.dictionary.web.component.paging.IPagingNavigatorListener;
import org.complitex.osznconnection.file.web.component.ReuseIfLongIdEqualStrategy;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
import org.complitex.resources.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.image.StaticImage;
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
    private RequestFileLoadPanel requestFileLoadPanel;
    private boolean modificationsAllowed;
    private boolean hasFieldDescription;

    public TarifFileList(PageParameters parameters) {
        super();
        init(parameters.get("request_file_id").toOptionalLong());
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

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderJavaScriptReference(WebCommonResourceInitializer.HIGHLIGHT_JS);
        response.renderJavaScriptReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".js"));
        response.renderCSSReference(new PackageResourceReference(AbstractProcessableListPanel.class,
                AbstractProcessableListPanel.class.getSimpleName() + ".css"));
    }

    private void init(Long requestFileId) {
        this.hasFieldDescription = hasFieldDescription();
        this.modificationsAllowed =
                //- только пользователи, принадлежащие организации или администраторы могут обрабатывать файлы.
                (osznSessionBean.getCurrentUserOrganizationId(getTemplateSession()) != null || osznSessionBean.isAdmin())
                && //можно обрабатывать файлы, только если в базу загружены описания структур для файлов запросов.
                hasFieldDescription;

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Фильтр модель
        final RequestFileFilter filter = (RequestFileFilter) getFilterObject(newFilter(requestFileId));
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filter);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        AjaxLink<Void> filter_reset = new AjaxLink<Void>("filter_reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                RequestFileFilter filterObject = newFilter(null);
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
                return !isProcessing();
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
        final Map<Long, IModel<Boolean>> selectModels = new HashMap<Long, IModel<Boolean>>();

        //Модель данных списка
        final DataProvider<RequestFile> dataProvider = new DataProvider<RequestFile>() {

            @Override
            protected Iterable<? extends RequestFile> getData(int first, int count) {
                final RequestFileFilter filter = filterModel.getObject();

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
        dataProvider.setSort("loaded", SortOrder.DESCENDING);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1) {

            @Override
            protected void populateItem(final Item<RequestFile> item) {
                final Long objectId = item.getModelObject().getId();

                /* for highlighting to work properly */
                AbstractProcessableListPanel.augmentItem(item, objectId);

                //Выбор файлов
                CheckBox checkBox = new CheckBox("selected", selectModels.get(objectId)) {

                    @Override
                    public boolean isVisible() {
                        return !isLoading(item.getModelObject()) || !isProcessing();
                    }

                    @Override
                    public boolean isEnabled() {
                        return !isProcessing();
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
                        return isLoading(item.getModelObject());
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

                item.add(new Label("month", DateUtil.displayMonth(item.getModelObject().getMonth(), getLocale())));
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
                        if (isLoaded(item.getModelObject())) {
                            return getStringOrKey("status.loaded");
                        } else if (isLoading(item.getModelObject())) {
                            return getStringOrKey("status.loading");
                        } else if (isLoadError(item.getModelObject())) {
                            return getStringOrKey("status.load_error");
                        } else {
                            return "";
                        }
                    }
                }));
            }
        };
        dataViewContainer.add(dataView);

        //Reuse Strategy
        dataView.setItemReuseStrategy(new ReuseIfLongIdEqualStrategy());

        //Постраничная навигация
        PagingNavigator pagingNavigator = new PagingNavigator("paging", dataView, getPreferencesPage(), filterForm);
        pagingNavigator.addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                for (IModel<Boolean> model : selectModels.values()) {
                    model.setObject(false);
                }
            }
        });
        filterForm.add(pagingNavigator);

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));

        showMessages();

        if (isProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        //Удалить
        final AjaxLink<Void> delete = new AjaxLink<Void>("delete") {

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
                for (long requestFileId : selectModels.keySet()) {
                    if (selectModels.get(requestFileId).getObject()) {
                        RequestFile tarifFile = requestFileBean.findById(requestFileId);
                        try {
                            requestFileBean.delete(tarifFile);

                            info(getStringFormat("info.deleted", tarifFile.getFullName()));

                            logBean.info(Module.NAME, TarifFileList.class, RequestFileGroup.class, null, tarifFile.getId(),
                                    Log.EVENT.REMOVE, tarifFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                                    tarifFile.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("error.delete", tarifFile.getFullName()));

                            logBean.error(Module.NAME, TarifFileList.class, RequestFileGroup.class, null, tarifFile.getId(),
                                    Log.EVENT.REMOVE, tarifFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    tarifFile.getLogObjectName());
                            break;
                        }
                    }
                }
                target.add(filterForm);
                target.add(messages);
            }
        };
        delete.setVisibilityAllowed(modificationsAllowed);
        filterForm.add(delete);

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new RequestFileLoadPanel.ILoader() {

                    @Override
                    public void load(long userOrganizationId, long osznId, String districtCode, int monthFrom, int monthTo,
                            int year, AjaxRequestTarget target) {
                        completedDisplayed = false;
                        processManagerBean.loadTarif(userOrganizationId, osznId, districtCode, monthFrom, monthTo, year);

                        addTimer(dataViewContainer, filterForm, messages);
                        target.add(filterForm);
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
                AbstractProcessableListPanel.highlightProcessed(target, rf.getId());
            } else {
                error(getStringFormat("tarif.load_error", rf.getFullName()));
                AbstractProcessableListPanel.highlightError(target, rf.getId());
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

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        waitForStopTimer = 0;
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(4)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isProcessing() && ++waitForStopTimer > 2) {
                    this.stop();
                    target.add(filterForm);
                } else {
                    //update feedback messages panel
                    target.add(messages);
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
