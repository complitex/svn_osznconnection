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
public enum ActualPaymentDBF {

    SUR_NAM(String.class, 30),
    F_NAM(String.class, 15),
    M_NAM(String.class, 20),
    INDX(String.class, 6),
    N_NAME(String.class, 30),
    N_CODE(String.class, 5),
    VUL_CAT(String.class, 7),
    VUL_NAME(String.class, 30),
    VUL_CODE(String.class, 5),
    BLD_NUM(String.class, 7),
    CORP_NUM(String.class, 2),
    FLAT(String.class, 9),
    OWN_NUM(String.class, 15),
    APP_NUM(String.class, 8),
    DAT_BEG(Date.class, 8),
    DAT_END(Date.class, 8),
    CM_AREA(BigDecimal.class, 7, 2),
    NM_AREA(BigDecimal.class, 7, 2),
    BLC_AREA(BigDecimal.class, 5, 2),
    FROG(BigDecimal.class, 5, 1),
    DEBT(BigDecimal.class, 10, 2),
    NUMB(Integer.class, 2),
    P1(BigDecimal.class, 10, 4),
    N1(BigDecimal.class, 10, 4),
    P2(BigDecimal.class, 10, 4),
    N2(BigDecimal.class, 10, 4),
    P3(BigDecimal.class, 10, 4),
    N3(BigDecimal.class, 10, 4),
    P4(BigDecimal.class, 10, 4),
    N4(BigDecimal.class, 10, 4),
    P5(BigDecimal.class, 10, 4),
    N5(BigDecimal.class, 10, 4),
    P6(BigDecimal.class, 10, 4),
    N6(BigDecimal.class, 10, 4),
    P7(BigDecimal.class, 10, 4),
    N7(BigDecimal.class, 10, 4),
    P8(BigDecimal.class, 10, 4),
    N8(BigDecimal.class, 10, 4);
    
    private Class type;
    private int length;
    private int scale = 0;

    ActualPaymentDBF(Class type, int length, int scale) {
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    ActualPaymentDBF(Class type, int length) {
        this.type = type;
        this.length = length;
    }

    ActualPaymentDBF(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }
}
