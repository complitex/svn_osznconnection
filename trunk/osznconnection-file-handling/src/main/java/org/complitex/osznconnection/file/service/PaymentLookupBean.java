/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 * Вспомогательный для PaymentLookupPanel бин.
 * @author Artem
 */
@Stateless
public class PaymentLookupBean extends AbstractBean {

    @EJB(beanName = "AddressService")
    private AddressService addressService;
    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    /**
     * Разрешить исходящий в ЦН адрес по схеме "локальная адресная база -> адрес центра начислений"
     * Делегирует всю работу AddressService.resolveOutgoingAddress().
     * @param actualPayment
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        addressService.resolveOutgoingAddress(payment, calculationCenterId, adapter);
    }

    @Transactional
    public void resolveOutgoingAddress(ActualPayment actualPayment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        addressService.resolveOutgoingAddress(actualPayment, calculationCenterId, adapter);
    }

    /**
     * Получить детальную информацию о клиентах ЦН.
     * Вся работа по поиску делегируется адаптеру взаимодействия с ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquireAccountCorrectionDetails()
     * @param actualPayment
     * @return
     */
    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(Payment payment) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByAddress(payment.getOutgoingDistrict(), payment.getOutgoingStreetType(), payment.getOutgoingStreet(),
                payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(), payment.getOutgoingApartment(), payment,
                (Date) payment.getField(PaymentDBF.DAT1));
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(ActualPayment actualPayment) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByAddress(actualPayment.getOutgoingDistrict(), actualPayment.getOutgoingStreetType(),
                actualPayment.getOutgoingStreet(), actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), actualPayment, (Date) actualPayment.getField(ActualPaymentDBF.DAT_BEG));
    }

    @Transactional
    public void setupOutgoingDistrict(Payment payment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationCenterId, payment.getOrganizationId());
        if (districtCorrections.isEmpty()) {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        } else if (districtCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(payment, districtCorrection.getCorrection(), districtCorrection.getCode());
        }
    }

    @Transactional
    public void setupOutgoingDistrict(ActualPayment actualPayment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationCenterId,
                actualPayment.getOrganizationId());
        if (districtCorrections.isEmpty()) {
            actualPayment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        } else if (districtCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(actualPayment, districtCorrection.getCorrection(), districtCorrection.getCode());
        }
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByOsznAccount(Payment payment) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByOsznAccount(payment);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByMegabankAccount(Payment payment, String megabankAccount) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByMegabankAccount(payment.getOutgoingDistrict(), payment, megabankAccount);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByMegabankAccount(ActualPayment actualPayment, String megabankAccount) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByMegabankAccount(actualPayment.getOutgoingDistrict(), actualPayment, megabankAccount);
    }
}
