/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Artem
 */
public enum SubsidyDBF {
    
    FIO(String.class, 30),              // ФИО
    ID_RAJ(String.class, 5),            // Код района
    NP_CODE(String.class, 5),           // Код населенного пункта
    NP_NAME(String.class, 30),           // Название населенного пункта
    CAT_V(String.class, 7),             // Тип улицы
    VULCOD(String.class, 8),           // Код улицы
    NAME_V(String.class, 30),           // Название улицы
    BLD(String.class, 7),               // Номер дома
    CORP(String.class, 2),              // Корпус дома
    FLAT(String.class, 9),              // Номер квартиры
    RASH(String.class, 14),             // Номер л/с ПУ
    NUMB(String.class, 8),
    DAT1(Date.class, 8),                // Дата начала периода, на который предоставляется субсидия
    DAT2(Date.class, 8),                // Дата конца периода, на который предоставляется субсидия
    NM_PAY(BigDecimal.class, 9, 2),     // Начисление в пределах нормы
    
    P1(BigDecimal.class, 9, 4),           
    P2(BigDecimal.class, 9, 4),            
    P3(BigDecimal.class,9 , 4),            
    P4(BigDecimal.class, 9, 4),            
    P5(BigDecimal.class, 9, 4),
    P6(BigDecimal.class, 9, 4),            
    P7(BigDecimal.class, 9, 4),            
    P8(BigDecimal.class, 9, 4),   
    
    SM1(BigDecimal.class, 9, 2),
    SM2(BigDecimal.class, 9, 2),
    SM3(BigDecimal.class,9 , 2),
    SM4(BigDecimal.class, 9, 2),
    SM5(BigDecimal.class, 9, 2),
    SM6(BigDecimal.class, 9, 2),
    SM7(BigDecimal.class, 9, 2),
    SM8(BigDecimal.class, 9, 2),
    
    SB1(BigDecimal.class, 9, 2),
    SB2(BigDecimal.class, 9, 2),
    SB3(BigDecimal.class,9 , 2),
    SB4(BigDecimal.class, 9, 2),
    SB5(BigDecimal.class, 9, 2),
    SB6(BigDecimal.class, 9, 2),
    SB7(BigDecimal.class, 9, 2),
    SB8(BigDecimal.class, 9, 2),
    
    OB1(BigDecimal.class, 9, 2),
    OB2(BigDecimal.class, 9, 2),
    OB3(BigDecimal.class,9 , 2),
    OB4(BigDecimal.class, 9, 2),
    OB5(BigDecimal.class, 9, 2),
    OB6(BigDecimal.class, 9, 2),
    OB7(BigDecimal.class, 9, 2),
    OB8(BigDecimal.class, 9, 2),
    
    SUMMA(BigDecimal.class, 13, 2),
    NUMM(Integer.class, 2),
    SUBS(BigDecimal.class, 13, 2),
    KVT(Integer.class, 3);

    private Class<?> type;
    private int length;
    private int scale = 0;

    SubsidyDBF(Class<?> type, int length, int scale) {
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    SubsidyDBF(Class<?> type, int length) {
        this.type = type;
        this.length = length;
    }

    SubsidyDBF(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }
    
}
