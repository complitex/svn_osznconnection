/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Запись в локальной таблице номеров л/c person_account.
 * @author Artem
 */
public class PersonAccount implements Serializable {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String city;
    private String streetType;
    private String street;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;
    private String accountNumber;
    private Long osznId;
    private Long calculationCenterId;
    private String oszn;
    private String calculationCenter;
    private boolean admin;
    private String organizations;
    private String puAccountNumber;

    public PersonAccount() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Long getCalculationCenterId() {
        return calculationCenterId;
    }

    public void setCalculationCenterId(Long calculationCenterId) {
        this.calculationCenterId = calculationCenterId;
    }

    public Long getOsznId() {
        return osznId;
    }

    public void setOsznId(Long osznId) {
        this.osznId = osznId;
    }

    public String getCalculationCenter() {
        return calculationCenter;
    }

    public void setCalculationCenter(String calculationCenter) {
        this.calculationCenter = calculationCenter;
    }

    public String getOszn() {
        return oszn;
    }

    public void setOszn(String oszn) {
        this.oszn = oszn;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getOrganizations() {
        return organizations;
    }

    public void setOrganizations(String organizations) {
        this.organizations = organizations;
    }

    public String getPuAccountNumber() {
        return puAccountNumber;
    }

    public void setPuAccountNumber(String puAccountNumber) {
        this.puAccountNumber = puAccountNumber;
    }
}
