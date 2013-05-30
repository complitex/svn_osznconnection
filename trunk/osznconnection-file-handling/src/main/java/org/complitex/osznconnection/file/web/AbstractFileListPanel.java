/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.web.component.BookmarkablePageLinkPanel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.scroll.ScrollListBehavior;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Artem
 */
public abstract class AbstractFileListPanel extends AbstractProcessableListPanel<RequestFile, RequestFileFilter> {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LogBean logBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    public AbstractFileListPanel(String id) {
        super(id);

        //Имя
        addColumn(new Column() {

            @Override
            public Component head(ISortStateLocator stateLocator, DataView<?> dataView, Component refresh) {
                return new ArrowOrderByBorder("header.name", "name", stateLocator, dataView, refresh);
            }

            @Override
            public Component filter() {
                return new TextField<String>("name");
            }

            @Override
            public Component field(Item<RequestFile> item) {
                return new BookmarkablePageLinkPanel<RequestFile>("name", item.getModelObject().getFullName(),
                        ScrollListBehavior.SCROLL_PREFIX + String.valueOf(item.getModelObject().getId()), getItemListPageClass(),
                        new PageParameters().set("request_file_id", item.getModelObject().getId()));
            }
        });
    }

    @Override
    protected boolean hasFieldDescription() {
        return requestFileDescriptionBean.getFileDescription(getRequestFileType()) != null;
    }

    @Override
    protected void initFilter(RequestFileFilter filter) {
        super.initFilter(filter);
        filter.setType(getRequestFileType());
    }

    @Override
    protected void logSuccessfulDeletion(RequestFile requestFile) {
        final long requestFileId = requestFile.getId();
        logger().info("Request file (ID : {}, full name: '{}') has been deleted.", requestFileId, getFullName(requestFile));
        logBean.info(Module.NAME, getWebPage().getClass(), RequestFile.class, null, requestFileId,
                Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                requestFile.getLogObjectName());
    }

    @Override
    protected void logFailDeletion(RequestFile requestFile, Exception e) {
        final long requestFileId = requestFile.getId();
        logger().error("Cannot delete request file (ID : " + requestFileId + ", full name: '" + getFullName(requestFile) + "').", e);
        logBean.error(Module.NAME, getWebPage().getClass(), RequestFile.class, null, requestFileId,
                Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                requestFile.getLogObjectName());
    }

    private Logger logger() {
        return LoggerFactory.getLogger(getWebPage().getClass());
    }

    @Override
    protected int getSize(RequestFileFilter filter) {
        return requestFileBean.size(filter);
    }

    @Override
    protected List<RequestFile> getObjects(RequestFileFilter filter) {
        return requestFileBean.getRequestFiles(filter);
    }

    @Override
    protected boolean isProcessing(RequestFile object) {
        return object.isProcessing();
    }

    @Override
    protected RequestFileStatus getStatus(RequestFile object) {
        return object.getStatus();
    }

    @Override
    protected String getFullName(RequestFile object) {
        return object.getFullName();
    }

    @Override
    protected Date getLoaded(RequestFile object) {
        return object.getLoaded();
    }

    @Override
    protected long getOsznId(RequestFile object) {
        return object.getOrganizationId();
    }

    @Override
    protected long getUserOrganizationId(RequestFile object) {
        return object.getUserOrganizationId();
    }

    @Override
    protected int getMonth(RequestFile object) {
        return DateUtil.getMonth(object.getBeginDate()) + 1;
    }

    @Override
    protected int getYear(RequestFile object) {
        return DateUtil.getYear(object.getBeginDate());
    }

    @Override
    protected int getLoadedRecordCount(RequestFile object) {
        return object.getLoadedRecordCount();
    }

    @Override
    protected int getBindedRecordCount(RequestFile object) {
        return object.getBindedRecordCount();
    }

    @Override
    protected int getFilledRecordCount(RequestFile object) {
        return object.getFilledRecordCount();
    }

    @Override
    protected RequestFile getById(long id) {
        return requestFileBean.findById(id);
    }

    @Override
    protected void delete(RequestFile requestFile) {
        requestFileBean.delete(requestFile);
    }

    @Override
    protected MonthParameterViewMode getLoadMonthParameterViewMode() {
        return MonthParameterViewMode.RANGE;
    }

    protected abstract RequestFile.TYPE getRequestFileType();

    protected abstract Class<? extends WebPage> getItemListPageClass();
}