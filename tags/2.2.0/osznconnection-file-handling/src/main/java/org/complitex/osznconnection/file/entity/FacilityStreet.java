package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityStreet extends AbstractAddressRequest<FacilityStreetDBF> {
    public FacilityStreet() {
    }

    public FacilityStreet(Long requestFileId) {
        setRequestFileId(requestFileId);
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_STREET;
    }
}
