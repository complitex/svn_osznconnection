package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.IExecutorObject;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.02.14 1:13
 */
public abstract class AbstractExecutorObject implements IExecutorObject {
    private boolean cancel = false;
    private String errorMessage;

    @Override
    public void cancel() {
        cancel = true;
    }

    @Override
    public boolean isCanceled() {
        return cancel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
