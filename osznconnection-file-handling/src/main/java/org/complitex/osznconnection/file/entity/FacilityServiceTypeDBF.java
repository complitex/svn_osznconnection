/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum FacilityServiceTypeDBF {

    COD, // Код района
    CDPR, // Код ЄДРПОУ (ОГРН) организации
    NCARD, // Идентификатор льготника
    IDCODE, // ИНН собственника жилья/льготника (ставят ИНН льготника)
    PASP, // Серия и номер паспорта собственника жилья/льготника (ставят паспорт льготника)
    FIO, // ФИО собственника жилья/льготника (ставят ФИО льготника)
    IDPIL, // ИНН льготника
    PASPPIL, // Серия и номер паспорта льготника
    FIOPIL, // ФИО льготника
    INDEX, // Почтовый индекс
    CDUL, // Код улицы
    HOUSE, // Номер дома
    BUILD, // Корпус
    APT, // Номер квартиры
    KAT, // Категория льготы ЕДАРП
    LGCODE, // Код возмещения
    YEARIN, // Год начала действия льготы
    MONTHIN, // Месяц начала действия льготы
    YEAROUT, // Год окончания действия льготы
    MONTHOUT, // Месяц окончания действия льготы
    RAH, // Номер л/с ПУ
    RIZN, // Тип услуги
    TARIF, // Код тарифа услуги
}
