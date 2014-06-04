package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
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
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);

        attributes.getAjaxCallListeners().add(new AjaxCallListener(){
            @Override
            public AjaxCallListener onBefore(CharSequence before) {
                return super.onBefore("if(confirm('" + getString("delete_caution") + "')){" + before + "}");
            }
        });
    }
}
