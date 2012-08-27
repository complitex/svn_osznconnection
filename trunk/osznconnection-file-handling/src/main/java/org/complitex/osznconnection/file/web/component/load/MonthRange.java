/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.load;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class MonthRange implements Serializable {

    private int monthFrom;
    private int monthTo;

    public MonthRange(int monthFrom, int monthTo) {
        this.monthFrom = monthFrom;
        this.monthTo = monthTo;
    }

    public MonthRange(int month) {
        this(month, month);
    }

    public int getMonthFrom() {
        return monthFrom;
    }

    public int getMonthTo() {
        return monthTo;
    }
}
