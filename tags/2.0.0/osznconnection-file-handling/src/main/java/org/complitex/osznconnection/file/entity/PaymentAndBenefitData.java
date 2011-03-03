/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Artem
 */
public class PaymentAndBenefitData implements Serializable {

    private Integer lodgerCount;
    private Integer userCount;
    private BigDecimal percent;
    private BigDecimal charge;
    private BigDecimal normCharge;
    private BigDecimal saldo;
    private BigDecimal reducedArea;
    private Integer roomCount;
    private String ownership;
    private Double tarif;

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public Integer getLodgerCount() {
        return lodgerCount;
    }

    public void setLodgerCount(Integer lodgerCount) {
        this.lodgerCount = lodgerCount;
    }

    public BigDecimal getNormCharge() {
        return normCharge;
    }

    public void setNormCharge(BigDecimal normCharge) {
        this.normCharge = normCharge;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public BigDecimal getReducedArea() {
        return reducedArea;
    }

    public void setReducedArea(BigDecimal reducedArea) {
        this.reducedArea = reducedArea;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Double getTarif() {
        return tarif;
    }

    public void setTarif(Double tarif) {
        this.tarif = tarif;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    @Override
    public String toString() {
        return "Lodger count: " + lodgerCount + ", user count: " + userCount + ", tarif: " + tarif + ", saldo: " + saldo + ", room count: " + roomCount
                + ", reduced area: " + reducedArea + ", percent: " + percent + ", ownership: " + ownership + ", norm charge: " + normCharge
                + ", charge: " + charge;
    }
}
