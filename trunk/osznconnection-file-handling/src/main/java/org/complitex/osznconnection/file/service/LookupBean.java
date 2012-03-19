/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;

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
    private RequestFileBean requestFileBean;
    @EJB
    private ActualPaymentBean actualPaymentBean;
    @EJB
    private ServiceProviderAdapter adapter;

    /**
     * Разрешить исходящий в ЦН адрес по схеме "локальная адресная база -> адрес центра начислений"
     * Делегирует всю работу AddressService.resolveOutgoingAddress().
     * @param request
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment, long userOrganizationId) {
        addressService.resolveOutgoingAddress(payment, calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId));
    }

    @Transactional
    public void resolveOutgoingAddress(ActualPayment actualPayment, long userOrganizationId) {
        addressService.resolveOutgoingAddress(actualPayment, calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId));
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
    private List<AccountDetail> acquireAccountDetailsByAddress(AbstractRequest request, String district, String streetType, String street,
            String buildingNumber, String buildingCorp, String apartment, Date date, long userOrganizationId) throws DBException {
        return adapter.acquireAccountDetailsByAddress(calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId), request,
                district, streetType, street, buildingNumber, buildingCorp, apartment, date);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(Payment payment, long userOrganizationId) throws DBException {
        return acquireAccountDetailsByAddress(payment, payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1), userOrganizationId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(ActualPayment actualPayment, long userOrganizationId) throws DBException {
        RequestFile actualPaymentFile = requestFileBean.findById(actualPayment.getRequestFileId());
        return acquireAccountDetailsByAddress(actualPayment, actualPayment.getOutgoingDistrict(), actualPayment.getOutgoingStreetType(),
                actualPayment.getOutgoingStreet(), actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), actualPaymentBean.getFirstDay(actualPayment, actualPaymentFile), userOrganizationId);
    }

    @Transactional
    public String resolveOutgoingDistrict(Payment payment, long userOrganizationId) {
        payment.setStatus(RequestStatus.LOADED);
        addressService.resolveOutgoingDistrict(payment, calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId));
        if (!(payment.getStatus() == RequestStatus.DISTRICT_UNRESOLVED
                || payment.getStatus() == RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)) {
            return payment.getOutgoingDistrict();
        } else {
            return null;
        }
    }

    @Transactional
    public String resolveOutgoingDistrict(ActualPayment actualPayment, long userOrganizationId) {
        actualPayment.setStatus(RequestStatus.LOADED);
        addressService.resolveOutgoingDistrict(actualPayment, calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId));
        if (!(actualPayment.getStatus() == RequestStatus.DISTRICT_UNRESOLVED
                || actualPayment.getStatus() == RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)) {
            return actualPayment.getOutgoingDistrict();
        } else {
            return null;
        }
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAccount(AbstractRequest request, String district, String account, 
        long userOrganizationId) throws DBException, UnknownAccountNumberTypeException {
        return adapter.acquireAccountDetailsByAccount(calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId), 
                request, district, account);
    }
}
