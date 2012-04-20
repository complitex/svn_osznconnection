/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.util.Map;

/**
 *
 * @author Artem
 */
public class ActualPayment extends AbstractRequest {

    public <T> T getField(ActualPaymentDBF actualPaymentDBF) {
        return getField(actualPaymentDBF.name());
    }

    public String getStringField(ActualPaymentDBF actualPaymentDBF) {
        return dbfFields.get(actualPaymentDBF.name());
    }

    public void setField(ActualPaymentDBF actualPaymentDBF, Object object) {
        setField(actualPaymentDBF.name(), object);
    }

    @Override
    protected RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.ACTUAL_PAYMENT;
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
    private Map<String, String> updateFieldMap;

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

    public Map<String, String> getUpdateFieldMap() {
        return updateFieldMap;
    }

    public void setUpdateFieldMap(Map<String, String> updateFieldMap) {
        this.updateFieldMap = updateFieldMap;
    }
}
