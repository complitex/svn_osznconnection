package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *
 * Запись файла запроса начислений.
 * @see org.complitex.osznconnection.file.entity.AbstractRequest
 *
 * Имена полей фиксированы в <code>Enum<code> перечислении <code>PaymentDBF</code>
 * @see org.complitex.osznconnection.file.entity.PaymentDBF
 */
public class Payment extends AbstractAccountRequest<PaymentDBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.PAYMENT;
    }

    @Override
    public String getCity() {
        return getStringField(PaymentDBF.N_NAME);
    }

    @Override
    public String getStreet() {
        return getStringField(PaymentDBF.VUL_NAME);
    }

    @Override
    public String getBuildingNumber() {
        return getStringField(PaymentDBF.BLD_NUM);
    }

    @Override
    public String getBuildingCorp() {
        return getStringField(PaymentDBF.CORP_NUM);
    }
}
