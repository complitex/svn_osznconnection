package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityForm2 extends AbstractAccountRequest<FacilityForm2DBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_FORM2;
    }
}
