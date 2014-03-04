package org.complitex.osznconnection.file.web.component.process;

import com.google.common.collect.ImmutableSet;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;

import java.io.Serializable;
import java.util.Set;

public class ProcessingManager implements Serializable {
    private Set<ProcessType> supportedProcessTypes;

    public ProcessingManager(ProcessType... supportedProcessTypes) {
        this.supportedProcessTypes = ImmutableSet.copyOf(supportedProcessTypes);
    }

    private ProcessManagerBean processManagerBean() {
        return EjbBeanLocator.getBean(ProcessManagerBean.class);
    }

    public boolean isGlobalProcessing() {
        ProcessManagerBean processManagerBean = processManagerBean();
        boolean isGlobalProcessing = false;
        for (ProcessType processType : supportedProcessTypes) {
            isGlobalProcessing |= processManagerBean.isGlobalProcessing(processType);
        }

        return isGlobalProcessing;
    }

    public boolean isGlobalWaiting(IExecutorObject object) {
        ProcessManagerBean processManagerBean = processManagerBean();
        boolean isGlobalWaiting = false;
        for (ProcessType processType : supportedProcessTypes) {
            isGlobalWaiting |= processManagerBean.isGlobalWaiting(processType, object);
        }

        return isGlobalWaiting;
    }
}
