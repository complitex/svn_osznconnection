/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class FacilityForm2 extends AbstractRequest {

    public <T> T getField(FacilityForm2DBF facilityForm2DBF) {
        return getField(facilityForm2DBF.name());
    }

    public String getStringField(FacilityForm2DBF facilityForm2DBF) {
        return dbfFields.get(facilityForm2DBF.name());
    }

    public void setField(FacilityForm2DBF facilityForm2DBF, Object object) {
        setField(facilityForm2DBF.name(), object);
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_FORM2;
    }
    private String lastName;
    private String firstName;
    private String middleName;

    public FacilityForm2() {
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
}
