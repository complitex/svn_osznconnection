package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.exception.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.05.11 18:51
 */
public class CanceledByUserException extends AbstractException{
    public CanceledByUserException() {
        super("Процесс остановлен пользователем");
    }
}
