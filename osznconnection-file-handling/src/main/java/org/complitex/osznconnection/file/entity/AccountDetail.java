/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Класс хранящий детальную информацию о клиентах ЦН(л/c, ФИО, ИНН).
 * @author Artem
 */
public class AccountDetail implements Serializable {

    private String accountNumber;
    private String ownerName;
    private String ownerINN;
    private String megabankAccountNumber;
    private String serviceProviderAccountNumberInfo;
    private String serviceProviderCode;
    private String serviceProviderAccountNumber;
    private String street;
    private String streetType;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerINN() {
        return ownerINN;
    }

    public void setOwnerINN(String ownerINN) {
        this.ownerINN = ownerINN;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public String getMegabankAccountNumber() {
        return megabankAccountNumber;
    }

    public void setMegabankAccountNumber(String megabankAccountNumber) {
        this.megabankAccountNumber = megabankAccountNumber;
    }

    public String getServiceProviderAccountNumberInfo() {
        return serviceProviderAccountNumberInfo;
    }

    public void setServiceProviderAccountNumberInfo(String serviceProviderAccountNumberInfo) {
        this.serviceProviderAccountNumberInfo = serviceProviderAccountNumberInfo;
    }

    public String getServiceProviderCode() {
        return serviceProviderCode;
    }

    public void setServiceProviderCode(String serviceProviderCode) {
        this.serviceProviderCode = serviceProviderCode;
    }

    public String getServiceProviderAccountNumber() {
        return serviceProviderAccountNumber;
    }

    public void setServiceProviderAccountNumber(String serviceProviderAccountNumber) {
        this.serviceProviderAccountNumber = serviceProviderAccountNumber;
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

    @Override
    public String toString() {
        return "{ Account : " + accountNumber + ", owner name : " + ownerName + ", owner INN : " + ownerINN
                + ", megabankAccountNumber : " + megabankAccountNumber + ", puAccountNumberInfo : " + serviceProviderAccountNumberInfo
                + ", street type : " + streetType + ", street : " + street + ", building number : "
                + buildingNumber + ", building corp : " + buildingCorp + ", apartment : " + apartment + "}";
    }
}
