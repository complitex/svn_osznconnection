/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description.convert;

/**
 *
 * @author Artem
 */
public class ConversionException extends Exception {

    public ConversionException(Throwable cause) {
        super(cause);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException() {
    }
}
