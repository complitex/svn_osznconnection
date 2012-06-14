/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
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
        response.renderCSSReference(new PackageResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".css"));
        response.renderJavaScriptReference(new PackageResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".js"));
    }

    public void deactivateDataRow(AjaxRequestTarget target) {
        target.appendJavaScript("(function(){ $('table tr.data-row.data-row-hover').removeClass('data-row-hover'); })();");
    }
}
