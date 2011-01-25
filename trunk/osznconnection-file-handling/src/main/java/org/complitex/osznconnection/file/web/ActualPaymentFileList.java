package org.complitex.osznconnection.file.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
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
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollListBehavior;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.pages.actualpayment.ActualPaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.*;
import org.complitex.template.web.pages.ScrollListPage;

import static org.complitex.osznconnection.file.service.process.ProcessManagerBean.TYPE.ACTUAL_PAYMENT;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 13.01.11 19:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ActualPaymentFileList extends ScrollListPage {
    private static final Logger log = LoggerFactory.getLogger(ActualPaymentFileList.class);

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

    @EJB(name = "LogBean")
    private LogBean logBean;

    private int waitForStopTimer;
    private int timerIndex = 0;
    private boolean completedDisplayed = false;

    private final static String ITEM_ID_PREFIX = "item";

    private RequestFileLoadPanel requestFileLoadPanel;

    public ActualPaymentFileList(PageParameters parameters) {
        super(parameters);
        init(parameters.getAsLong("request_file_id"));
    }

    public ActualPaymentFileList() {
        super();
        init(null);
    }

    private void init(Long requestFileId) {
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

        filterObject.setId(requestFileId);

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

        //Id
        filterForm.add(new TextField<String>("id"));

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

        //Имя
        filterForm.add(new TextField<String>("name"));

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

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

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
        final Map<RequestFile, IModel<Boolean>> selectModels = new HashMap<RequestFile, IModel<Boolean>>();

        //Модель данных списка
        final SortableDataProvider<RequestFile> dataProvider = new SortableDataProvider<RequestFile>() {

            @Override
            public Iterator<? extends RequestFile> iterator(int first, int count) {
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

                selectModels.clear();
                for (RequestFile rf : requestFiles) {
                    selectModels.put(rf, new Model<Boolean>(false));
                }

                return requestFiles.iterator();
            }

            @Override
            public int size() {
                return requestFileBean.size(filterModel.getObject());
            }

            @Override
            public IModel<RequestFile> model(RequestFile object) {
                return new Model<RequestFile>(object);
            }
        };
        dataProvider.setSort(getSortProperty("loaded"), getSortOrder(false));

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
                checkBox.setVisible(!rf.isProcessing() || !isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(rf.isProcessing());
                item.add(processing);

                item.add(new Label("id", StringUtil.valueOf(rf.getId())));

                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new BookmarkablePageLinkPanel<RequestFile>("name", rf.getFullName(),
                        ScrollListBehavior.SCROLL_PREFIX+String.valueOf(rf.getId()), ActualPaymentList.class,
                        new PageParameters("request_file_id=" + rf.getId())));

                DomainObject domainObject = organizationStrategy.findById(rf.getOrganizationId());
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                item.add(new Label("month", DateUtil.displayMonth(rf.getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(rf.getYear())));

                 //loaded, binding filled count
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount())));
                item.add(new Label("binded_record_count", StringUtil.valueOf(rf.getBindedRecordCount())));
                item.add(new Label("filled_record_count", StringUtil.valueOf(rf.getFilledRecordCount())));

                String dots = "";
                if (rf.isProcessing()){
                    if (processManagerBean.isProcessing(ACTUAL_PAYMENT)){
                        dots += StringUtil.getDots(timerIndex%5);
                    }
                }

                item.add(new Label("status", getStringOrKey(rf.getStatus()) + dots));
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
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.filled_record_count", "filled_record_count", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getClass().getName(), filterForm));

        //Связать
        Button bind = new Button("bind") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFile rf : selectModels.keySet()) {
                    if (selectModels.get(rf).getObject()) {
                        requestFiles.add(rf);
                    }
                }

                completedDisplayed = false;

                processManagerBean.bindActualPayment(requestFiles);

                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(bind);

        //Обработать
        Button process = new Button("process") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFile rf : selectModels.keySet()) {
                    if (selectModels.get(rf).getObject()) {
                        requestFiles.add(rf);
                    }
                }

                completedDisplayed = false;

                processManagerBean.fillActualPayment(requestFiles);

                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(process);

        //Выгрузить
        Button save = new Button("save") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFile rf : selectModels.keySet()) {
                    if (selectModels.get(rf).getObject()) {
                        requestFiles.add(rf);
                    }
                }

                completedDisplayed = false;

                processManagerBean.saveActualPayment(requestFiles);

                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(save);

        //Удалить
        Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
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

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(delete);

         //Отменить
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel(ACTUAL_PAYMENT);

                info(getStringOrKey("process.cancel"));
            }

            @Override
            public boolean isVisible() {
                return isProcessing() && !processManagerBean.isStop(ACTUAL_PAYMENT);
            }
        };
        filterForm.add(cancel);

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                getString("load_panel_title"),
                new RequestFileLoadPanel.ILoader(){

                    @Override
                    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed = false;
                        processManagerBean.loadActualPayment(organizationId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                }, ACTUAL_PAYMENT);

        add(requestFileLoadPanel);
    }

    private boolean isProcessing() {
        return processManagerBean.isProcessing(ACTUAL_PAYMENT);
    }

    private void showMessages() {

        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        List<RequestFile> list = processManagerBean.getProcessed(ACTUAL_PAYMENT, ActualPaymentFileList.class);

        for (RequestFile rf : list){

            switch (rf.getStatus()){
                case SKIPPED:
                    highlightProcessed(target, rf);
                    info(getStringFormat("actual_payment.skipped", rf.getFullName(),
                            processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));
                    break;
                case LOADED:
                case BOUND:
                case FILLED:
                case SAVED:
                    highlightProcessed(target, rf);
                    info(getStringFormat("actual_payment.processed", rf.getFullName(),
                            processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));
                    break;
                case LOAD_ERROR:
                case BIND_ERROR:
                case FILL_ERROR:
                case SAVE_ERROR:
                    highlightError(target, rf);
                    info(getStringFormat("actual_payment.process_error", rf.getFullName(),
                            processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));
                    break;
            }
        }

       //Process completed
        if (processManagerBean.isCompleted(ACTUAL_PAYMENT) && !completedDisplayed) {
            info(getStringFormat("process.done", processManagerBean.getSuccessCount(ACTUAL_PAYMENT),
                    processManagerBean.getSkippedCount(ACTUAL_PAYMENT), processManagerBean.getErrorCount(ACTUAL_PAYMENT),
                    processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));

            completedDisplayed = true;
        }

        //Process canceled
        if (processManagerBean.isCanceled(ACTUAL_PAYMENT) && !completedDisplayed) {
            info(getStringFormat("process.canceled", processManagerBean.getSuccessCount(ACTUAL_PAYMENT),
                    processManagerBean.getSkippedCount(ACTUAL_PAYMENT), processManagerBean.getErrorCount(ACTUAL_PAYMENT),
                    processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));

            completedDisplayed = true;
        }

        //Process error
        if (processManagerBean.isCriticalError(ACTUAL_PAYMENT) && !completedDisplayed) {
            error(getStringFormat("process.critical_error", processManagerBean.getSuccessCount(ACTUAL_PAYMENT),
                    processManagerBean.getSkippedCount(ACTUAL_PAYMENT), processManagerBean.getErrorCount(ACTUAL_PAYMENT),
                    processManagerBean.getProcess(ACTUAL_PAYMENT).ordinal()));

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
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

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

                timerIndex++;
            }
        };
    }

    private void addTimer(WebMarkupContainer dataViewContainer, Form<?> filterForm, AjaxFeedbackPanel messages) {
        boolean needCreateNewTimer = true;

        List<AjaxSelfUpdatingTimerBehavior> timers = null;
        timers = Lists.newArrayList(Iterables.filter(dataViewContainer.getBehaviors(), AjaxSelfUpdatingTimerBehavior.class));
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

            @Override
            protected void onClick() {
                requestFileLoadPanel.open();
            }
        });
    }
}
