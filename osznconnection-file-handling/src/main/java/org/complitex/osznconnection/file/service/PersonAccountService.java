/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFileGroup;

/**
 * Разрешает номер л/c
 * @author Artem
 */
@Stateless
public class PersonAccountService extends AbstractBean {

    @EJB(beanName = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;
    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;
    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;
    @EJB
    private ActualPaymentBean actualPaymentBean;

    /**
     * Попытаться разрешить номер личного счета локально, т.е. из локальной таблицы person_account
     * Если успешно, то просиавить account number, статус в RequestStatus.ACCOUNT_NUMBER_RESOLVED и обновить account number для всех benefit записей,
     * соответствующих данному payment.
     * @param actualPayment
     * @param calculationCenterId
     */
    @Transactional
    public void resolveLocalAccount(Payment payment, long calculationCenterId) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.N_NAME), null, (String) payment.getField(PaymentDBF.VUL_NAME), null,
                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                (String) payment.getField(PaymentDBF.FLAT), (String) payment.getField(PaymentDBF.OWN_NUM_SR),
                payment.getOrganizationId(), calculationCenterId);
        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        }
    }

    @Transactional
    public void resolveLocalAccount(ActualPayment actualPayment, long calculationCenterId) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber((String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT),
                (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CODE),
                (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM), (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM),
                (String) actualPayment.getField(ActualPaymentDBF.FLAT), null,
                actualPayment.getOrganizationId(), calculationCenterId);
        if (!Strings.isEmpty(accountNumber)) {
            actualPayment.setAccountNumber(accountNumber);
            actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(actualPayment.getId(), accountNumber);
        }
    }

    /**
     * Попытаться разрешить номер л/с в ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquirePersonAccount()
     * Если успешно, то обновить account number для всех benefit записей,
     * соответствующих данному payment и записать в локальную таблицу номеров л/c(person_account) найденный номер.
     * @param actualPayment
     * @param calculationCenterId
     * @param adapter
     */
    @Transactional
    public void resolveRemoteAccount(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) throws DBException {
        adapter.acquirePersonAccount(payment, payment.getOutgoingDistrict(), payment.getOutgoingStreetType(), payment.getOutgoingStreet(),
                payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(), payment.getOutgoingApartment(),
                (Date) payment.getField(PaymentDBF.DAT1));
        if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());
            personAccountLocalBean.saveOrUpdate(payment.getAccountNumber(), (String) payment.getField(PaymentDBF.F_NAM),
                    (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                    (String) payment.getField(PaymentDBF.N_NAME), null, (String) payment.getField(PaymentDBF.VUL_NAME), null,
                    (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                    (String) payment.getField(PaymentDBF.FLAT), (String) payment.getField(PaymentDBF.OWN_NUM_SR),
                    payment.getOrganizationId(), calculationCenterId);
        }
    }

    @Transactional
    public void resolveRemoteAccount(ActualPayment actualPayment, long calculationCenterId, ICalculationCenterAdapter adapter) throws DBException {
        adapter.acquirePersonAccount(actualPayment, actualPayment.getOutgoingDistrict(), actualPayment.getOutgoingStreetType(),
                actualPayment.getOutgoingStreet(), actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), (Date) actualPayment.getField(ActualPaymentDBF.DAT_BEG));
        if (actualPayment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(actualPayment.getId(), actualPayment.getAccountNumber());
            personAccountLocalBean.saveOrUpdate(actualPayment.getAccountNumber(), (String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                    (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                    (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT),
                    (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CODE),
                    (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM), (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM),
                    (String) actualPayment.getField(ActualPaymentDBF.FLAT), null,
                    actualPayment.getOrganizationId(), calculationCenterId);
        }
    }

    /**
     * Корректировать account number из UI в случае когда в ЦН больше одного человека соотвествуют номеру л/c.
     * @param actualPayment
     * @param accountNumber
     */
    @Transactional
    public void correctAccountNumber(Payment payment, String accountNumber) {
        payment.setAccountNumber(accountNumber);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        paymentBean.update(payment);

        long paymentFileId = payment.getRequestFileId();
        long benefitFileId = requestFileGroupBean.getBenefitFileId(paymentFileId);
        if (benefitBean.isBenefitFileBound(benefitFileId) && paymentBean.isPaymentFileBound(paymentFileId)) {
            requestFileGroupBean.updateStatus(benefitFileId, RequestFileGroup.STATUS.BOUND);
        }

        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        personAccountLocalBean.saveOrUpdate(payment.getAccountNumber(), (String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.N_NAME), null, (String) payment.getField(PaymentDBF.VUL_NAME), null,
                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                (String) payment.getField(PaymentDBF.FLAT), (String) payment.getField(PaymentDBF.OWN_NUM_SR),
                payment.getOrganizationId(), calculationCenterId);
    }

    @Transactional
    public void correctAccountNumber(ActualPayment actualPayment, String accountNumber) {
        actualPayment.setAccountNumber(accountNumber);
        actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        actualPaymentBean.update(actualPayment);

        //TODO: fix logic
//        long paymentFileId = actualPayment.getRequestFileId();
//        long benefitFileId = requestFileGroupBean.getBenefitFileId(paymentFileId);
//        if (benefitBean.isBenefitFileBound(benefitFileId) && paymentBean.isPaymentFileBound(paymentFileId)) {
//            requestFileGroupBean.updateStatus(benefitFileId, RequestFileGroup.STATUS.BOUND);
//        }

        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        personAccountLocalBean.saveOrUpdate(actualPayment.getAccountNumber(), (String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT),
                (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_CODE),
                (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM), (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM),
                (String) actualPayment.getField(ActualPaymentDBF.FLAT), null,
                actualPayment.getOrganizationId(), calculationCenterId);
    }
}
