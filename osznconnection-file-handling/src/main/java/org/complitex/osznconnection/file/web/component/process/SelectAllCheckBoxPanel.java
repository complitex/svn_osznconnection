/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;

/**
 *
 * @author Artem
 */
public final class SelectAllCheckBoxPanel extends Panel {

    public SelectAllCheckBoxPanel(String id, final ProcessingManager<?> processingManager) {
        super(id);

        CheckBox selectAll = new CheckBox("selectAll", new Model<Boolean>(false)) {

            @Override
            public boolean isEnabled() {
                return !processingManager.isGlobalProcessing();
            }

            @Override
            public void updateModel() {
                //skip update model
            }
        };
        selectAll.add(new CssAttributeBehavior("processable-list-panel-select-all"));
        add(selectAll);
    }
}
