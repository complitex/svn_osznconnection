package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.MoreOneAccountException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

import static org.complitex.osznconnection.file.entity.PaymentDBF.OWN_NUM_SR;

/**
 * Разрешает номер л/c
 * @author Artem
 */
@Stateless
public class PersonAccountService extends AbstractBean {
    private final Logger log = LoggerFactory.getLogger(PersonAccountService.class);

    @EJB
    private PersonAccountBean personAccountBean;

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
    private SubsidyService subsidyService;

    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;

    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private ServiceProviderAdapter serviceProviderAdapter;

    public String getAccountNumber(AbstractAccountRequest request, String puPersonAccount, Long calculationCenterId)
            throws MoreOneAccountException {
        List<PersonAccount> personAccounts = personAccountBean.getPersonAccounts(FilterWrapper.of(new PersonAccount(request,
                puPersonAccount, calculationCenterId)));

        if (personAccounts.size() == 1){
            return personAccounts.get(0).getAccountNumber();
        }else if (personAccounts.size() > 1){
            throw new MoreOneAccountException();
        }

        return null;
    }

    public void save(AbstractAccountRequest request, String puPersonAccount, Long calculationCenterId)
            throws MoreOneAccountException {
        List<PersonAccount> personAccounts = personAccountBean.getPersonAccounts(FilterWrapper.of(new PersonAccount(request,
                puPersonAccount, calculationCenterId)));
        if (personAccounts.isEmpty()){
            personAccountBean.save(new PersonAccount(request, puPersonAccount, calculationCenterId));
        }else if (personAccounts.size() == 1){
            PersonAccount personAccount = personAccounts.get(0);

            if (!personAccount.getAccountNumber().equals(request.getAccountNumber())){
                personAccountBean.save(personAccount);
            }
        }else {
            throw new MoreOneAccountException();
        }
    }

    public void resolveAccountNumber(AbstractAccountRequest request, String puPersonAccount,
                                     String servicingOrganizationCode, CalculationContext calculationContext,
                                     boolean updatePuAccount) throws DBException {
        try {
            //resolve local account
            String accountNumber = getAccountNumber(request, puPersonAccount, calculationContext.getCalculationCenterId());

            if (accountNumber != null) {
                request.setAccountNumber(accountNumber);
                request.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);

                return;
            }

            //resolve remote account
            AccountDetail accountDetail = serviceProviderAdapter.acquireAccountDetail(calculationContext, request,
                    request.getLastName(), puPersonAccount,
                    request.getOutgoingDistrict(), request.getOutgoingStreetType(), request.getOutgoingStreet(),
                    request.getOutgoingBuildingNumber(), request.getOutgoingBuildingCorp(),
                    request.getOutgoingApartment(), request.getDate(), updatePuAccount);

            if (request.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
                //check servicing organization
                if (servicingOrganizationCode != null && !servicingOrganizationCode.equals(accountDetail.getZheu())){
                    request.setStatus(RequestStatus.SERVICING_ORGANIZATION_NOT_FOUND);

                    return;
                }

                save(request, puPersonAccount, calculationContext.getCalculationCenterId());
            }
        } catch (MoreOneAccountException e) {
            request.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    /**
     * Корректировать account number из UI в случае когда в ЦН больше одного человека соответствуют номеру л/c.
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

        try {
            save(payment, payment.getStringField(PaymentDBF.OWN_NUM_SR), calculationContext.getCalculationCenterId());
        } catch (MoreOneAccountException e) {
            throw new RuntimeException(e);
        }
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
        try {
            save(actualPayment, actualPayment.getStringField(ActualPaymentDBF.OWN_NUM),
                    calculationContext.getCalculationCenterId());
        } catch (MoreOneAccountException e) {
            throw new RuntimeException(e);
        }
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

        try {
            save(subsidy, subsidy.getStringField(SubsidyDBF.RASH), calculationCenterBean.getContextWithAnyCalculationCenter(
                    userOrganizationId).getCalculationCenterId());
        } catch (MoreOneAccountException e) {
            throw new RuntimeException(e);
        }
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
        try {
            save(dwellingCharacteristics, dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE),
                    calculationContext.getCalculationCenterId());
        } catch (MoreOneAccountException e) {
            throw new RuntimeException(e);
        }
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
        try {
            save(facilityServiceType, facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE),
                    calculationContext.getCalculationCenterId());
        } catch (MoreOneAccountException e) {
            throw new RuntimeException(e);
        }
    }
}
