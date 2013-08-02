/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import java.util.Locale;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

/**
 *
 * @author Artem
 */
public final class RequestFileStatusRenderer {

    private RequestFileStatusRenderer() {
    }

    public static String render(RequestFileStatus status, Locale locale) {
        return ResourceUtil.getString(RequestFileStatusRenderer.class.getName(), status.name(), locale);
    }
}
