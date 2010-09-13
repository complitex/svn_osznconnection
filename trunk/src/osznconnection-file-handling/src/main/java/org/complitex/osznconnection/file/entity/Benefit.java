package org.complitex.osznconnection.file.entity;

import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 */
public class Benefit extends AbstractRequest {

    public Object getField(BenefitDBF benefitDBF) {
        return dbfFields.get(benefitDBF.name());
    }

    public void setField(BenefitDBF benefitDBF, Object object) {
        dbfFields.put(benefitDBF.name(), object);
    }

    @Override
    protected Class getFieldType(String name) throws FieldNotFoundException {
        try {
            return BenefitDBF.valueOf(name).getType();
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(e);
        }
    }

    private String city;
    private String street;
    private String building;
    private String apartment;

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}