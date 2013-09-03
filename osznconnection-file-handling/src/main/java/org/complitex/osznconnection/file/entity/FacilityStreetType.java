package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityStreetType extends AbstractRequest<FacilityStreetTypeDBF> {
    public FacilityStreetType() {
    }

    public FacilityStreetType(Long requestFileId) {
        setRequestFileId(requestFileId);
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_STREET_TYPE;
    }
}
