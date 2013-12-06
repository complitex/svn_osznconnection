/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.*;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.AbstractProcessableListPanel;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.file.web.component.process.*;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public abstract class AbstractReferenceBookFileList extends TemplatePage {

    private static final int AJAX_TIMER = 1;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    private RequestFileLoadPanel requestFileLoadPanel;
    private final ModificationManager modificationManager;

    protected AbstractReferenceBookFileList() {
        this.modificationManager = new ModificationManager(this, hasFieldDescription());
        init();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        AbstractProcessableListPanel.renderResources(response);
    }

    protected abstract RequestFileType getRequestFileType();

    protected abstract void load(long userOrganizationId, long osznId, DateParameter dateParameter);

    protected abstract ProcessType getLoadProcessType();

    protected abstract Class<? extends Page> getItemsPage();

    private boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(getRequestFileType()) != null;
    }

    protected void init() {
        final RequestFileProcessingManager processingManager =
                new RequestFileProcessingManager(getLoadProcessType());
        final RequestFileMessagesManager messagesManager = new RequestFileMessagesManager(this) {

            @Override
            public void showMessages(AjaxRequestTarget target) {
                addMessages("load_process", target, getLoadProcessType(),
                        RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
                addCompetedMessages("load_process", getLoadProcessType());
            }
        };

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
        form.add(new SelectAllCheckBoxPanel("selectAllCheckBoxPanel", processingManager));

        //Id
        form.add(new TextField<String>("id"));

        //Дата загрузки
        form.add(new DatePicker<Date>("loaded"));

        //Имя
        form.add(new TextField<String>("name"));

        //Осзн
        form.add(new OsznFilter("organization"));

        // Организация пользователя
        form.add(new UserOrganizationFilter("userOrganization"));

        //Месяц
        form.add(new MonthDropDownChoice("month").setNullValid(true));

        //Год
        form.add(new YearDropDownChoice("year").setNullValid(true));

        //Статус
        form.add(new RequestFileStatusFilter("status"));

        //Модель выбранных элементов списка.
        final SelectManager selectManager = new SelectManager();

        //Модель данных списка
        final RequestFileDataProvider dataProvider = new RequestFileDataProvider(this, model, selectManager);
        dataProvider.setSort("loaded", SortOrder.DESCENDING);

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        form.add(dataViewContainer);

        final TimerManager timerManager = new TimerManager(AJAX_TIMER, messagesManager, processingManager, form,
                dataViewContainer);
        timerManager.addUpdateComponent(messages);

        //Таблица файлов запросов
        final ProcessDataView<RequestFile> dataView = new ProcessDataView<RequestFile>("request_files", dataProvider) {

            @Override
            protected void populateItem(Item<RequestFile> item) {
                final RequestFile requestFile = item.getModelObject();

                //Выбор файлов
                item.add(new ItemCheckBoxPanel<>("itemCheckBoxPanel", processingManager, selectManager));

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(requestFile.getId())));

                //Дата загрузки
                item.add(new ItemDateLoadedLabel("loaded", requestFile.getLoaded()));

                item.add(new BookmarkablePageLinkPanel<>("name", requestFile.getFullName(), getItemsPage(),
                        new PageParameters().set("request_file_id", requestFile.getId())));

                //ОСЗН
                item.add(new ItemOrganizationLabel("organization", requestFile.getOrganizationId()));

                //Организация пользователя
                item.add(new ItemOrganizationLabel("userOrganization", requestFile.getUserOrganizationId()));

                item.add(new Label("month", DateUtil.displayMonth(requestFile.getBeginDate(), getLocale())));
                item.add(new Label("year", DateUtil.getYear(requestFile.getBeginDate()) + ""));

                item.add(new Label("dbf_record_count", StringUtil.valueOf(item.getModelObject().getDbfRecordCount())));

                //Количество загруженных записей
                item.add(new Label("loaded_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(requestFile.getLoadedRecordCount());
                    }
                }));

                //Статус
                item.add(new RequestFileItemStatusLabel("status", processingManager, timerManager));
            }
        };
        dataViewContainer.add(dataView);

        //Постраничная навигация
        ProcessPagingNavigator pagingNavigator = new ProcessPagingNavigator("paging", dataView, getPreferencesPage(),
                selectManager, form);
        form.add(pagingNavigator);

        //Сортировка
        form.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, form));

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

        //Удалить
        buttons.add(new RequestFileDeleteButton("delete", selectManager, form, messages) {

            @Override
            protected Class<?> getLoggerControllerClass() {
                return AbstractReferenceBookFileList.this.getClass();
            }

            @Override
            protected void logSuccess(RequestFile requestFile) {
                log().info("Request file of type {} (ID : {}, full name: '{}') has been deleted.",
                        new Object[]{getRequestFileType(), requestFile.getId(), requestFile.getFullName()});
            }

            @Override
            protected void logError(RequestFile requestFile, Exception e) {
                log().error("Couldn't delete request file of type " + getRequestFileType()
                        + " (ID: " + requestFile.getId() + ", full name: '" + requestFile.getFullName() + "').", e);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new ResourceModel("load_panel_title"),
                new RequestFileLoader(messagesManager, timerManager, getLoadProcessType(), form) {

                    @Override
                    public void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
                        AbstractReferenceBookFileList.this.load(userOrganizationId, osznId, dateParameter);
                    }
                }, MonthParameterViewMode.EXACT);

        add(requestFileLoadPanel);

        //Запуск таймера
        timerManager.startTimer();

        //Отобразить сообщения
        messagesManager.showMessages();

        //Отобразить сообщения об отсутствии описания файлов запросов если необходимо
        modificationManager.reportErrorIfNecessary();
    }

    private RequestFileFilter newFilter() {
        final RequestFileFilter filter = new RequestFileFilter();
        filter.setType(getRequestFileType());
        return filter;
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
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
