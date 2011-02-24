/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter.exception;

import org.complitex.dictionary.service.exception.AbstractException;

/**
 *
 * @author Artem
 */
public class DBException extends AbstractException {

    private static String MESSAGE_PATTERN = "Ошибка доступа к центру начислений";

    public DBException(Throwable cause) {
        super(cause, MESSAGE_PATTERN);
    }
}
