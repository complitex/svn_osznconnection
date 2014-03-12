package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 *
 * @author Artem
 */
public final class DataRowHoverBehavior extends Behavior {

    public DataRowHoverBehavior() {
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".css")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".js")));
    }

    public void deactivateDataRow(AjaxRequestTarget target) {
        target.appendJavaScript("(function(){ $('table tr.data-row.data-row-hover').removeClass('data-row-hover'); })();");
    }
}
