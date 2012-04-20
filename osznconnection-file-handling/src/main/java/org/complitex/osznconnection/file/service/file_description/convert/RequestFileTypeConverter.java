/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description.convert;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Artem
 */
public final class RequestFileTypeConverter {

    private final DateFormat dateFormat;

    public RequestFileTypeConverter(String datePattern) {
        this.dateFormat = new SimpleDateFormat(datePattern);
    }

    public String toString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Integer) {
            return value.toString();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (value instanceof Date) {
            return dateFormat.format((Date) value);
        }
        throw new IllegalStateException("Couldn't transform object value to string. Value type: " + value.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T toObject(String value, Class<T> type) throws ConversionException {
        try {
            if (value == null) {
                return null;
            }
            if (type == String.class) {
                return (T) value;
            }
            if (type == Integer.class) {
                return (T) Integer.valueOf(value);
            }
            if (type == BigDecimal.class) {
                return (T) new BigDecimal(value);
            }
            if (type == Date.class) {
                return (T) dateFormat.parse(value);
            }
            throw new IllegalStateException("Couldn't transform string to object value. Type: " + type);
        } catch (NumberFormatException e) {
            throw new ConversionException(e);
        } catch (ParseException e) {
            throw new ConversionException(e);
        }
    }
}
