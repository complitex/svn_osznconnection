package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionary.service.exception.AbstractException;

public class MoreOneAccountException extends AbstractException {
    public MoreOneAccountException() {
        super("Найдено более одного лицевого счета");
    }
}
