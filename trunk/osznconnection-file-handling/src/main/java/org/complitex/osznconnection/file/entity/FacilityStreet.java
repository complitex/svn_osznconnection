/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityStreet extends AbstractRequest {
    private String streetType;
    private String streetTypeCode;

    public FacilityStreet() {
    }

    public FacilityStreet(Long requestFileId) {
        setRequestFileId(requestFileId);
    }

    public <T> T getField(FacilityStreetDBF facilityStreetDBF) {
        return getField(facilityStreetDBF.name());
    }

    public String getStringField(FacilityStreetDBF facilityStreetDBF) {
        return dbfFields.get(facilityStreetDBF.name());
    }

    @Override
    public RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.FACILITY_STREET;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getStreetTypeCode() {
        return streetTypeCode;
    }

    public void setStreetTypeCode(String streetTypeCode) {
        this.streetTypeCode = streetTypeCode;
    }
}
