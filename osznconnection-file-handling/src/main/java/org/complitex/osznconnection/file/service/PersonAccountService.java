/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import java.util.List;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 * Разрешает номер л/c
 * @author Artem
 */
@Stateless
public class PersonAccountService extends AbstractBean {

    @EJB
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB
    private BenefitBean benefitBean;
    @EJB
    private PaymentBean paymentBean;
    @EJB
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;
    @EJB
    private ActualPaymentBean actualPaymentBean;
    @EJB
    private SubsidyBean subsidyBean;
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private ServiceProviderAdapter adapter;

    /**
     * Попытаться разрешить номер личного счета локально, т.е. из локальной таблицы person_account
     * Если успешно, то просиавить account number, статус в RequestStatus.ACCOUNT_NUMBER_RESOLVED и обновить account number для всех benefit записей,
     * соответствующих данному payment.
     * @param actualPayment
     * @param calculationCenterId
     */
    @Transactional
    public void resolveLocalAccount(Payment payment, CalculationContext calculationContext) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(payment, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        }
    }

    @Transactional
    public void resolveLocalAccount(ActualPayment actualPayment, CalculationContext calculationContext) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(actualPayment, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
        if (!Strings.isEmpty(accountNumber)) {
            actualPayment.setAccountNumber(accountNumber);
            actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        }
    }

    @Transactional
    public void resolveLocalAccount(Subsidy subsidy, CalculationContext calculationContext) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(subsidy, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
        if (!Strings.isEmpty(accountNumber)) {
            subsidy.setAccountNumber(accountNumber);
            subsidy.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
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
    public void resolveRemoteAccount(Payment payment, CalculationContext calculationContext,
            Boolean updatePUAccount) throws DBException {
        adapter.acquirePersonAccount(calculationContext, RequestFile.TYPE.PAYMENT, payment,
                payment.getStringField(PaymentDBF.SUR_NAM),
                payment.getStringField(PaymentDBF.OWN_NUM_SR), payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1), updatePUAccount);
        if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());
            personAccountLocalBean.saveOrUpdate(payment, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
        }
    }

    @Transactional
    public void resolveRemoteAccount(ActualPayment actualPayment, Date date, CalculationContext calculationContext,
            Boolean updatePUAccount) throws DBException {
        adapter.acquirePersonAccount(calculationContext, RequestFile.TYPE.ACTUAL_PAYMENT, actualPayment,
                actualPayment.getStringField(ActualPaymentDBF.SUR_NAM),
                actualPayment.getStringField(ActualPaymentDBF.OWN_NUM), actualPayment.getOutgoingDistrict(),
                actualPayment.getOutgoingStreetType(), actualPayment.getOutgoingStreet(),
                actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), date, updatePUAccount);
        if (actualPayment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            personAccountLocalBean.saveOrUpdate(actualPayment, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
        }
    }

    @Transactional
    public void resolveRemoteAccount(Subsidy subsidy, CalculationContext calculationContext,
            Boolean updatePUAccount) throws DBException {
        adapter.acquirePersonAccount(calculationContext, RequestFile.TYPE.SUBSIDY, subsidy,
                subsidy.getLastName(), subsidy.getStringField(SubsidyDBF.RASH),
                subsidy.getOutgoingDistrict(), subsidy.getOutgoingStreetType(), subsidy.getOutgoingStreet(),
                subsidy.getOutgoingBuildingNumber(), subsidy.getOutgoingBuildingCorp(),
                subsidy.getOutgoingApartment(), (Date) subsidy.getField(SubsidyDBF.DAT1), updatePUAccount);
        if (subsidy.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            createLocalAccountsForSubsidyWithTheSameRash(subsidy, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
        }
    }

    @Transactional
    private void createLocalAccountsForSubsidyWithTheSameRash(Subsidy subsidy, long calculationCenterId, long userOrganizationId) {
        List<Subsidy> subsWithSameRash = subsidyBean.findWithTheSameRash(subsidy.getRequestFileId(),
                subsidy.getStringField(SubsidyDBF.RASH));
        if (subsWithSameRash != null && !subsWithSameRash.isEmpty()) {
            for (Subsidy s : subsWithSameRash) {
                s.setAccountNumber(subsidy.getAccountNumber());
                personAccountLocalBean.saveOrUpdate(s, calculationCenterId, userOrganizationId);
            }
        }
    }

    /**
     * Корректировать account number из UI в случае когда в ЦН больше одного человека соотвествуют номеру л/c.
     * @param actualPayment
     * @param accountNumber
     */
    @Transactional
    public void updateAccountNumber(Payment payment, String accountNumber, long userOrganizationId) {
        payment.setAccountNumber(accountNumber);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        paymentBean.updateAccountNumber(payment);

        long paymentFileId = payment.getRequestFileId();
        long benefitFileId = requestFileGroupBean.getBenefitFileId(paymentFileId);
        if (benefitBean.isBenefitFileBound(benefitFileId) && paymentBean.isPaymentFileBound(paymentFileId)) {
            requestFileGroupBean.updateStatus(benefitFileId, RequestFileStatus.BOUND);
        }

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        personAccountLocalBean.saveOrUpdate(payment, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
    }

    @Transactional
    public void updateAccountNumber(ActualPayment actualPayment, String accountNumber, long userOrganizationId) {
        actualPayment.setAccountNumber(accountNumber);
        actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        actualPaymentBean.updateAccountNumber(actualPayment);

        long actualPaymentFileId = actualPayment.getRequestFileId();
        RequestFile actualPaymentFile = requestFileBean.findById(actualPaymentFileId);
        if (actualPaymentBean.isActualPaymentFileBound(actualPaymentFileId)) {
            actualPaymentFile.setStatus(RequestFileStatus.BOUND);
            requestFileBean.save(actualPaymentFile);
        }

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        personAccountLocalBean.saveOrUpdate(actualPayment, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
    }

    @Transactional
    public void updateAccountNumber(Subsidy subsidy, String accountNumber, long userOrganizationId) {
        subsidy.setAccountNumber(accountNumber);
        subsidy.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        createLocalAccountsForSubsidyWithTheSameRash(subsidy, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());

        subsidyBean.updateAccountNumber(subsidy);
        long subsidyFileId = subsidy.getRequestFileId();
        RequestFile subsidyFile = requestFileBean.findById(subsidyFileId);
        if (subsidyBean.isSubsidyFileBound(subsidyFileId)) {
            subsidyFile.setStatus(RequestFileStatus.BOUND);
            requestFileBean.save(subsidyFile);
        }
    }
}
