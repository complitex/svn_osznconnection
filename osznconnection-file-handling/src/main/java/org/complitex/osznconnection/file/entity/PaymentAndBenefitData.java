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
    private Double apartmentFeeTarif;
    private Double heatingTarif;
    private Double hotWaterTarif;
    private Double coldWaterTarif;
    private Double gasTarif;
    private Double powerTarif;
    private Double garbageDisposalTarif;
    private Double drainageTarif;
    private Double heatingArea;
    private Double chargeHotWater;
    private Double chargeColdWater;
    private Double chargeGas;
    private Double chargePower;
    private Double chargeGarbageDisposal;
    private Double chargeDrainage;

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

    public Double getApartmentFeeTarif() {
        return apartmentFeeTarif;
    }

    public void setApartmentFeeTarif(Double apartmentFeeTarif) {
        this.apartmentFeeTarif = apartmentFeeTarif;
    }

    public Double getChargeColdWater() {
        return chargeColdWater;
    }

    public void setChargeColdWater(Double chargeColdWater) {
        this.chargeColdWater = chargeColdWater;
    }

    public Double getChargeDrainage() {
        return chargeDrainage;
    }

    public void setChargeDrainage(Double chargeDrainage) {
        this.chargeDrainage = chargeDrainage;
    }

    public Double getChargeGarbageDisposal() {
        return chargeGarbageDisposal;
    }

    public void setChargeGarbageDisposal(Double chargeGarbageDisposal) {
        this.chargeGarbageDisposal = chargeGarbageDisposal;
    }

    public Double getChargeGas() {
        return chargeGas;
    }

    public void setChargeGas(Double chargeGas) {
        this.chargeGas = chargeGas;
    }

    public Double getChargeHotWater() {
        return chargeHotWater;
    }

    public void setChargeHotWater(Double chargeHotWater) {
        this.chargeHotWater = chargeHotWater;
    }

    public Double getChargePower() {
        return chargePower;
    }

    public void setChargePower(Double chargePower) {
        this.chargePower = chargePower;
    }

    public Double getColdWaterTarif() {
        return coldWaterTarif;
    }

    public void setColdWaterTarif(Double coldWaterTarif) {
        this.coldWaterTarif = coldWaterTarif;
    }

    public Double getDrainageTarif() {
        return drainageTarif;
    }

    public void setDrainageTarif(Double drainageTarif) {
        this.drainageTarif = drainageTarif;
    }

    public Double getGarbageDisposalTarif() {
        return garbageDisposalTarif;
    }

    public void setGarbageDisposalTarif(Double garbageDisposalTarif) {
        this.garbageDisposalTarif = garbageDisposalTarif;
    }

    public Double getGasTarif() {
        return gasTarif;
    }

    public void setGasTarif(Double gasTarif) {
        this.gasTarif = gasTarif;
    }

    public Double getHeatingArea() {
        return heatingArea;
    }

    public void setHeatingArea(Double heatingArea) {
        this.heatingArea = heatingArea;
    }

    public Double getHeatingTarif() {
        return heatingTarif;
    }

    public void setHeatingTarif(Double heatingTarif) {
        this.heatingTarif = heatingTarif;
    }

    public Double getHotWaterTarif() {
        return hotWaterTarif;
    }

    public void setHotWaterTarif(Double hotWaterTarif) {
        this.hotWaterTarif = hotWaterTarif;
    }

    public Double getPowerTarif() {
        return powerTarif;
    }

    public void setPowerTarif(Double powerTarif) {
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
