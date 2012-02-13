/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongSizeException;

/**
 *
 * @author Artem
 */
public class ActualPayment extends AbstractRequest {

    public Object getField(ActualPaymentDBF actualPaymentDBF) {
        return dbfFields.get(actualPaymentDBF.name());
    }

    public void setField(ActualPaymentDBF actualPaymentDBF, Object object) {
        dbfFields.put(actualPaymentDBF.name(), object);
    }

    @Override
    protected Class getFieldType(String name) throws FieldNotFoundException {
        try {
            return ActualPaymentDBF.valueOf(name).getType();
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(name);
        }
    }

    @Override
    protected void checkSize(String name, Object value) throws FieldWrongSizeException {
        if (value == null || value instanceof Date) {
            return;
        }

        ActualPaymentDBF actualPaymentDBF = ActualPaymentDBF.valueOf(name);

        if (value instanceof BigDecimal) {
            if (((BigDecimal) value).scale() > actualPaymentDBF.getScale()) {
                throw new FieldWrongSizeException(value.toString());
            }
        }

        if (value.toString().length() > actualPaymentDBF.getLength()) {
            throw new FieldWrongSizeException(value.toString());
        }
    }
    private Long internalCityId;
    private Long internalStreetId;
    private Long internalStreetTypeId;
    private Long internalBuildingId;
    private String outgoingCity;
    private String outgoingDistrict;
    private String outgoingStreet;
    private String outgoingStreetType;
    private String outgoingBuildingNumber;
    private String outgoingBuildingCorp;
    private String outgoingApartment;
    private Map<String, Object> updateFieldMap;

    public Long getInternalBuildingId() {
        return internalBuildingId;
    }

    public void setInternalBuildingId(Long internalBuildingId) {
        this.internalBuildingId = internalBuildingId;
    }

    public Long getInternalCityId() {
        return internalCityId;
    }

    public void setInternalCityId(Long internalCityId) {
        this.internalCityId = internalCityId;
    }

    public Long getInternalStreetId() {
        return internalStreetId;
    }

    public void setInternalStreetId(Long internalStreetId) {
        this.internalStreetId = internalStreetId;
    }

    public Long getInternalStreetTypeId() {
        return internalStreetTypeId;
    }

    public void setInternalStreetTypeId(Long internalStreetTypeId) {
        this.internalStreetTypeId = internalStreetTypeId;
    }

    public String getOutgoingApartment() {
        return outgoingApartment;
    }

    public void setOutgoingApartment(String outgoingApartment) {
        this.outgoingApartment = outgoingApartment;
    }

    public String getOutgoingBuildingCorp() {
        return outgoingBuildingCorp;
    }

    public void setOutgoingBuildingCorp(String outgoingBuildingCorp) {
        this.outgoingBuildingCorp = outgoingBuildingCorp;
    }

    public String getOutgoingBuildingNumber() {
        return outgoingBuildingNumber;
    }

    public void setOutgoingBuildingNumber(String outgoingBuildingNumber) {
        this.outgoingBuildingNumber = outgoingBuildingNumber;
    }

    public String getOutgoingCity() {
        return outgoingCity;
    }

    public void setOutgoingCity(String outgoingCity) {
        this.outgoingCity = outgoingCity;
    }

    public String getOutgoingStreet() {
        return outgoingStreet;
    }

    public void setOutgoingStreet(String outgoingStreet) {
        this.outgoingStreet = outgoingStreet;
    }

    public String getOutgoingStreetType() {
        return outgoingStreetType;
    }

    public void setOutgoingStreetType(String outgoingStreetType) {
        this.outgoingStreetType = outgoingStreetType;
    }

    public String getOutgoingDistrict() {
        return outgoingDistrict;
    }

    public void setOutgoingDistrict(String outgoingDistrict) {
        this.outgoingDistrict = outgoingDistrict;
    }

    public Map<String, Object> getUpdateFieldMap() {
        return updateFieldMap;
    }

    public void setUpdateFieldMap(Map<String, Object> updateFieldMap) {
        this.updateFieldMap = updateFieldMap;
    }
}
