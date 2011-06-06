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
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.adapter.exception.UnknownAccountNumberTypeException;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.RequestFile;

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

    void prepareCity(ActualPayment actualPayment, String city, String cityCode);

    void prepareDistrict(ActualPayment actualPayment, String district, String districtCode);

    void prepareStreet(ActualPayment actualPayment, String street, String streetCode);

    void prepareStreetType(ActualPayment actualPayment, String streetType, String streetTypeCode);

    void prepareBuilding(ActualPayment actualPayment, String buildingNumber, String buildingCorp, String buildingCode);

    void prepareApartment(ActualPayment actualPayment, String apartment, String apartmentCode);

    void acquirePersonAccount(RequestFile.TYPE requestFileType, AbstractRequest request, String lastName, String puAccountNumber,
            String district, String streetType, String street, String buildingNumber, String buildingCorp, String apartment,
            Date date) throws DBException;

    List<AccountDetail> acquireAccountDetailsByAddress(AbstractRequest request, String district, String streetType, String street,
            String buildingNumber, String buildingCorp, String apartment, Date date) throws DBException;

    List<AccountDetail> acquireAccountDetailsByAccount(AbstractRequest request, String district, String account) throws DBException,
            UnknownAccountNumberTypeException;

    void processPaymentAndBenefit(Payment payment, List<Benefit> benefits, long calculationCenterId) throws DBException;

    void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId) throws DBException;

    Collection<BenefitData> getBenefitData(Benefit benefit, Date dat1) throws DBException;

    void processActualPayment(ActualPayment actualPayment, Date date) throws DBException;
}
