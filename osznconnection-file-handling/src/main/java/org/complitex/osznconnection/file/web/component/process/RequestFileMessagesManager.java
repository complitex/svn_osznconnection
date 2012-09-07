/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

/**
 *
 * @author Artem
 */
public abstract class RequestFileMessagesManager extends MessagesManager<RequestFile> {

    public RequestFileMessagesManager(Component component) {
        super(component);
    }

    @Override
    protected RequestFileStatus getStatus(RequestFile object) {
        return object.getStatus();
    }

    @Override
    protected String getFullName(RequestFile object) {
        return object.getFullName();
    }
}
