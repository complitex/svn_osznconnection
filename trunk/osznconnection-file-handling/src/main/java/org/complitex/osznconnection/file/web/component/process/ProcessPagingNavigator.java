/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.data.DataView;
import org.complitex.dictionary.web.component.paging.IPagingNavigatorListener;
import org.complitex.dictionary.web.component.paging.PagingNavigator;

/**
 *
 * @author Artem
 */
public final class ProcessPagingNavigator extends PagingNavigator {

    public ProcessPagingNavigator(String id, DataView<?> dataView, String page, final SelectManager selectManager,
            Component... toUpdate) {
        super(id, dataView, page, toUpdate);

        addListener(new IPagingNavigatorListener() { //clear select checkbox model on page change

            @Override
            public void onChangePage() {
                selectManager.clearSelection();
            }
        });
    }
}
