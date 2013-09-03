package org.complitex.osznconnection.file.entity;

import java.util.Date;

public class FacilityServiceType extends AbstractAccountRequest<FacilityServiceTypeDBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_SERVICE_TYPE;
    }

    public FacilityServiceType() {
    }

    public FacilityServiceType(String city, Date date) {
        setCity(city);
        setDate(date);
    }

    @Override
    public String getStreetCode() {
        return getStringField(FacilityServiceTypeDBF.CDUL);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(FacilityServiceTypeDBF.BUILD);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(FacilityServiceTypeDBF.HOUSE);
    }
}
