package org.complitex.osznconnection.file.entity;

import org.complitex.address.util.AddressRenderer;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class Subsidy extends AbstractAccountRequest<SubsidyDBF> {
    private List<SubsidyMasterData> masterDataList;

    public String getAddress(Locale locale){
        return AddressRenderer.displayAddress(getStreetType(), getStreet(), getBuildingNumber(), getBuildingCorp(),
                getApartment(), locale);
    }

    public List<SubsidyMasterData> getMasterDataList() {
        return masterDataList;
    }

    public void setMasterDataList(List<SubsidyMasterData> masterDataList) {
        this.masterDataList = masterDataList;
    }

    public String getFio(){
        return getStringField(SubsidyDBF.FIO, "_CYR");
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
