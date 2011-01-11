/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
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

    /**
     * Попытаться разрешить номер личного счета локально, т.е. из локальной таблицы person_account
     * Если успешно, то просиавить account number, статус в RequestStatus.ACCOUNT_NUMBER_RESOLVED и обновить account number для всех benefit записей,
     * соответствующих данному payment.
     * @param payment
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

    /**
     * Попытаться разрешить номер л/с в ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquirePersonAccount()
     * Если успешно, то обновить account number для всех benefit записей,
     * соответствующих данному payment и записать в локальную таблицу номеров л/c(person_account) найденный номер.
     * @param payment
     * @param calculationCenterId
     * @param adapter
     */
    @Transactional
    public void resolveRemoteAccount(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) throws DBException {
        adapter.acquirePersonAccount(payment);
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

    /**
     * Корректировать account number из UI в случае когда в ЦН большне одного человека соотвествуют номеру л/c.
     * @param payment
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

    /**
     * Получает детальную информацию о номерах л/с, фамилии, ИНН в случае когда к одному адресу в ЦН привязано несколько человек.
     * Делегирует всю работу реализации адаптера взаимодействия с ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquireAccountCorrectionDetails()
     * @param payment
     * @return
     */
    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) throws DBException {
        return calculationCenterBean.getDefaultCalculationCenterAdapter().acquireAccountDetailsByAddress(payment);
    }
}
