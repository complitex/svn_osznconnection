package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Класс хранящий детальную информацию о клиентах ЦН(л/c, ФИО, ИНН).
 * @author Artem
 */
public class AccountDetail implements Serializable {
    private String accCode;
    private String ownerFio;
    private String ownerINN;
    private String ercCode;
    private String serviceProviderAccountNumberInfo;
    private String zheu;
    private String zheuCode;
    private String street;
    private String streetType;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;
    private String houseCode;
    private String districtCode;

    public String getAccCode() {
        return accCode;
    }

    public void setAccCode(String accCode) {
        this.accCode = accCode;
    }

    public String getOwnerINN() {
        return ownerINN;
    }

    public void setOwnerINN(String ownerINN) {
        this.ownerINN = ownerINN;
    }

    public String getOwnerFio() {
        return ownerFio;
    }

    public void setOwnerFio(String ownerFio) {
        this.ownerFio = ownerFio;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getBuildingCorp() {
        return buildingCorp;
    }

    public void setBuildingCorp(String buildingCorp) {
        this.buildingCorp = buildingCorp;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getErcCode() {
        return ercCode;
    }

    public void setErcCode(String ercCode) {
        this.ercCode = ercCode;
    }

    public String getServiceProviderAccountNumberInfo() {
        return serviceProviderAccountNumberInfo;
    }

    public void setServiceProviderAccountNumberInfo(String serviceProviderAccountNumberInfo) {
        this.serviceProviderAccountNumberInfo = serviceProviderAccountNumberInfo;
    }

    public String getZheu() {
        return zheu;
    }

    public void setZheu(String zheu) {
        this.zheu = zheu;
    }

    public String getZheuCode() {
        return zheuCode;
    }

    public void setZheuCode(String zheuCode) {
        this.zheuCode = zheuCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getHouseCode() {
        return houseCode;
    }

    public void setHouseCode(String houseCode) {
        this.houseCode = houseCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    @Override
    public String toString() {
        return "{ Account : " + accCode + ", owner name : " + ownerFio + ", owner INN : " + ownerINN
                + ", ercCode : " + ercCode + ", puAccountNumberInfo : " + serviceProviderAccountNumberInfo
                + ", street type : " + streetType + ", street : " + street + ", building number : "
                + buildingNumber + ", building corp : " + buildingCorp + ", apartment : " + apartment + "}";
    }
}
