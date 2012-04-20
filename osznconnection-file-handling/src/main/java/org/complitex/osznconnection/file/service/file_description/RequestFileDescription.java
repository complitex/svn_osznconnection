/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.file_description.convert.RequestFileTypeConverter;

/**
 *
 * @author Artem
 */
public class RequestFileDescription implements Serializable {

    private Long id;
    private final String fileType;
    private final String datePattern;
    private final RequestFileTypeConverter typeConverter;
    private List<RequestFileFieldDescription> fields = Lists.newArrayList();

    public RequestFileDescription(String fileType, String datePattern) {
        this.fileType = fileType;
        this.datePattern = datePattern;
        this.typeConverter = new RequestFileTypeConverter(datePattern);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileType() {
        return fileType;
    }

    public RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.valueOf(fileType);
    }

    public List<RequestFileFieldDescription> getFields() {
        return fields;
    }

    public void setFields(List<RequestFileFieldDescription> fields) {
        this.fields = fields;
    }

    public void addField(RequestFileFieldDescription field) {
        this.fields.add(field);
    }

    public RequestFileFieldDescription getField(String fieldName) {
        for (RequestFileFieldDescription field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public RequestFileTypeConverter getTypeConverter() {
        return typeConverter;
    }
}
