package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 13:15:40
 */
public class AbstractRequest implements Serializable {
    private Long id;
    private Long requestFileId;
    private Long organizationId;
    private String accountNumber;
    private Status status;
    
    protected Map<String, Object> dbfFields= new HashMap<String, Object>();

    private Long cityId;
    private Long streetId;
    private Long buildingId;
    private Long apartmentId;
    private String internalCity;
    private String internalStreet;
    private String internalBuilding;
    private String internalApartment;

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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Object> getDbfFields() {
        return dbfFields;
    }

    public void setDbfFields(Map<String, Object> dbfFields) {
        this.dbfFields = dbfFields;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getStreetId() {
        return streetId;
    }

    public void setStreetId(Long streetId) {
        this.streetId = streetId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getInternalCity() {
        return internalCity;
    }

    public void setInternalCity(String internalCity) {
        this.internalCity = internalCity;
    }

    public String getInternalStreet() {
        return internalStreet;
    }

    public void setInternalStreet(String internalStreet) {
        this.internalStreet = internalStreet;
    }

    public String getInternalBuilding() {
        return internalBuilding;
    }

    public void setInternalBuilding(String internalBuilding) {
        this.internalBuilding = internalBuilding;
    }

    public String getInternalApartment() {
        return internalApartment;
    }

    public void setInternalApartment(String internalApartment) {
        this.internalApartment = internalApartment;
    }
}