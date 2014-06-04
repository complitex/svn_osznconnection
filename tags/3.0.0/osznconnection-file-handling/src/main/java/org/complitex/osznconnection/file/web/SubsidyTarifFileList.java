package org.complitex.osznconnection.file.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.ajax.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
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

import static org.complitex.dictionary.util.DateUtil.getYear;
import static org.complitex.osznconnection.file.service.process.ProcessType.LOAD_SUBSIDY_TARIF;

/**
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class SubsidyTarifFileList extends TemplatePage {

    private static final int AJAX_TIMER = 4;

    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    private RequestFileLoadPanel requestFileLoadPanel;
    private final ModificationManager modificationManager;

    public SubsidyTarifFileList() {
        this.modificationManager = new ModificationManager(this, hasFieldDescription());
        init();
    }

    private boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(RequestFileType.SUBSIDY_TARIF) != null;
    }

    private RequestFileFilter newFilter() {
        final RequestFileFilter filter = new RequestFileFilter();
        filter.setType(RequestFileType.SUBSIDY_TARIF);
        return filter;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        AbstractProcessableListPanel.renderResources(response);
    }

    private void init() {
        final ProcessingManager processingManager = new ProcessingManager(ProcessType.LOAD_SUBSIDY_TARIF);
        final MessagesManager messagesManager = new MessagesManager(this) {

            @Override
            public void showMessages(AjaxRequestTarget target) {
                addMessages("load_process", target, LOAD_SUBSIDY_TARIF,
                        RequestFileStatus.LOADED, RequestFileStatus.LOAD_ERROR);
                addCompetedMessages("load_process", LOAD_SUBSIDY_TARIF);
            }
        };

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Фильтр модель
        final RequestFileFilter filter = (RequestFileFilter) getFilterObject(newFilter());
        final IModel<RequestFileFilter> model = new CompoundPropertyModel<RequestFileFilter>(filter);

        //Фильтр форма
        final Form<RequestFileFilter> form = new Form<RequestFileFilter>("form", model);
        form.setOutputMarkupId(true);
        add(form);

        AjaxLink<Void> filter_reset = new AjaxLink<Void>("filter_reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                RequestFileFilter filterObject = newFilter();
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

        //Имя
        form.add(new TextField<String>("name"));

        //Осзн
        form.add(new OsznFilter("organization"));

        // Организация пользователя
        form.add(new UserOrganizationFilter("userOrganization"));

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
            protected void populateItem(final Item<RequestFile> item) {
                final Long objectId = item.getModelObject().getId();

                //Выбор файлов
                item.add(new ItemCheckBoxPanel<RequestFile>("itemCheckBoxPanel", processingManager, selectManager));

                //Идентификатор файла
                item.add(new Label("id", StringUtil.valueOf(objectId)));

                //Дата загрузки
                item.add(new ItemDateLoadedLabel("loaded", item.getModelObject().getLoaded()));

                item.add(new Label("name", item.getModelObject().getFullName()));

                //ОСЗН
                item.add(new ItemOrganizationLabel("organization", item.getModelObject().getOrganizationId()));

                //Организация пользователя
                item.add(new ItemOrganizationLabel("userOrganization", item.getModelObject().getUserOrganizationId()));

                item.add(new Label("year", getYear(item.getModelObject().getBeginDate()) + ""));

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
                item.add(new ItemStatusLabel("status", processingManager, timerManager));
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
        form.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, form));

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
                return SubsidyTarifFileList.class;
            }

            @Override
            protected void logSuccess(RequestFile requestFile) {
                log().info("Request file (ID : {}, full name: '{}') has been deleted.", requestFile.getId(),
                        requestFile.getFullName());
            }

            @Override
            protected void logError(RequestFile requestFile, Exception e) {
                log().error("Cannot delete request file (ID : " + requestFile.getId() + ", full name: '"
                        + requestFile.getFullName() + "').", e);
            }
        });

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel", new ResourceModel("load_panel_title"),
                MonthParameterViewMode.HIDDEN) {
            @Override
            protected void load(Long userOrganizationId, Long osznId, DateParameter dateParameter, AjaxRequestTarget target) {
                processManagerBean.loadSubsidyTarif(userOrganizationId, osznId, dateParameter.getMonth(), dateParameter.getYear());

                messagesManager.resetCompletedStatus(ProcessType.LOAD_SUBSIDY_TARIF);

                selectManager.clearSelection();
                timerManager.addTimer();
                target.add(form);
            }
        };

        add(requestFileLoadPanel);

        //Запуск таймера
        timerManager.startTimer();

        //Отобразить сообщения
        messagesManager.showMessages();

        //Отобразить сообщения об отсутствии описания файлов запросов если необходимо
        modificationManager.reportErrorIfNecessary();
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
