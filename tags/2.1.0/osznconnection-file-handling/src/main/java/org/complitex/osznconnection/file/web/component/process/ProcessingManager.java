/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import java.io.Serializable;
import java.util.Set;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;

/**
 *
 * @author Artem
 */
public abstract class ProcessingManager<M extends IExecutorObject> implements Serializable {

    public abstract boolean isProcessing(M object);

    protected abstract Set<ProcessType> getSupportedProcessTypes();

    private ProcessManagerBean processManagerBean() {
        return EjbBeanLocator.getBean(ProcessManagerBean.class);
    }

    public boolean isGlobalProcessing() {
        ProcessManagerBean processManagerBean = processManagerBean();
        boolean isGlobalProcessing = false;
        for (ProcessType processType : getSupportedProcessTypes()) {
            isGlobalProcessing |= processManagerBean.isGlobalProcessing(processType);
        }
        return isGlobalProcessing;
    }

    public boolean isGlobalWaiting(M object) {
        ProcessManagerBean processManagerBean = processManagerBean();
        boolean isGlobalWaiting = false;
        for (ProcessType processType : getSupportedProcessTypes()) {
            isGlobalWaiting |= processManagerBean.isGlobalWaiting(processType, object);
        }
        return isGlobalWaiting;
    }
}
