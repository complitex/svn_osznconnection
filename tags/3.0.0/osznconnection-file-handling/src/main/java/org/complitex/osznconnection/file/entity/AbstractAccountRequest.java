package org.complitex.osznconnection.file.entity;

import java.util.Date;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 15.08.13 21:00
 */
public abstract class AbstractAccountRequest<E extends Enum> extends AbstractAddressRequest<E> {
    private String accountNumber;

    private String lastName;
    private String firstName;
    private String middleName;

    private Date date;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
