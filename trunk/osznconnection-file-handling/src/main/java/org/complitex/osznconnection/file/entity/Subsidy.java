package org.complitex.osznconnection.file.entity;

import java.util.List;

/**
 *
 * @author Artem
 */
public class Subsidy extends AbstractAccountRequest<SubsidyDBF> {
    private List<SubsidyMasterData> masterDataList;

    public List<SubsidyMasterData> getMasterDataList() {
        return masterDataList;
    }

    public void setMasterDataList(List<SubsidyMasterData> masterDataList) {
        this.masterDataList = masterDataList;
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.SUBSIDY;
    }

    @Override
    public String getCity() {
        return getStringField(SubsidyDBF.NP_NAME, "_CYR");
    }

    @Override
    public String getStreetType() {
        return getStringField(SubsidyDBF.CAT_V, "_CYR");
    }

    @Override
    public String getStreetCode() {
        return null; //code is not used for correction
    }

    @Override
    public String getStreet() {
        return getStringField(SubsidyDBF.NAME_V, "_CYR");
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(SubsidyDBF.BLD, "_CYR");
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(SubsidyDBF.CORP, "_CYR");
    }

    @Override
    public String getApartment() {
        return getStringField(SubsidyDBF.FLAT, "_CYR");
    }
}
