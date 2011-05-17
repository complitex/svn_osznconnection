package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.exception.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.05.11 13:43
 */
public class EmptyFileException extends AbstractException {
    public EmptyFileException() {
        super("Пустой файл");
    }
}
