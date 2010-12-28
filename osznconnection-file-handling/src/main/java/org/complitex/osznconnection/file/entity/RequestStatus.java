/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;
import java.util.List;

/**
 * Перечисление статусов для записей файлов payment и benefit.
 * @author Artem
 */
public enum RequestStatus implements IEnumCode {

    /* группа "локально неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какя-то часть внутреннего адреса у записи не разрешена */
    CITY_UNRESOLVED_LOCALLY(200), STREET_UNRESOLVED_LOCALLY(201), BUILDING_UNRESOLVED_LOCALLY(202),

    MORE_ONE_LOCAL_CITY(234), MORE_ONE_LOCAL_STREET(235), MORE_ONE_LOCAL_BUILDING(236),
    
    MORE_ONE_LOCAL_CITY_CORRECTION(210), MORE_ONE_LOCAL_STREET_CORRECTION(211), MORE_ONE_LOCAL_BUILDING_CORRECTION(228),

    /*  указывает на то, что адрес откорректирован вручную в UI */
    ADDRESS_CORRECTED(204),

    /* группа "неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какая-то часть исходящего в ЦН адреса у записи не разрешена */
    CITY_UNRESOLVED(205), DISTRICT_UNRESOLVED(206), STREET_TYPE_UNRESOLVED(207),
    STREET_UNRESOLVED(208), BUILDING_UNRESOLVED(209),

    MORE_ONE_REMOTE_CITY_CORRECTION(229), MORE_ONE_REMOTE_DISTRICT_CORRECTION(230), MORE_ONE_REMOTE_STREET_TYPE_CORRECTION(231),
    MORE_ONE_REMOTE_STREET_CORRECTION(232), MORE_ONE_REMOTE_BUILDING_CORRECTION(233),

    /* группа "ненайденных" статусов, т.е. любой статус из группы указывает на то, что запрос на получение л/c в ЦН был сделан,
     но часть адреса не распознана в ЦН */
    CITY_NOT_FOUND(221), DISTRICT_NOT_FOUND(222), STREET_TYPE_NOT_FOUND(223),
    STREET_NOT_FOUND(224), BUILDING_NOT_FOUND(225), BUILDING_CORP_NOT_FOUND(226),
    APARTMENT_NOT_FOUND(227),

    /* Номер л/c не найден */
    ACCOUNT_NUMBER_NOT_FOUND(212),

    /* Более одного человека в ЦН, имеющие разные л/c, привязаны к одному адресу. Указывает на то, что в UI появится возможность выбрать
      нужный л/c вручную */
    MORE_ONE_ACCOUNTS(213),

    /* Номер л/c разрешен */
    ACCOUNT_NUMBER_RESOLVED(214),

    /* Указывает на то, что запись обработана */
    PROCESSED(215),

    /* Указывает на то, что не найден код тарифа в таблице тарифов для заполнения поля CODE2_1.
     См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processData()  */
    TARIF_CODE2_1_NOT_FOUND(216),

    /* Не сопоставлен носитель льготы
        См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefitData()
     */
    BENEFIT_OWNER_NOT_ASSOCIATED(217),

    /* Указывает на то, что код привилегии не найден в таблице коррекций
        См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefitData()
     */
    BENEFIT_NOT_FOUND(218),

    PROCESSING_INVALID_FORMAT(219),

    BINDING_INVALID_FORMAT(203),

    PAYMENT_NOT_EXISTS(220);

    private int code;

    private RequestStatus(int code){
        this.code = code;
    }

    public boolean isAddressResolved() {
        return !Lists.newArrayList(ADDRESS_CORRECTED, 
                CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_CITY, MORE_ONE_LOCAL_STREET,
                MORE_ONE_LOCAL_BUILDING, MORE_ONE_LOCAL_CITY_CORRECTION, MORE_ONE_LOCAL_STREET_CORRECTION, MORE_ONE_LOCAL_BUILDING_CORRECTION,
                CITY_UNRESOLVED, DISTRICT_UNRESOLVED, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED, BUILDING_UNRESOLVED, MORE_ONE_REMOTE_CITY_CORRECTION,
                MORE_ONE_REMOTE_DISTRICT_CORRECTION, MORE_ONE_REMOTE_STREET_TYPE_CORRECTION, MORE_ONE_REMOTE_STREET_CORRECTION,
                MORE_ONE_REMOTE_BUILDING_CORRECTION).
                contains(this);
    }

    public boolean isAddressResolvedLocally(){
        return !Lists.newArrayList(ADDRESS_CORRECTED, CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY,
                MORE_ONE_LOCAL_CITY, MORE_ONE_LOCAL_STREET, MORE_ONE_LOCAL_BUILDING, MORE_ONE_LOCAL_BUILDING_CORRECTION,
                MORE_ONE_LOCAL_STREET_CORRECTION, MORE_ONE_LOCAL_CITY_CORRECTION).
                contains(this);
    }

    public boolean isLocallyCorrectable() {
        return Lists.newArrayList(CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_CITY,
                MORE_ONE_LOCAL_STREET, MORE_ONE_LOCAL_BUILDING).
                contains(this);
    }

    /**
     * Возвращает список статусов которые могут иметь несвязанные записи.
     * @return
     */
    public static List<RequestStatus> notBoundStatuses() {
        return Lists.newArrayList(ACCOUNT_NUMBER_NOT_FOUND,
                ADDRESS_CORRECTED, 
                BUILDING_CORP_NOT_FOUND, BUILDING_NOT_FOUND, BUILDING_UNRESOLVED, BUILDING_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_BUILDING_CORRECTION,
                MORE_ONE_REMOTE_BUILDING_CORRECTION, MORE_ONE_LOCAL_BUILDING,
                CITY_UNRESOLVED, CITY_UNRESOLVED_LOCALLY, CITY_NOT_FOUND, MORE_ONE_LOCAL_CITY_CORRECTION, MORE_ONE_REMOTE_CITY_CORRECTION,
                MORE_ONE_LOCAL_CITY,
                DISTRICT_UNRESOLVED, DISTRICT_NOT_FOUND, MORE_ONE_REMOTE_DISTRICT_CORRECTION,
                STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED, STREET_TYPE_NOT_FOUND, MORE_ONE_REMOTE_STREET_TYPE_CORRECTION, MORE_ONE_LOCAL_STREET,
                STREET_NOT_FOUND, STREET_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_STREET_CORRECTION, MORE_ONE_REMOTE_STREET_CORRECTION,
                APARTMENT_NOT_FOUND,
                MORE_ONE_ACCOUNTS, BINDING_INVALID_FORMAT, PAYMENT_NOT_EXISTS);
    }

    /**
     * Возвращает список статусов которые могут иметь необработанные записи.
     * @return
     */
    public static List<RequestStatus> notProcessedStatuses() {
        List<RequestStatus> result = notBoundStatuses();
        result.add(ACCOUNT_NUMBER_RESOLVED);
        result.add(TARIF_CODE2_1_NOT_FOUND);
        result.add(BENEFIT_OWNER_NOT_ASSOCIATED);
        result.add(BENEFIT_NOT_FOUND);
        result.add(PROCESSING_INVALID_FORMAT);
        return result;
    }

    @Override
    public int getCode() {
        return code;
    }
}
