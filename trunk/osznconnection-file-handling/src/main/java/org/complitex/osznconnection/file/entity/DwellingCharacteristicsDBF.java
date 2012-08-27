/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum DwellingCharacteristicsDBF {

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
    BLILD, // Корпус
    APT, // Номер квартиры
    VL, // Тип собственности
    PLZAG, // Общая площадь помещения
    PLOPAL, // Отапливаемая площадь помещения
}
