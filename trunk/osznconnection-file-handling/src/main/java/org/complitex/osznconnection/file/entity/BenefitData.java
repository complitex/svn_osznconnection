package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.10.2010 17:11:01
 */
public class BenefitData implements Serializable {

    private String firstName;
    private String lastName;
    private String middleName;
    private String inn;
    private String passportSerial;
    private String passportNumber;
    private String orderFamily;
    private String code;
    private String userCount;
    private Long calcCenterId;
    private Long privilegeObjectId;
    private String osznPrivilegeCode;

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

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getPassportSerial() {
        return passportSerial;
    }

    public void setPassportSerial(String passportSerial) {
        this.passportSerial = passportSerial;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getOrderFamily() {
        return orderFamily;
    }

    public void setOrderFamily(String orderFamily) {
        this.orderFamily = orderFamily;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserCount() {
        return userCount;
    }

    public void setUserCount(String userCount) {
        this.userCount = userCount;
    }

    public String getOsznPrivilegeCode() {
        return osznPrivilegeCode;
    }

    public void setOsznPrivilegeCode(String osznBenefitCode) {
        this.osznPrivilegeCode = osznBenefitCode;
    }

    public Long getPrivilegeObjectId() {
        return privilegeObjectId;
    }

    public void setPrivilegeObjectId(Long privilegeObjectId) {
        this.privilegeObjectId = privilegeObjectId;
    }

    public Long getCalcCenterId() {
        return calcCenterId;
    }

    public void setCalcCenterId(Long calcCenterId) {
        this.calcCenterId = calcCenterId;
    }

    @Override
    public String toString() {
        return "first name = " + firstName + ", middle name = " + middleName + ", last name = " + lastName + ", Inn = " + inn
                + ", passport serial = " + passportSerial + ", passport number = " + passportNumber + ", benefit code = " + code
                + ", order fam = " + orderFamily + ", user count = " + userCount;
    }
}
