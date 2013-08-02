/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityTarif extends AbstractRequest {
    public FacilityTarif() {
    }

    public FacilityTarif(Long requestFileId) {
        setRequestFileId(requestFileId);
    }

    public <T> T getField(FacilityTarifDBF facilityTarifDBF) {
        return getField(facilityTarifDBF.name());
    }

    public String getStringField(FacilityTarifDBF facilityTarifDBF) {
        return dbfFields.get(facilityTarifDBF.name());
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_TARIF;
    }
}
