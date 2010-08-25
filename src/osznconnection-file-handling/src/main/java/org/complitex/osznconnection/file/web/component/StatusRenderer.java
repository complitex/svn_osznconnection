/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.Status;

/**
 *
 * @author Artem
 */
public class StatusRenderer implements IChoiceRenderer<Status> {

    private static final String RESOURCE_BUNDLE = StatusRenderer.class.getName();

    @Override
    public Object getDisplayValue(Status object) {
        return displayValue(object);
    }

    @Override
    public String getIdValue(Status object, int index) {
        return object.name();
    }

    public static String displayValue(Status object) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, object.name(), Session.get().getLocale());
    }
}
