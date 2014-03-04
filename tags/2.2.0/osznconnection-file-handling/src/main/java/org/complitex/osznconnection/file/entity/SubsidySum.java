package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 0:19
 */
public class SubsidySum {
    private BigDecimal nSum;
    private BigDecimal sbSum;
    private BigDecimal smSum;

    public SubsidySum() {
    }

    public SubsidySum(BigDecimal nSum, BigDecimal sbSum, BigDecimal smSum) {
        this.nSum = nSum;
        this.sbSum = sbSum;
        this.smSum = smSum;
    }

    public BigDecimal getNSum() {
        return nSum;
    }

    public void setNSum(BigDecimal nSum) {
        this.nSum = nSum;
    }

    public BigDecimal getSbSum() {
        return sbSum;
    }

    public void setSbSum(BigDecimal sbSum) {
        this.sbSum = sbSum;
    }

    public BigDecimal getSmSum() {
        return smSum;
    }

    public void setSmSum(BigDecimal smSum) {
        this.smSum = smSum;
    }
}
