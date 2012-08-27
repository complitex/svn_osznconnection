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
public class DateParameter implements Serializable {

    private static final int NO_MONTH_VALUE = -1;
    private final int year;
    private final int monthFrom;
    private final int monthTo;
    private final boolean monthEnabled;

    public DateParameter(int year, int monthFrom, int monthTo) {
        this.year = year;
        this.monthFrom = monthFrom;
        this.monthTo = monthTo;
        this.monthEnabled = true;
    }

    public DateParameter(int year) {
        this.year = year;
        this.monthFrom = this.monthTo = NO_MONTH_VALUE;
        this.monthEnabled = false;
    }

    public int getMonthFrom() {
        return monthFrom;
    }

    public int getMonthTo() {
        return monthTo;
    }

    public int getYear() {
        return year;
    }

    /**
     * In cases where exact month specified, i.e. monthTo == monthFrom this method may be more convenient than getMonthFrom()
     * and getMonthTo().
     * @return The same result as method getMonthFrom().
     */
    public int getMonth() {
        return getMonthFrom();
    }

    /**
     * @return Whether month values are enabled.
     */
    public boolean isMonthEnabled() {
        return monthEnabled;
    }
}
