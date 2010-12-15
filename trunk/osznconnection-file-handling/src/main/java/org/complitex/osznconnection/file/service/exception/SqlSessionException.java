package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 19:50:39
 */
public class SqlSessionException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Ошибка сохранения в базу данных";

    public SqlSessionException(Throwable cause) {
        super(true, cause, MESSAGE_PATTERN);
    }
}
