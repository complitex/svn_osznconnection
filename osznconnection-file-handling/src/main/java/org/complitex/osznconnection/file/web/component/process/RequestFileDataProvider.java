/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import java.util.List;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
public class RequestFileDataProvider extends DataProvider<RequestFile> {

    private final TemplatePage page;
    private final IModel<RequestFileFilter> model;
    private final SelectManager selectManager;

    public RequestFileDataProvider(TemplatePage page,
            IModel<RequestFileFilter> model, SelectManager selectManager) {
        this.page = page;
        this.model = model;
        this.selectManager = selectManager;
    }

    private RequestFileBean requestFileBean() {
        return EjbBeanLocator.getBean(RequestFileBean.class);
    }

    @Override
    protected Iterable<? extends RequestFile> getData(int first, int count) {
        final RequestFileFilter filter = model.getObject();

        //store preference, but before clear data order related properties.
        {
            filter.setAscending(false);
            filter.setSortProperty(null);
            page.setFilterObject(filter);
        }
        page.setFilterObject(filter);

        //prepare filter object
        filter.setFirst(first);
        filter.setCount(count);
        filter.setSortProperty(getSort().getProperty());
        filter.setAscending(getSort().isAscending());

        List<RequestFile> requestFiles = requestFileBean().getRequestFiles(filter);

        selectManager.initializeSelectModels(requestFiles);

        return requestFiles;
    }

    @Override
    protected int getSize() {
        return requestFileBean().size(model.getObject());
    }
}
