package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Запись в локальной таблице номеров л/c person_account
 */
public class PersonAccount implements Serializable {
    private Long id;

    private String firstName;
    private String middleName;
    private String lastName;

    private Long cityObjectId;
    private Long streetTypeObjectId;
    private Long streetObjectId;
    private Long buildingObjectId;
    private Long apartmentObjectId;

    private String apartment;

    private Long organizationId;
    private Long userOrganizationId;
    private Long calculationCenterId;

    private String accountNumber;
    private String puAccountNumber;

    private String organizationName;
    private String userOrganizationName;
    private String calculationCenterName;

    public PersonAccount() {
    }

    public PersonAccount(AbstractAccountRequest request, String puAccountNumber, Long calculationCenterId) {
        firstName = request.getFirstName();
        middleName = request.getMiddleName();
        lastName = request.getLastName();

        cityObjectId = request.getCityObjectId();
        streetObjectId = request.getStreetObjectId();
        streetTypeObjectId = request.getStreetTypeObjectId();
        buildingObjectId = request.getBuildingObjectId();
        apartmentObjectId = request.getApartmentObjectId();

        apartment = request.getApartment();

        this.accountNumber = request.getAccountNumber();
        organizationId = request.getOrganizationId();
        userOrganizationId = request.getUserOrganizationId();

        this.puAccountNumber = puAccountNumber;
        this.calculationCenterId = calculationCenterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getCityObjectId() {
        return cityObjectId;
    }

    public void setCityObjectId(Long cityObjectId) {
        this.cityObjectId = cityObjectId;
    }

    public Long getStreetObjectId() {
        return streetObjectId;
    }

    public void setStreetObjectId(Long streetObjectId) {
        this.streetObjectId = streetObjectId;
    }

    public Long getStreetTypeObjectId() {
        return streetTypeObjectId;
    }

    public void setStreetTypeObjectId(Long streetTypeObjectId) {
        this.streetTypeObjectId = streetTypeObjectId;
    }

    public Long getBuildingObjectId() {
        return buildingObjectId;
    }

    public void setBuildingObjectId(Long buildingObjectId) {
        this.buildingObjectId = buildingObjectId;
    }

    public Long getApartmentObjectId() {
        return apartmentObjectId;
    }

    public void setApartmentObjectId(Long apartmentObjectId) {
        this.apartmentObjectId = apartmentObjectId;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
    }

    public Long getCalculationCenterId() {
        return calculationCenterId;
    }

    public void setCalculationCenterId(Long calculationCenterId) {
        this.calculationCenterId = calculationCenterId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getPuAccountNumber() {
        return puAccountNumber;
    }

    public void setPuAccountNumber(String puAccountNumber) {
        this.puAccountNumber = puAccountNumber;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getUserOrganizationName() {
        return userOrganizationName;
    }

    public void setUserOrganizationName(String userOrganizationName) {
        this.userOrganizationName = userOrganizationName;
    }

    public String getCalculationCenterName() {
        return calculationCenterName;
    }

    public void setCalculationCenterName(String calculationCenterName) {
        this.calculationCenterName = calculationCenterName;
    }
}
