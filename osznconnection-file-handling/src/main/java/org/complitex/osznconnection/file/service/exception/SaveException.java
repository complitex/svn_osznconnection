package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 16:45:30
 */
public class SaveException extends ExecuteException {
    private final static String MESSAGE_PATTERN = "Ошибка выгрузки файла {0}";

    public SaveException(Throwable cause, RequestFile requestFile) {
        super(cause, MESSAGE_PATTERN, requestFile.getAbsolutePath());
    }

     public SaveException(Throwable cause, boolean warn, RequestFile requestFile) {
        super(cause, warn, MESSAGE_PATTERN, requestFile.getFullName());
    }

    public SaveException(Throwable cause, boolean warn, RequestFileGroup group) {
        super(cause, warn, MESSAGE_PATTERN, group.getFullName());
    }
}
