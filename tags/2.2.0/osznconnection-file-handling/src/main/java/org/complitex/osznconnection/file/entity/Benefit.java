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
public class Benefit extends AbstractAccountRequest<BenefitDBF> {
    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.BENEFIT;
    }

    public boolean hasPriv() {
        return getStringField(BenefitDBF.PRIV_CAT) != null;
    }
}
