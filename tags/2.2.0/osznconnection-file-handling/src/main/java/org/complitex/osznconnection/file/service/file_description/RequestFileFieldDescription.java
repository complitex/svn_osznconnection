/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class RequestFileFieldDescription implements Serializable {

    private long id;
    private Long requestFileDescriptionId;
    private final String name;
    private final String type;
    private final int length;
    private final Integer scale;
    private final Class<?> fieldType;

    public RequestFileFieldDescription(String name, String type, int length, Integer scale) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.scale = scale;

        //integer -> long
        if (Integer.class.getName().equals(type)){
            type = Long.class.getName();
        }

        try {
            fieldType = Thread.currentThread().getContextClassLoader().loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public Integer getScale() {
        return scale;
    }

    public String getType() {
        return type;
    }

    public Long getRequestFileDescriptionId() {
        return requestFileDescriptionId;
    }

    public void setRequestFileDescriptionId(Long requestFileDescriptionId) {
        this.requestFileDescriptionId = requestFileDescriptionId;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }
}
