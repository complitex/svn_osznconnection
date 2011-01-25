package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 18:45
 */
public class BindException extends ExecuteException{
    private final static String MESSAGE_PATTERN = "Ошибка связывания файла запроса {0}";

    public BindException(boolean warn, RequestFile requestFile) {
        super(warn, MESSAGE_PATTERN, requestFile.getFullName());
    }

    public BindException(Throwable cause, boolean warn, RequestFile requestFile) {
        super(cause, warn, MESSAGE_PATTERN, requestFile.getFullName());
    }

    public BindException(Throwable cause, boolean warn, RequestFileGroup group) {
        super(cause, warn, MESSAGE_PATTERN, group.getFullName());
    }
}
