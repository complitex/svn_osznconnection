/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
import org.complitex.dictionary.web.component.YearDropDownChoice;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.process.*;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class FacilityForm2FileList extends ScrollListPage {

    private static final Logger log = LoggerFactory.getLogger(FacilityForm2FileList.class);
    private static final int AJAX_TIMER = 4;
    @EJB
    private ProcessManagerBean processManagerBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;
    private WebMarkupContainer buttons;
    private Form<RequestFileFilter> form;
    private ModificationManager modificationManager;

    public FacilityForm2FileList() {
        this.modificationManager = new ModificationManager(this, hasFieldDescription());
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

    private void init() {
        final RequestFileProcessingManager processingManager =
                new RequestFileProcessingManager(getFillProcessType(), getSaveProcessType());
        final RequestFileMessagesManager messagesManager = new RequestFileMessagesManager(this) {

            @Override
            public void showMessages(AjaxRequestTarget target) {
                addMessages("fill_process", target, getFillProcessType(),
                        RequestFileStatus.FILLED, RequestFileStatus.FILL_ERROR);
                addMessages("save_process", target, getSaveProcessType(),
                        RequestFileStatus.SAVED, RequestFileStatus.SAVE_ERROR);

                addCompetedMessages("fill_process", getFillProcessType());
                addCompetedMessages("save_process", getSaveProcessType());
            }
        };

        add(new Label("title", new ResourceModel("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Фильтр модель
        RequestFileFilter filter = (RequestFileFilter) getFilterObject(newFilter());
        final IModel<RequestFileFilter> model = new CompoundPropertyModel<RequestFileFilter>(filter);

        //Фильтр форма
        form = new Form<>("form", model);
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

                //ОСЗН
                item.add(new ItemOrganizationLabel("organization", requestFile.getOrganizationId()));

                //Организация пользователя
                item.add(new ItemOrganizationLabel("userOrganization", requestFile.getUserOrganizationId()));

                item.add(new Label("month", DateUtil.displayMonth(requestFile.getBeginDate(), getLocale())));
                item.add(new Label("year", DateUtil.getYear(requestFile.getBeginDate()) + ""));

                //Количество обработанных записей
                item.add(new Label("filled_record_count", new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        return StringUtil.valueOf(requestFile.getFilledRecordCount());
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
        form.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.user_organization", "user_organization_id", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, form));
        form.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, form));

        //Контейнер кнопок для ajax
        buttons = new WebMarkupContainer("buttons");
        buttons.setOutputMarkupId(true);
        buttons.setVisibilityAllowed(modificationManager.isModificationsAllowed());
        form.add(buttons);

        timerManager.addUpdateComponent(buttons);

        //Обработать
        buttons.add(new AjaxLink<Void>("process") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                //TODO: add start processing dialog
            }
        });

        //Выгрузить
        buttons.add(new AjaxLink<Void>("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                messagesManager.resetCompletedStatus(getSaveProcessType());

                processManagerBean.saveFacilityForm2(selectManager.getSelectedFileIds(), null);

                selectManager.clearSelection();
                timerManager.addTimer();
                target.add(form);
            }
        });

        //Удалить
        buttons.add(new RequestFileDeleteButton("delete", selectManager, form, messages) {

            @Override
            protected Class<?> getLoggerControllerClass() {
                return FacilityForm2FileList.class;
            }

            @Override
            protected void logSuccess(RequestFile requestFile) {
                log.info("Request file (ID : {}, full name: '{}') has been deleted.", requestFile.getId(),
                        requestFile.getFullName());
            }

            @Override
            protected void logError(RequestFile requestFile, Exception e) {
                log.error("Cannot delete request file (ID : " + requestFile.getId() + ", full name: '"
                        + requestFile.getFullName() + "').", e);
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

        //Запуск таймера
        timerManager.startTimer();

        //Отобразить сообщения
        messagesManager.showMessages();

        //Отобразить сообщения об отсутствии описания файлов запросов если необходимо
        modificationManager.reportErrorIfNecessary();
    }

    private ProcessType getFillProcessType() {
        return ProcessType.FILL_FACILITY_FORM2;
    }

    private ProcessType getSaveProcessType() {
        return ProcessType.SAVE_FACILITY_FORM2;
    }
}
