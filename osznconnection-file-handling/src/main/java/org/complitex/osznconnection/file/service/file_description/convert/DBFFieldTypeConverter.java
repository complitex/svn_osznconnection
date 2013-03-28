/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description.convert;

import com.linuxense.javadbf.DBFField;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class DBFFieldTypeConverter {

    public static Class<?> toJavaType(DBFField dbfField) {
        switch (dbfField.getDataType()) {
            case DBFField.FIELD_TYPE_C:
                return String.class;
            case DBFField.FIELD_TYPE_N:
                return dbfField.getDecimalCount() == 0 ? Long.class : BigDecimal.class;
            case DBFField.FIELD_TYPE_D:
                return Date.class;
            default:
                throw new IllegalArgumentException("Unexpected DBF field type. Field name: " + dbfField.getName()
                        + ", field type: " + dbfField.getDataType());
        }
    }

    public static byte toDBFType(String fieldName, Class<?> javaType) {
        if (javaType == String.class) {
            return DBFField.FIELD_TYPE_C;
        } else if (javaType == Long.class || javaType == BigDecimal.class) {
            return DBFField.FIELD_TYPE_N;
        } else if (javaType == Date.class) {
            return DBFField.FIELD_TYPE_D;
        }
        throw new IllegalArgumentException("Unexpected field's java type. Field name: " + fieldName
                + ", field java type: " + javaType);
    }

    private DBFFieldTypeConverter() {
    }
}
