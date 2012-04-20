package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:14:28
 *
 * Файл тарифа.
 * Имя файла TARIF12.DBF
 *
 * @see org.complitex.osznconnection.file.entity.AbstractRequest
 */
public class Tarif extends AbstractRequest {

    @Override
    protected RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.TARIF;
    }
}
