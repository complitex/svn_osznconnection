package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;
import org.complitex.dictionary.entity.ILongId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 13:15:40
 *
 * Абстрактное представление записи файла запроса.
 * Используется <code>Map<String, Object></code> для хранения названий и значений полей.
 */
public abstract class AbstractRequest<E extends Enum> implements ILongId{
    private Long id;
    private Long requestFileId;
    private Long organizationId;
    private Long userOrganizationId;

    private RequestStatus status;

    private List<RequestWarning> warnings = Lists.newArrayList();
    private Map<String, Object> dbfFields = new HashMap<>();

    private Map<String, Object> updateFieldMap;

    public Map<String, Object> getUpdateFieldMap() {
        return updateFieldMap;
    }

    public void setUpdateFieldMap(Map<String, Object> updateFieldMap) {
        this.updateFieldMap = updateFieldMap;
    }

    public Object getField(String fieldName) {
        return dbfFields.get(fieldName);
    }

    public void setField(String fieldName, Object object) {
        dbfFields.put(fieldName, object);
    }

    public Object getField(E e) {
        return getField(e.name());
    }

    public String getStringField(E e) {
        Object o = dbfFields.get(e.name());

        return o != null ? o.toString() : null;
    }

    public void setField(E e, Object object) {
        setField(e.name(), object);
    }

    public abstract RequestFileType getRequestFileType();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestFileId() {
        return requestFileId;
    }

    public void setRequestFileId(Long requestFileId) {
        this.requestFileId = requestFileId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Map<String, Object> getDbfFields() {
        return dbfFields;
    }

    public void setDbfFields(Map<String, Object> dbfFields) {
        this.dbfFields = dbfFields;
    }

    public List<RequestWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<RequestWarning> warnings) {
        this.warnings = warnings;
    }
}
