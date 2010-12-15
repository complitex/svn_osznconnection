/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class PaymentAndBenefitData implements Serializable {

    private Integer lodgerCount;
    private Integer userCount;
    private Double percent;
    private Double charge;
    private Double normCrarge;
    private Double saldo;
    private Double reducedArea;
    private Integer roomCount;
    private String ownership;
    private Double tarif;

    public Double getCharge() {
        return charge;
    }

    public void setCharge(Double charge) {
        this.charge = charge;
    }

    public Integer getLodgerCount() {
        return lodgerCount;
    }

    public void setLodgerCount(Integer lodgerCount) {
        this.lodgerCount = lodgerCount;
    }

    public Double getNormCrarge() {
        return normCrarge;
    }

    public void setNormCrarge(Double normCrarge) {
        this.normCrarge = normCrarge;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Double getReducedArea() {
        return reducedArea;
    }

    public void setReducedArea(Double reducedArea) {
        this.reducedArea = reducedArea;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
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
}
