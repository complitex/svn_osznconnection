/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

/**
 *
 * @author Artem
 */
public class RequestFileItemStatusLabel extends ItemStatusLabel<RequestFile> {

    public RequestFileItemStatusLabel(String id, ProcessingManager<RequestFile> processingManager,
            TimerManager timerManager) {
        super(id, processingManager, timerManager);
    }

    @Override
    protected RequestFileStatus getStatus(RequestFile object) {
        return object.getStatus();
    }
}
