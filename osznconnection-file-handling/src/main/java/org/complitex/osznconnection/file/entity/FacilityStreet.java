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
}
