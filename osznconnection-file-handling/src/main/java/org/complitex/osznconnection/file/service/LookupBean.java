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
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.ActualPayment;

/**
 * @author Artem
 */
@Stateless
public class LookupBean extends AbstractBean {

    @EJB
    private AddressService addressService;
    @EJB
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    /**
     * Разрешить исходящий в ЦН адрес по схеме "локальная адресная база -> адрес центра начислений"
     * Делегирует всю работу AddressService.resolveOutgoingAddress().
     * @param request
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
     * @param request
     * @return
     */
    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(AbstractRequest request, String district, String streetType, String street,
            String buildingNumber, String buildingCorp, String apartment, Date date) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByAddress(request, district, streetType, street, buildingNumber, buildingCorp, apartment, date);
    }

    @Transactional
    public void resolveOutgoingDistrict(Payment payment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        addressService.resolveOutgoingDistrict(payment, calculationCenterId, adapter);
    }

    @Transactional
    public void resolveOutgoingDistrict(ActualPayment actualPayment) {
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        addressService.resolveOutgoingDistrict(actualPayment, calculationCenterId, adapter);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByOsznAccount(Payment payment) throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByOsznAccount(payment);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByMegabankAccount(AbstractRequest request, String district, String megabankAccount)
            throws DBException {
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        return adapter.acquireAccountDetailsByMegabankAccount(request, district, megabankAccount);
    }
}
