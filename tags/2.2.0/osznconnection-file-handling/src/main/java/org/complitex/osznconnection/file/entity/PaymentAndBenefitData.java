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
    private BigDecimal apartmentFeeCharge;
    private BigDecimal normCharge;
    private BigDecimal saldo;
    private BigDecimal reducedArea;
    private Integer roomCount;
    private String ownership;
    private BigDecimal apartmentFeeTarif;
    private BigDecimal heatingTarif;
    private BigDecimal hotWaterTarif;
    private BigDecimal coldWaterTarif;
    private BigDecimal gasTarif;
    private BigDecimal powerTarif;
    private BigDecimal garbageDisposalTarif;
    private BigDecimal drainageTarif;
    private BigDecimal heatingArea;
    private BigDecimal chargeHotWater;
    private BigDecimal chargeColdWater;
    private BigDecimal chargeGas;
    private BigDecimal chargePower;
    private BigDecimal chargeGarbageDisposal;
    private BigDecimal chargeDrainage;

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

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public BigDecimal getApartmentFeeCharge() {
        return apartmentFeeCharge;
    }

    public void setApartmentFeeCharge(BigDecimal apartmentFeeCharge) {
        this.apartmentFeeCharge = apartmentFeeCharge;
    }

    public BigDecimal getApartmentFeeTarif() {
        return apartmentFeeTarif;
    }

    public void setApartmentFeeTarif(BigDecimal apartmentFeeTarif) {
        this.apartmentFeeTarif = apartmentFeeTarif;
    }

    public BigDecimal getChargeColdWater() {
        return chargeColdWater;
    }

    public void setChargeColdWater(BigDecimal chargeColdWater) {
        this.chargeColdWater = chargeColdWater;
    }

    public BigDecimal getChargeDrainage() {
        return chargeDrainage;
    }

    public void setChargeDrainage(BigDecimal chargeDrainage) {
        this.chargeDrainage = chargeDrainage;
    }

    public BigDecimal getChargeGarbageDisposal() {
        return chargeGarbageDisposal;
    }

    public void setChargeGarbageDisposal(BigDecimal chargeGarbageDisposal) {
        this.chargeGarbageDisposal = chargeGarbageDisposal;
    }

    public BigDecimal getChargeGas() {
        return chargeGas;
    }

    public void setChargeGas(BigDecimal chargeGas) {
        this.chargeGas = chargeGas;
    }

    public BigDecimal getChargeHotWater() {
        return chargeHotWater;
    }

    public void setChargeHotWater(BigDecimal chargeHotWater) {
        this.chargeHotWater = chargeHotWater;
    }

    public BigDecimal getChargePower() {
        return chargePower;
    }

    public void setChargePower(BigDecimal chargePower) {
        this.chargePower = chargePower;
    }

    public BigDecimal getColdWaterTarif() {
        return coldWaterTarif;
    }

    public void setColdWaterTarif(BigDecimal coldWaterTarif) {
        this.coldWaterTarif = coldWaterTarif;
    }

    public BigDecimal getDrainageTarif() {
        return drainageTarif;
    }

    public void setDrainageTarif(BigDecimal drainageTarif) {
        this.drainageTarif = drainageTarif;
    }

    public BigDecimal getGarbageDisposalTarif() {
        return garbageDisposalTarif;
    }

    public void setGarbageDisposalTarif(BigDecimal garbageDisposalTarif) {
        this.garbageDisposalTarif = garbageDisposalTarif;
    }

    public BigDecimal getGasTarif() {
        return gasTarif;
    }

    public void setGasTarif(BigDecimal gasTarif) {
        this.gasTarif = gasTarif;
    }

    public BigDecimal getHeatingArea() {
        return heatingArea;
    }

    public void setHeatingArea(BigDecimal heatingArea) {
        this.heatingArea = heatingArea;
    }

    public BigDecimal getHeatingTarif() {
        return heatingTarif;
    }

    public void setHeatingTarif(BigDecimal heatingTarif) {
        this.heatingTarif = heatingTarif;
    }

    public BigDecimal getHotWaterTarif() {
        return hotWaterTarif;
    }

    public void setHotWaterTarif(BigDecimal hotWaterTarif) {
        this.hotWaterTarif = hotWaterTarif;
    }

    public BigDecimal getPowerTarif() {
        return powerTarif;
    }

    public void setPowerTarif(BigDecimal powerTarif) {
        this.powerTarif = powerTarif;
    }

    @Override
    public String toString() {
        return "Lodger count: " + lodgerCount + ", user count: " + userCount + ", apartment fee tarif: " + apartmentFeeTarif + ", saldo: " + saldo + ", room count: " + roomCount
                + ", reduced area: " + reducedArea + ", percent: " + percent + ", ownership: " + ownership + ", norm charge: " + normCharge
                + ", apartment fee charge: " + apartmentFeeCharge
                + ", heating tarif: " + heatingTarif + ", hot water tarif: " + hotWaterTarif + ", cold water tarif: " + coldWaterTarif
                + ", gas tarif: " + gasTarif + ", power tarif: " + powerTarif + ", garbage disposal tarif: " + garbageDisposalTarif
                + ", drainage tarif: " + drainageTarif + ", heating area: " + heatingArea + ", charge hot water: " + chargeHotWater + ""
                + ", charge cold water: " + chargeColdWater + ", charge gas: " + chargeGas + ", charge power: " + chargePower
                + ", charge garbage disposal: " + chargeGarbageDisposal + ", charge drainage: " + chargeDrainage;
    }
}
