package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityStreetType extends AbstractRequest {
    public FacilityStreetType() {
    }

    public FacilityStreetType(Long requestFileId) {
        setRequestFileId(requestFileId);
    }

    public <T> T getField(FacilityStreetTypeDBF facilityStreetTypeDBF) {
        return getField(facilityStreetTypeDBF.name());
    }

    public String getStringField(FacilityStreetTypeDBF facilityStreetTypeDBF) {
        return dbfFields.get(facilityStreetTypeDBF.name());
    }

    @Override
    public RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.FACILITY_STREET_TYPE;
    }
}
