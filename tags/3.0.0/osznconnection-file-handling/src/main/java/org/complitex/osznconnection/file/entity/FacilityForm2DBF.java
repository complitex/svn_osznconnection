/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum FacilityForm2DBF {

    CDPR, // Код ЄДРПОУ (ОГРН) организации
    IDCODE, // ИНН льготника
    FIO, // ФИО льготника
    PPOS, //
    RS, // Номер л/с ПУ
    YEARIN, // Год выгрузки данных
    MONTHIN, // Месяц выгрузки данных
    LGCODE, // Код льготы
    DATA1, // Дата начала периода
    DATA2, // Дата окончания периода
    LGKOL, // Кол-во пользующихся льготой
    LGKAT, // Категория льготы ЕДАРП
    LGPRC, // Процент льготы
    SUMM, // Сумма возмещения
    FACT, // Объем фактического потребления (для услуг со счетчиком)
    TARIF, // Ставка тарифа
    FLAG, //
}
