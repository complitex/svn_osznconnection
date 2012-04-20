package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.08.2010 16:17:13
 *
 * Перечисление допустимых имен и типов полей записей файлов запроса возмещения по льготам.
 *
 * @see org.complitex.osznconnection.file.entity.Benefit
 */
public enum BenefitDBF {

    OWN_NUM, //1     OWN_NUM	    CHARACTER 	15	0	Номер дела
    REE_NUM, //2     REE_NUM	    NUMERIC 	2	0	Номер реестра
    OWN_NUM_SR, //3     OWN_NUM_SR	CHARACTER	15		Лицевой счет в обслуж. организации
    FAM_NUM, //4     FAM_NUM	    NUMERIC 	2	0	Номер члена семьи
    SUR_NAM, //5     SUR_NAM	    CHARACTER	30		Фамилия
    F_NAM, //6     F_NAM	    CHARACTER	15		Имя
    M_NAM, //7     M_NAM	    CHARACTER	20		Отчество
    IND_COD, //8     IND_COD	    CHARACTER	10		Идентификационный номер
    PSP_SER, //9     PSP_SER	    CHARACTER	6		Серия паспорта
    PSP_NUM, //10    PSP_NUM	    CHARACTER	6		Номер паспорта
    OZN, //11    OZN	        NUMERIC	    1	0	Признак владельца
    CM_AREA, //12    CM_AREA	    NUMERIC	    10	2	Общая площадь
    HEAT_AREA,//13    HEAT_ AREA	NUMERIC	    10	2	Обогреваемая площадь
    OWN_FRM, //14    OWN_FRM  	NUMERIC	    6	0	Форма собственности
    HOSTEL, //15    HOSTEL	    NUMERIC	    2	0	Количество комнат
    PRIV_CAT, //16    PRIV_CAT     NUMERIC	    3	0	Категория льготы на платежи
    ORD_FAM, //17    ORD_FAM	    NUMERIC	    2	0	Порядок семьи льготников для расчета платежей
    OZN_SQ_ADD, //18    OZN_SQ_ADD	NUMERIC 	1	0	Признак учета дополнительной площади
    OZN_ABS, //19    OZN_ABS	    NUMERIC 	1	0	Признак отсутствия данных в базе ЖЭО
    RESERV1, //20    RESERV1	    NUMERIC	    10	2	Резерв
    RESERV2;         //21    RESERV2	    CHARACTER	10		Резерв
}
