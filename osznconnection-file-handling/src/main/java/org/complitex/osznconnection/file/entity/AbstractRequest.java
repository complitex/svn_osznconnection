package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.complitex.osznconnection.file.service.file_description.convert.ConversionException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 13:15:40
 *
 * Абстрактное представление записи файла запроса.
 * Используется <code>Map<String, Object></code> для хранения названий и значений полей.
 */
public abstract class AbstractRequest implements Serializable {

    private Long id;
    private Long requestFileId;
    private Long organizationId;
    private RequestStatus status;
    private String accountNumber;
    private List<RequestWarning> warnings = Lists.newArrayList();
    protected Map<String, String> dbfFields = new HashMap<String, String>();

    @SuppressWarnings({"unchecked"})
    protected <T> T getField(String fieldName) {
        final String stringValue = dbfFields.get(fieldName);
        final RequestFileDescription description = getDescription();
        final RequestFileFieldDescription fieldDescription = description.getField(fieldName);
        if (fieldDescription == null) {
            throw new IllegalStateException("Couldn't find field description. "
                    + "Request file type: " + getRequestFileType().name() + ", request id: '" + getId()
                    + "', field name: '" + fieldName + "'.");
        }
        final Class<?> expectedType = fieldDescription.getFieldType();
        try {
            return (T) description.getTypeConverter().toObject(stringValue, expectedType);
        } catch (ConversionException e) {
            throw new IllegalStateException("Couldn't perform type conversion. "
                    + "Request file type: " + getRequestFileType().name() + ", request id: '" + getId()
                    + "', field name: '" + fieldName
                    + "', string value of field: '" + stringValue
                    + "', expected java type a field value to be converted to: " + expectedType+".", e);
        }
    }

    protected void setField(String fieldName, Object object) {
        final RequestFileDescription description = getDescription();
        dbfFields.put(fieldName, description.getTypeConverter().toString(object));
    }

    protected RequestFileDescription getDescription() {
        RequestFileDescriptionBean requestFileDescriptionBean = EjbBeanLocator.getBean(RequestFileDescriptionBean.class);
        return requestFileDescriptionBean.getFileDescription(getRequestFileType());
    }

    public abstract RequestFile.TYPE getRequestFileType();

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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Map<String, String> getDbfFields() {
        return dbfFields;
    }

    public void setDbfFields(Map<String, String> dbfFields) {
        this.dbfFields = dbfFields;
    }

    public List<RequestWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<RequestWarning> warnings) {
        this.warnings = warnings;
    }
}
