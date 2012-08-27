/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean.MoreOneAccountException;
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
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;
    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;
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
        try {
            String accountNumber = personAccountLocalBean.findLocalAccountNumber(payment, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
            if (!Strings.isEmpty(accountNumber)) {
                payment.setAccountNumber(accountNumber);
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
                benefitBean.updateAccountNumber(payment.getId(), accountNumber);
            }
        } catch (MoreOneAccountException e) {
            payment.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    @Transactional
    public void resolveLocalAccount(ActualPayment actualPayment, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountLocalBean.findLocalAccountNumber(actualPayment, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
            if (!Strings.isEmpty(accountNumber)) {
                actualPayment.setAccountNumber(accountNumber);
                actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    @Transactional
    public void resolveLocalAccount(Subsidy subsidy, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountLocalBean.findLocalAccountNumber(subsidy, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
            if (!Strings.isEmpty(accountNumber)) {
                subsidy.setAccountNumber(accountNumber);
                subsidy.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            subsidy.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    @Transactional
    public void resolveLocalAccount(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountLocalBean.findLocalAccountNumber(dwellingCharacteristics, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
            if (!Strings.isEmpty(accountNumber)) {
                dwellingCharacteristics.setAccountNumber(accountNumber);
                dwellingCharacteristics.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    @Transactional
    public void resolveLocalAccount(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountLocalBean.findLocalAccountNumber(facilityServiceType, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
            if (!Strings.isEmpty(accountNumber)) {
                facilityServiceType.setAccountNumber(accountNumber);
                facilityServiceType.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
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
            personAccountLocalBean.saveOrUpdate(subsidy, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
        }
    }

    @Transactional
    public void resolveRemoteAccount(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext,
            Boolean updatePUAccount) throws DBException {
        adapter.acquirePersonAccount(calculationContext, RequestFile.TYPE.DWELLING_CHARACTERISTICS, dwellingCharacteristics,
                dwellingCharacteristics.getLastName(), dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE),
                dwellingCharacteristics.getOutgoingDistrict(), dwellingCharacteristics.getOutgoingStreetType(),
                dwellingCharacteristics.getOutgoingStreet(),
                dwellingCharacteristics.getOutgoingBuildingNumber(), dwellingCharacteristics.getOutgoingBuildingCorp(),
                dwellingCharacteristics.getOutgoingApartment(), dwellingCharacteristics.getDate(), updatePUAccount);
        if (dwellingCharacteristics.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            personAccountLocalBean.saveOrUpdate(dwellingCharacteristics, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
        }
    }

    @Transactional
    public void resolveRemoteAccount(FacilityServiceType facilityServiceType, CalculationContext calculationContext,
            Boolean updatePUAccount) throws DBException {
        adapter.acquirePersonAccount(calculationContext, RequestFile.TYPE.DWELLING_CHARACTERISTICS, facilityServiceType,
                facilityServiceType.getLastName(), facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE),
                facilityServiceType.getOutgoingDistrict(), facilityServiceType.getOutgoingStreetType(),
                facilityServiceType.getOutgoingStreet(),
                facilityServiceType.getOutgoingBuildingNumber(), facilityServiceType.getOutgoingBuildingCorp(),
                facilityServiceType.getOutgoingApartment(), facilityServiceType.getDate(), updatePUAccount);
        if (facilityServiceType.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            personAccountLocalBean.saveOrUpdate(facilityServiceType, calculationContext.getCalculationCenterId(),
                    calculationContext.getUserOrganizationId());
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
        subsidyBean.updateAccountNumberForSimilarSubs(subsidy);

        long subsidyFileId = subsidy.getRequestFileId();
        RequestFile subsidyFile = requestFileBean.findById(subsidyFileId);
        if (subsidyBean.isSubsidyFileBound(subsidyFileId)) {
            subsidyFile.setStatus(RequestFileStatus.BOUND);
            requestFileBean.save(subsidyFile);
        }

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        personAccountLocalBean.saveOrUpdate(subsidy, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
    }

    @Transactional
    public void updateAccountNumber(DwellingCharacteristics dwellingCharacteristics, String accountNumber, long userOrganizationId) {
        dwellingCharacteristics.setAccountNumber(accountNumber);
        dwellingCharacteristics.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        dwellingCharacteristicsBean.updateAccountNumber(dwellingCharacteristics);

        long dwellingCharacteristicsFileId = dwellingCharacteristics.getRequestFileId();
        RequestFile dwellingCharacteristicsFile = requestFileBean.findById(dwellingCharacteristicsFileId);
        if (dwellingCharacteristicsBean.isDwellingCharacteristicsFileBound(dwellingCharacteristicsFileId)) {
            dwellingCharacteristicsFile.setStatus(RequestFileStatus.BOUND);
            requestFileBean.save(dwellingCharacteristicsFile);
        }

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        personAccountLocalBean.saveOrUpdate(dwellingCharacteristics, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
    }

    @Transactional
    public void updateAccountNumber(FacilityServiceType facilityServiceType, String accountNumber, long userOrganizationId) {
        facilityServiceType.setAccountNumber(accountNumber);
        facilityServiceType.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        facilityServiceTypeBean.updateAccountNumber(facilityServiceType);

        long facilityServiceTypeFileId = facilityServiceType.getRequestFileId();
        RequestFile facilityServiceTypeFile = requestFileBean.findById(facilityServiceTypeFileId);
        if (dwellingCharacteristicsBean.isDwellingCharacteristicsFileBound(facilityServiceTypeFileId)) {
            facilityServiceTypeFile.setStatus(RequestFileStatus.BOUND);
            requestFileBean.save(facilityServiceTypeFile);
        }

        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);
        personAccountLocalBean.saveOrUpdate(facilityServiceType, calculationContext.getCalculationCenterId(),
                calculationContext.getUserOrganizationId());
    }
}
