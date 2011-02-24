package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.exception.AbstractException;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.01.11 15:37
 */
public class AlreadyProcessingException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Файл {0} уже обрабатывается";

    public AlreadyProcessingException(RequestFile rf) {
        super(MESSAGE_PATTERN, rf.getFullName());
    }

    public AlreadyProcessingException(RequestFileGroup rfg) {
        super(MESSAGE_PATTERN, rfg.getFullName());
    }
}

