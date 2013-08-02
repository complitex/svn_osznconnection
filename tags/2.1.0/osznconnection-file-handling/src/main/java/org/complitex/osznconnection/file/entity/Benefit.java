package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 *
 * Запись файла запроса возмещения по льготам.
 * @see org.complitex.osznconnection.file.entity.AbstractRequest
 *
 * Имена полей фиксированы в <code>Enum<code> перечислении <code>BenefitDBF</code>
 * @see org.complitex.osznconnection.file.entity.BenefitDBF
 */
public class Benefit extends AbstractRequest {

    /**
     * Возвращает значение поля по перечислению <code>BenefitDBF</code>
     * @param benefitDBF константа поля
     * @return значение поля
     */
    public <T> T getField(BenefitDBF benefitDBF) {
        return getField(benefitDBF.name());
    }

    public String getStringField(BenefitDBF benefitDBF) {
        return dbfFields.get(benefitDBF.name());
    }

    /**
     * Устанавливает значение поля по перечислению <code>BenefitDBF</code>
     * @param benefitDBF константа поля
     * @param object значение
     */
    public void setField(BenefitDBF benefitDBF, Object object) {
        setField(benefitDBF.name(), object);
    }

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.BENEFIT;
    }
    private String city;
    private String street;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;

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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean hasPriv() {
        return getStringField(BenefitDBF.PRIV_CAT) != null;
    }
}
