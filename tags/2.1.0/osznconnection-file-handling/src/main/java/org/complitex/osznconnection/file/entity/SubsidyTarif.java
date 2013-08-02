package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:14:28
 *
 * Файл тарифа.
 * Имя файла TARIF12.DBF
 */
public class SubsidyTarif extends AbstractRequest {

    @Override
    public RequestFileType getRequestFileType() {
        return RequestFileType.SUBSIDY_TARIF;
    }
}
