package org.complitex.osznconnection.file.web;

import org.apache.wicket.Page;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.*;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.FileExecutorService;
import org.complitex.osznconnection.file.service.LoadRequestBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileFilter;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

    private int waitForStopTimer;

    private final static String ITEM_ID_PREFIX = "item";

    public RequestFileList() {
        super();

        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileFilter filterObject = new RequestFileFilter();
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filterObject);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();
                filterModel.setObject(new RequestFileFilter());
            }
        };
        filterForm.add(filter_reset);

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

        //Имя
        filterForm.add(new TextField<String>("name"));

        //Организация
        filterForm.add(new DropDownChoice<DomainObject>("organization",
                organizationStrategy.getAllOSZNs(), new IChoiceRenderer<DomainObject>() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }

            @Override
            public String getIdValue(DomainObject object, int index) {
                return String.valueOf(object.getId());
            }
        }));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Всего записей
        filterForm.add(new TextField<Integer>("dbfRecordCount", new Model<Integer>(), Integer.class));

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", new Model<Integer>(), Integer.class));

        //Связано записей
        filterForm.add(new TextField<Integer>("bindedRecordCount", new Model<Integer>(), Integer.class));

        //Статус
        filterForm.add(new DropDownChoice<RequestFile.STATUS>("status",
                Arrays.asList(RequestFile.STATUS.values()),
                new IChoiceRenderer<RequestFile.STATUS>() {

                    @Override
                    public Object getDisplayValue(RequestFile.STATUS object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(RequestFile.STATUS object, int index) {
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
                checkBox.setVisible(!rf.isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(rf.isProcessing());
                item.add(processing);

                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("name", rf.getName()));

                DomainObject domainObject = organizationStrategy.findById(rf.getOrganizationObjectId());
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                item.add(DateLabel.forDatePattern("month", new Model<Date>(rf.getDate()), "MMMM"));
                item.add(DateLabel.forDatePattern("year", new Model<Date>(rf.getDate()), "yyyy"));
                item.add(new Label("dbf_record_count", StringUtil.valueOf(rf.getDbfRecordCount())));
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount(), rf.getDbfRecordCount())));
                item.add(new Label("binded_record_count", StringUtil.valueOf(rf.getBindedRecordCount(), rf.getDbfRecordCount())));
                item.add(new Label("status", getStringOrKey(rf.getStatus().name())));

                Class<? extends Page> page = null;
                if (rf.isPayment()) {
                    page = PaymentList.class;
                } else if (rf.isBenefit()) {
                    page = BenefitList.class;
                }

                item.add(new BookmarkablePageLinkPanel<RequestFile>("action_list", getString("action_list"),
                        page, new PageParameters("request_file_id=" + rf.getId())));
            }
        };
        dataViewContainer.add(dataView);

        showMessages();

        //Таймер
        if (isProcessing()) {
            waitForStopTimer = 0;

            dataViewContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

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
            });
        }

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_object_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.dbf_record_count", "dbf_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, filterForm));

        //Удалить
        Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFileBean.delete(requestFile);
                        info(getStringFormat("info.deleted", requestFile.getName()));
                    }
                }
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(delete);

        //Связать
        Button bind = new Button("bind") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFiles.add(requestFile);
                    }
                }

                FileExecutorService.get().bind(requestFiles);
                selectModels.clear();
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(bind);
    }

    private boolean isProcessing() {
        return loadRequestBean.isLoading() || FileExecutorService.get().isBinding();
    }

    private int renderedIndex = 0;

    private boolean showLoaded = false;

    private void showMessages() {
        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        List<RequestFile> processed = loadRequestBean.getProcessed();

        for (int i = renderedIndex; i < processed.size(); ++i) {
            RequestFile rf = processed.get(i);

            switch (rf.getStatus()) {
                case LOADED:
                    if (target != null) { //highlight loaded
                        target.appendJavascript("$('#" + ITEM_ID_PREFIX + rf.getId() + "')"
                                + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                                + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
                    }
                    break;
                case ERROR_ALREADY_LOADED:
                    Calendar year = Calendar.getInstance();
                    year.setTime(rf.getDate());

                    error(getStringFormat("error.already_loaded", rf.getName(), year.get(Calendar.YEAR)));
                    break;
                case ERROR_SQL_SESSION:
                case ERROR_FIELD_TYPE:
                case ERROR_XBASEJ:
                case ERROR:
                    if (target != null) { //highlight error
                        target.appendJavascript("$('#" + ITEM_ID_PREFIX + rf.getId() + "')"
                                + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                                + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
                    }
                    error(getStringFormat("error.common", rf.getName()));
            }
        }

        if (!isProcessing() && processed.size() > 0 && !showLoaded) {
            int loaded = 0;
            int error = 0;

            for (RequestFile rf : processed) {
                if (rf.getStatus() == RequestFile.STATUS.LOADED) {
                    loaded++;
                } else {
                    error++;
                }
            }

            info(getStringFormat("info.loaded", loaded, error));
            showLoaded = true;
        }

        renderedIndex = processed.size();
    }
}
