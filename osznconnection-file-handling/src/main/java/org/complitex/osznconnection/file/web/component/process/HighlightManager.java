/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author Artem
 */
public final class HighlightManager {

    private HighlightManager() {
    }

    public static void highlightProcessed(AjaxRequestTarget target, long objectId) {
        if (target != null) {
            target.appendJavaScript("$('#" + ProcessDataView.ITEM_ID_PREFIX + objectId + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    public static void highlightError(AjaxRequestTarget target, long objectId) {
        if (target != null) {
            target.appendJavaScript("$('#" + ProcessDataView.ITEM_ID_PREFIX + objectId + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }
}
