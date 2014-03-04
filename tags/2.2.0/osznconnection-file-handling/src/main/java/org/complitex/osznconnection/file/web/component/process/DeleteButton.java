/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;

/**
 *
 * @author Artem
 */
public abstract class DeleteButton extends AjaxLink<Void> {

    public DeleteButton(String id) {
        super(id);
    }

    @Override
    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new AjaxCallDecorator() {

            @Override
            public CharSequence decorateScript(Component c, CharSequence script) {
                return "if(confirm('" + getString("delete_caution") + "')){" + script + "}";
            }
        };
    }
}
