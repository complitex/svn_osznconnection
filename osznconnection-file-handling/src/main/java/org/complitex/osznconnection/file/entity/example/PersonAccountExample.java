/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity.example;

import org.complitex.osznconnection.file.entity.PersonAccount;

/**
 *
 * @author Artem
 */
public class PersonAccountExample extends PersonAccount {

    private int start;
    private int size;
    private String orderByClause;
    private boolean asc;
    private Long localeId;
    private boolean admin;
    private String outerOrganizationsString;
    private String userOrganizationsString;

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public Long getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Long localeId) {
        this.localeId = localeId;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getOuterOrganizationsString() {
        return outerOrganizationsString;
    }

    public void setOuterOrganizationsString(String outerOrganizationsString) {
        this.outerOrganizationsString = outerOrganizationsString;
    }

    public String getUserOrganizationsString() {
        return userOrganizationsString;
    }

    public void setUserOrganizationsString(String userOrganizationsString) {
        this.userOrganizationsString = userOrganizationsString;
    }

    @Override
    public String getApartment() {
        return (super.getApartment() != null )?super.getApartment().toUpperCase():null;
    }

    @Override
    public String getBuildingCorp() {
        return (super.getBuildingCorp() != null )?super.getBuildingCorp().toUpperCase():null;
    }

    @Override
    public String getBuildingNumber() {
        return (super.getBuildingNumber() != null )?super.getBuildingNumber().toUpperCase():null;
    }

    @Override
    public String getCity() {
        return (super.getCity() != null )?super.getCity().toUpperCase():null;
    }

    @Override
    public String getFirstName() {
        return (super.getFirstName() != null )?super.getFirstName().toUpperCase():null;
    }

    @Override
    public String getLastName() {
        return (super.getLastName() != null )?super.getLastName().toUpperCase():null;
    }

    @Override
    public String getMiddleName() {
        return (super.getMiddleName() != null )?super.getMiddleName().toUpperCase():null;
    }

    @Override
    public String getStreet() {
        return (super.getStreet() != null )?super.getStreet().toUpperCase():null;
    }

    @Override
    public String getStreetType() {
        return (super.getStreetType() != null )?super.getStreetType().toUpperCase():null;
    }
}
