package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.service.executor.ExecuteException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 16:45:30
 */
public class SaveException extends ExecuteException {
    private final static String MESSAGE_PATTERN = "Ошибка выгрузки файла {0}";

    public SaveException(Throwable cause, IExecutorObject requestFile) {
        super(cause, MESSAGE_PATTERN, requestFile.getObjectName());
    }

     public SaveException(Throwable cause, boolean warn, IExecutorObject requestFile) {
        super(cause, warn, MESSAGE_PATTERN, requestFile.getObjectName());
    }
}
