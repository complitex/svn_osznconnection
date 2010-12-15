/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.util.Collection;
import org.complitex.osznconnection.file.entity.BenefitData;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.Payment;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Базовый интерфейс для реализаций адаптера взаимодействия с ЦН.
 * @author Artem
 */
@Local
public interface ICalculationCenterAdapter {

    void prepareCity(Payment payment, String city, String cityCode);

    void prepareDistrict(Payment payment, String district, String districtCode);

    void prepareStreet(Payment payment, String street, String streetCode);

    void prepareStreetType(Payment payment, String streetType, String streetTypeCode);

    void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, String buildingCode);

    void prepareApartment(Payment payment, String apartment, String apartmentCode);

    void acquirePersonAccount(Payment payment);

    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) throws AccountNotFoundException;

    public void processPaymentAndBenefit(Payment payment, List<Benefit> benefits, long calculationCenterId) throws AccountNotFoundException;

    public void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId) throws AccountNotFoundException;

    public Collection<BenefitData> getBenefitData(String accountNumber, Date dat1) throws AccountNotFoundException;
}
