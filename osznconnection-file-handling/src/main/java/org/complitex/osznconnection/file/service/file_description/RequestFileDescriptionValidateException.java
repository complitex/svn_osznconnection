/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Artem
 */
public class RequestFileDescriptionValidateException extends Exception {

    public static class ValidationError implements Serializable {

        private final String errorMessage;

        public ValidationError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
    private final List<ValidationError> errors;

    public RequestFileDescriptionValidateException(List<ValidationError> errors) {
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
