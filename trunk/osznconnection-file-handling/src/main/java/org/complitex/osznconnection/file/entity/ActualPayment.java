package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class ActualPayment extends AbstractAccountRequest<ActualPaymentDBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.ACTUAL_PAYMENT;
    }

    @Override
    public String getCity() {
        return getStringField(ActualPaymentDBF.N_NAME);
    }

    @Override
    public String getStreetType() {
        return getStringField(ActualPaymentDBF.VUL_CAT);
    }

    @Override
    public String getStreetCode() {
        return getStringField(ActualPaymentDBF.VUL_CODE);
    }

    @Override
    public String getStreet() {
        return getStringField(ActualPaymentDBF.VUL_NAME);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(ActualPaymentDBF.BLD_NUM);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(ActualPaymentDBF.CORP_NUM);
    }

    @Override
    public String getApartment() {
        return super.getApartment();
    }
}
