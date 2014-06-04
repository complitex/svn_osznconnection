/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;

/**
 *
 * @author Artem
 */
public class ActualPaymentData {

    private BigDecimal apartmentFeeCharge;
    private BigDecimal apartmentFeeTarif;
    private BigDecimal heatingCharge;
    private BigDecimal heatingTarif;
    private BigDecimal hotWaterCharge;
    private BigDecimal hotWaterTarif;
    private BigDecimal coldWaterCharge;
    private BigDecimal coldWaterTarif;
    private BigDecimal gasCharge;
    private BigDecimal gasTarif;
    private BigDecimal powerCharge;
    private BigDecimal powerTarif;
    private BigDecimal garbageDisposalCharge;
    private BigDecimal garbageDisposalTarif;
    private BigDecimal drainageCharge;
    private BigDecimal drainageTarif;

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

    public BigDecimal getColdWaterCharge() {
        return coldWaterCharge;
    }

    public void setColdWaterCharge(BigDecimal coldWaterCharge) {
        this.coldWaterCharge = coldWaterCharge;
    }

    public BigDecimal getColdWaterTarif() {
        return coldWaterTarif;
    }

    public void setColdWaterTarif(BigDecimal coldWaterTarif) {
        this.coldWaterTarif = coldWaterTarif;
    }

    public BigDecimal getDrainageCharge() {
        return drainageCharge;
    }

    public void setDrainageCharge(BigDecimal drainageCharge) {
        this.drainageCharge = drainageCharge;
    }

    public BigDecimal getDrainageTarif() {
        return drainageTarif;
    }

    public void setDrainageTarif(BigDecimal drainageTarif) {
        this.drainageTarif = drainageTarif;
    }

    public BigDecimal getGarbageDisposalCharge() {
        return garbageDisposalCharge;
    }

    public void setGarbageDisposalCharge(BigDecimal garbageDisposalCharge) {
        this.garbageDisposalCharge = garbageDisposalCharge;
    }

    public BigDecimal getGarbageDisposalTarif() {
        return garbageDisposalTarif;
    }

    public void setGarbageDisposalTarif(BigDecimal garbageDisposalTarif) {
        this.garbageDisposalTarif = garbageDisposalTarif;
    }

    public BigDecimal getGasCharge() {
        return gasCharge;
    }

    public void setGasCharge(BigDecimal gasCharge) {
        this.gasCharge = gasCharge;
    }

    public BigDecimal getGasTarif() {
        return gasTarif;
    }

    public void setGasTarif(BigDecimal gasTarif) {
        this.gasTarif = gasTarif;
    }

    public BigDecimal getHeatingCharge() {
        return heatingCharge;
    }

    public void setHeatingCharge(BigDecimal heatingCharge) {
        this.heatingCharge = heatingCharge;
    }

    public BigDecimal getHeatingTarif() {
        return heatingTarif;
    }

    public void setHeatingTarif(BigDecimal heatingTarif) {
        this.heatingTarif = heatingTarif;
    }

    public BigDecimal getHotWaterCharge() {
        return hotWaterCharge;
    }

    public void setHotWaterCharge(BigDecimal hotWaterCharge) {
        this.hotWaterCharge = hotWaterCharge;
    }

    public BigDecimal getHotWaterTarif() {
        return hotWaterTarif;
    }

    public void setHotWaterTarif(BigDecimal hotWaterTarif) {
        this.hotWaterTarif = hotWaterTarif;
    }

    public BigDecimal getPowerCharge() {
        return powerCharge;
    }

    public void setPowerCharge(BigDecimal powerCharge) {
        this.powerCharge = powerCharge;
    }

    public BigDecimal getPowerTarif() {
        return powerTarif;
    }

    public void setPowerTarif(BigDecimal powerTarif) {
        this.powerTarif = powerTarif;
    }

    @Override
    public String toString() {
        return "Apartment fee charge: " + apartmentFeeCharge + ", apartment fee tarif: " + apartmentFeeTarif
                + ", heating charge: " + heatingCharge + ", heating tarif: " + heatingTarif
                + ", hot water charge: " + hotWaterCharge + ", hot water tarif: " + hotWaterTarif
                + ", cold water charge: " + coldWaterCharge + ", cold water tarif: " + coldWaterTarif
                + ", gas charge: " + gasCharge + ", gas tarif: " + gasTarif
                + ", power charge: " + powerCharge + ", power tarif: " + powerTarif
                + ", gas charge: " + gasCharge + ", gas tarif: " + gasTarif
                + ", garbage disposal charge: " + garbageDisposalCharge + ", garbage disposal tarif: " + garbageDisposalTarif
                + ", drainage charge: " + drainageCharge + ", drainage tarif: " + drainageTarif;
    }
}
