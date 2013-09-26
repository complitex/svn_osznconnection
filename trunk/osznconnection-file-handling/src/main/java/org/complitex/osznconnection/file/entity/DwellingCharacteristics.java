package org.complitex.osznconnection.file.entity;

import java.util.Date;

public class DwellingCharacteristics extends AbstractAccountRequest<DwellingCharacteristicsDBF> {
    public DwellingCharacteristics() {
    }

    public DwellingCharacteristics(String city, Date date) {
        setCity(city);
        setDate(date);
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.DWELLING_CHARACTERISTICS;
    }

    @Override
    public String getStreetCode() {
        return getStringField(DwellingCharacteristicsDBF.CDUL);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(DwellingCharacteristicsDBF.HOUSE);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(DwellingCharacteristicsDBF.BUILD);
    }

    @Override
    public String getApartment() {
        return getStringField(DwellingCharacteristicsDBF.APT);
    }
}
