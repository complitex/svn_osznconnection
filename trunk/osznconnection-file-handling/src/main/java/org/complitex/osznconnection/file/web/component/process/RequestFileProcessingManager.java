/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.process.ProcessType;

/**
 *
 * @author Artem
 */
public final class RequestFileProcessingManager extends ProcessingManager<RequestFile> {

    private final ProcessType[] supportedProcessTypes;

    public RequestFileProcessingManager(ProcessType... supportedProcessTypes) {
        this.supportedProcessTypes = supportedProcessTypes;
    }

    @Override
    protected Set<ProcessType> getSupportedProcessTypes() {
        return ImmutableSet.copyOf(supportedProcessTypes);
    }

    @Override
    public boolean isProcessing(RequestFile object) {
        return object.isProcessing();
    }
}
