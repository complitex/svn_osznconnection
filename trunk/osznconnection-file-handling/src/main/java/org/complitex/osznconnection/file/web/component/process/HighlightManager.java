package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.ajax.AjaxRequestTarget;

public  class HighlightManager {
    public static void highlightProcessed(AjaxRequestTarget target, Long objectId) {
//        if (target != null) {
//            target.appendJavaScript(new JsStatement().$(null, "#" + ProcessDataView.ITEM_ID_PREFIX + objectId)
//                    .chain(new HighlightEffect(HighlightEffect.Mode.show, "'lightgreen'", 300))
//                    .chain(new HighlightEffect(HighlightEffect.Mode.show, "E0E4E9", 700))
//                    .render());
//        }
    }

    public static void highlightError(AjaxRequestTarget target, Long objectId) {
//        if (target != null) {
//            target.appendJavaScript(new JsStatement().$(null, "#" + ProcessDataView.ITEM_ID_PREFIX + objectId)
//                    .chain(new HighlightEffect(HighlightEffect.Mode.show, "darksalmon", 300))
//                    .chain(new HighlightEffect(HighlightEffect.Mode.show, "'E0E4E9'", 700))
//                    .render());
//        }
    }
}
