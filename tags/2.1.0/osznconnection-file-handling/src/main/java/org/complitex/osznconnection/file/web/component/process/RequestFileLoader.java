/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.IRequestFileLoader;

/**
 *
 * @author Artem
 */
public abstract class RequestFileLoader implements IRequestFileLoader {

    private final MessagesManager<?> messagesManager;
    private final TimerManager timerManager;
    private final ProcessType loadProcessType;
    private final Form<?> form;

    public RequestFileLoader(MessagesManager<?> messagesManager, TimerManager timerManager,
            ProcessType loadProcessType, Form<?> form) {
        this.messagesManager = messagesManager;
        this.timerManager = timerManager;
        this.loadProcessType = loadProcessType;
        this.form = form;
    }

    public abstract void load(long userOrganizationId, long osznId, DateParameter dateParameter);

    @Override
    public void load(long userOrganizationId, long osznId, DateParameter dateParameter, AjaxRequestTarget target) {
        messagesManager.resetCompletedStatus(loadProcessType);

        load(userOrganizationId, osznId, dateParameter);

        timerManager.addTimer();
        target.add(form);
    }
}
