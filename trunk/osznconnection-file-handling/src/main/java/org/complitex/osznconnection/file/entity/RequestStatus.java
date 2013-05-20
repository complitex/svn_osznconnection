/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 * Перечисление статусов для записей файлов payment и benefit.
 * @author Artem
 */
public enum RequestStatus implements IEnumCode {

    /* запись загружена */
    LOADED(240),

     /* группа "локально неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какая-то часть внутреннего адреса у записи не разрешена */
    CITY_UNRESOLVED_LOCALLY(200), STREET_TYPE_UNRESOLVED_LOCALLY(237), STREET_UNRESOLVED_LOCALLY(201), STREET_AND_BUILDING_UNRESOLVED_LOCALLY(231),
    BUILDING_UNRESOLVED_LOCALLY(202),

    /* найдено больше одной записи адреса во внутреннем адресном справочнике */
    MORE_ONE_LOCAL_CITY(234), MORE_ONE_LOCAL_STREET_TYPE(238), MORE_ONE_LOCAL_STREET(235), MORE_ONE_LOCAL_BUILDING(236),

    /* Найдено более одной записи в коррекциях для ОСЗН */
    MORE_ONE_LOCAL_CITY_CORRECTION(210), MORE_ONE_LOCAL_STREET_TYPE_CORRECTION(239), MORE_ONE_LOCAL_STREET_CORRECTION(211),
    MORE_ONE_LOCAL_BUILDING_CORRECTION(228),

    /*  указывает на то, что адрес откорректирован вручную в UI */
    ADDRESS_CORRECTED(204),

    /* группа "неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какая-то часть исходящего в ЦН адреса у записи не разрешена */
    CITY_UNRESOLVED(205), DISTRICT_UNRESOLVED(206), STREET_TYPE_UNRESOLVED(207),
    STREET_UNRESOLVED(208), BUILDING_UNRESOLVED(209),

    /* Найдено более одной записи в коррекциях для ЦН */
    MORE_ONE_REMOTE_CITY_CORRECTION(229), MORE_ONE_REMOTE_DISTRICT_CORRECTION(230),
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
    
    /**
     * Найдено более одной записи в локальной таблице счетов абонентов.
     */
    MORE_ONE_ACCOUNTS_LOCALLY(242),

    /**
     * Номер л/с ПУ не соответствует номеру л/с ЖЭКа из центра начислений.
     */
    ACCOUNT_NUMBER_MISMATCH(241),
    
    /* Номер л/c разрешен */
    ACCOUNT_NUMBER_RESOLVED(214),

    /* Указывает на то, что запись обработана */
    PROCESSED(215),

    /* Указывает на то, что не найден код тарифа для запросов по субсидиям в таблице тарифов для заполнения поля CODE2_1. */
    SUBSIDY_TARIF_CODE_NOT_FOUND(216),

    /* Не сопоставлен носитель льготы */
    BENEFIT_OWNER_NOT_ASSOCIATED(217),

    /* Указывает на то, что код привилегии не найден в таблице коррекций */
    BENEFIT_NOT_FOUND(218),

    PROCESSING_INVALID_FORMAT(219),

    BINDING_INVALID_FORMAT(203),

    PAYMENT_NOT_EXISTS(220);

    private int code;

    private RequestStatus(int code) {
        this.code = code;
    }
    private static final Set<RequestStatus> ADDRESS_UNRESOLVED_STATUSES = 
            Sets.immutableEnumSet(ImmutableList.of(ADDRESS_CORRECTED,
            CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, STREET_AND_BUILDING_UNRESOLVED_LOCALLY, 
            STREET_TYPE_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_CITY, MORE_ONE_LOCAL_STREET, 
            MORE_ONE_LOCAL_STREET_TYPE, MORE_ONE_LOCAL_BUILDING, MORE_ONE_LOCAL_CITY_CORRECTION, 
            MORE_ONE_LOCAL_STREET_CORRECTION, MORE_ONE_LOCAL_STREET_TYPE_CORRECTION, MORE_ONE_LOCAL_BUILDING_CORRECTION,
            CITY_UNRESOLVED, DISTRICT_UNRESOLVED, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED, BUILDING_UNRESOLVED, 
            MORE_ONE_REMOTE_CITY_CORRECTION, MORE_ONE_REMOTE_DISTRICT_CORRECTION, MORE_ONE_REMOTE_STREET_CORRECTION,
            MORE_ONE_REMOTE_BUILDING_CORRECTION));


    /**
     * Разрешен ли адрес.
     * Адрес считаем разрешенным, если статус payment записи не входит в список статусов, указывающих на то что адрес не разрешен локально,
     * не входит в список статусов, указывающих на то что адрес не разрешен в ЦН, и не равен RequestStatus.ADDRESS_CORRECTED,
     * который указывает на то, что адрес откорректировали в UI.
     */
    public boolean isAddressResolved() {
        return !ADDRESS_UNRESOLVED_STATUSES.contains(this);
    }

    private static final Set<RequestStatus> ADDRESS_UNRESOLVED_LOCALLY_STATUSES = 
            Sets.immutableEnumSet(ImmutableList.of(ADDRESS_CORRECTED,
            CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, STREET_AND_BUILDING_UNRESOLVED_LOCALLY,
            STREET_TYPE_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_CITY, MORE_ONE_LOCAL_STREET, 
            MORE_ONE_LOCAL_STREET_TYPE, MORE_ONE_LOCAL_BUILDING, MORE_ONE_LOCAL_BUILDING_CORRECTION,
            MORE_ONE_LOCAL_STREET_CORRECTION, MORE_ONE_LOCAL_STREET_TYPE_CORRECTION, MORE_ONE_LOCAL_CITY_CORRECTION));

    public boolean isAddressResolvedLocally() {
        return !ADDRESS_UNRESOLVED_LOCALLY_STATUSES.contains(this);
    }
    private static final Set<RequestStatus> ADDRESS_CORRECTABLE_STATUSES =
            Sets.immutableEnumSet(ImmutableList.of(CITY_UNRESOLVED_LOCALLY, STREET_UNRESOLVED_LOCALLY, 
            STREET_TYPE_UNRESOLVED_LOCALLY, BUILDING_UNRESOLVED_LOCALLY));

    public boolean isAddressCorrectable() {
        return ADDRESS_CORRECTABLE_STATUSES.contains(this);
    }
    
    public boolean isImmediatelySearchByAddress(){
        return this == ACCOUNT_NUMBER_MISMATCH || this == MORE_ONE_ACCOUNTS || this == MORE_ONE_ACCOUNTS_LOCALLY;
    }

    private static final Set<RequestStatus> UNBOUND_STATUSES = 
            Sets.immutableEnumSet(ImmutableList.of(ACCOUNT_NUMBER_NOT_FOUND, LOADED, ADDRESS_CORRECTED,
            BUILDING_CORP_NOT_FOUND, BUILDING_NOT_FOUND, BUILDING_UNRESOLVED, BUILDING_UNRESOLVED_LOCALLY, 
            MORE_ONE_LOCAL_BUILDING_CORRECTION, MORE_ONE_REMOTE_BUILDING_CORRECTION, MORE_ONE_LOCAL_BUILDING,
            CITY_UNRESOLVED, CITY_UNRESOLVED_LOCALLY, CITY_NOT_FOUND, MORE_ONE_LOCAL_CITY_CORRECTION, 
            MORE_ONE_REMOTE_CITY_CORRECTION, MORE_ONE_LOCAL_CITY, DISTRICT_UNRESOLVED, DISTRICT_NOT_FOUND, 
            MORE_ONE_REMOTE_DISTRICT_CORRECTION, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED, STREET_TYPE_NOT_FOUND, 
            MORE_ONE_LOCAL_STREET, MORE_ONE_LOCAL_STREET_TYPE, STREET_NOT_FOUND, STREET_UNRESOLVED_LOCALLY, 
            STREET_AND_BUILDING_UNRESOLVED_LOCALLY, STREET_TYPE_UNRESOLVED_LOCALLY, MORE_ONE_LOCAL_STREET_CORRECTION,
            MORE_ONE_LOCAL_STREET_TYPE_CORRECTION, MORE_ONE_REMOTE_STREET_CORRECTION,
            APARTMENT_NOT_FOUND, MORE_ONE_ACCOUNTS, MORE_ONE_ACCOUNTS_LOCALLY, ACCOUNT_NUMBER_MISMATCH, 
            BINDING_INVALID_FORMAT, PAYMENT_NOT_EXISTS));

    /**
     * Возвращает список статусов которые могут иметь несвязанные записи.
     * @return
     */
    public static Set<RequestStatus> unboundStatuses() {
        return UNBOUND_STATUSES;
    }
    
    private static final Set<RequestStatus> UNPROCESSED_SET_STATUSES = 
            Sets.immutableEnumSet(ImmutableList.<RequestStatus>builder().addAll(UNBOUND_STATUSES).
            add(ACCOUNT_NUMBER_RESOLVED).
            add(SUBSIDY_TARIF_CODE_NOT_FOUND).
            add(BENEFIT_OWNER_NOT_ASSOCIATED).
            add(BENEFIT_NOT_FOUND).
            add(PROCESSING_INVALID_FORMAT).build());

    /**
     * Возвращает список статусов которые могут иметь необработанные записи.
     * @return
     */
    public static Set<RequestStatus> unprocessedStatuses() {
        return UNPROCESSED_SET_STATUSES;
    }

    @Override
    public int getCode() {
        return code;
    }

    public boolean isNotIn(RequestStatus... statuses){
        for (RequestStatus s : statuses){
            if (this.equals(s)){
                return false;
            }
        }

        return true;
    }
}
