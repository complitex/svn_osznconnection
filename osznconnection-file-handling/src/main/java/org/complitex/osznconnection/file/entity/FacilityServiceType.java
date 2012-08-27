/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class FacilityServiceType extends AbstractRequest {

    public <T> T getField(FacilityServiceTypeDBF facilityServiceTypeDBF) {
        return getField(facilityServiceTypeDBF.name());
    }

    public String getStringField(FacilityServiceTypeDBF facilityServiceTypeDBF) {
        return dbfFields.get(facilityServiceTypeDBF.name());
    }

    public void setField(FacilityServiceTypeDBF facilityServiceTypeDBF, Object object) {
        setField(facilityServiceTypeDBF.name(), object);
    }

    @Override
    public RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.FACILITY_SERVICE_TYPE;
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
    private Long streetCorrectionId;
    private Map<String, String> updateFieldMap;
    private Date date;
    private String city;
    private String street;
    private String streetType;
    private String lastName;
    private String firstName;
    private String middleName;

    public FacilityServiceType() {
    }

    public FacilityServiceType(String city, Date date) {
        this.city = city;
        this.date = date;
    }

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public Long getStreetCorrectionId() {
        return streetCorrectionId;
    }

    public void setStreetCorrectionId(Long streetCorrectionId) {
        this.streetCorrectionId = streetCorrectionId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
