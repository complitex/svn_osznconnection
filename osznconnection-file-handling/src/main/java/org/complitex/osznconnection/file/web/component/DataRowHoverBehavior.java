/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 *
 * @author Artem
 */
public final class DataRowHoverBehavior extends AbstractBehavior {

    public DataRowHoverBehavior() {
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(new CompressedResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".css"));
        response.renderJavascriptReference(new JavascriptResourceReference(DataRowHoverBehavior.class,
                DataRowHoverBehavior.class.getSimpleName() + ".js"));

    }

    public void deactivateDataRow(AjaxRequestTarget target) {
        target.appendJavascript("(function(){ $('table tr.data-row.data-row-hover').removeClass('data-row-hover'); })();");
    }
}
