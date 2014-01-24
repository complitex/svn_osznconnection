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
        return getStringField(SubsidyDBF.NP_NAME_CYR);
    }

    @Override
    public String getStreetType() {
        return getStringField(SubsidyDBF.CAT_V_CYR);
    }

    @Override
    public String getStreetCode() {
        return getStringField(SubsidyDBF.VULCOD);
    }

    @Override
    public String getStreet() {
        return getStringField(SubsidyDBF.NAME_V_CYR);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(SubsidyDBF.BLD_CYR);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(SubsidyDBF.CORP_CYR);
    }

    @Override
    public String getApartment() {
        return getStringField(SubsidyDBF.FLAT_CYR);
    }
}
