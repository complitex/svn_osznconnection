package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class Subsidy extends AbstractAccountRequest<SubsidyDBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.SUBSIDY;
    }

    @Override
    public String getCity() {
        return getStringField(SubsidyDBF.NP_NAME);
    }

    @Override
    public String getStreetType() {
        return getStringField(SubsidyDBF.CAT_V);
    }

    @Override
    public String getStreetCode() {
        return getStringField(SubsidyDBF.VULCOD);
    }

    @Override
    public String getStreet() {
        return getStringField(SubsidyDBF.NAME_V);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(SubsidyDBF.BLD);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(SubsidyDBF.CORP);
    }

    @Override
    public String getApartment() {
        return getStringField(SubsidyDBF.FLAT);
    }
}
