package org.complitex.osznconnection.file.service;

import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.correction.entity.DistrictCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.List;

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

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private OsznOrganizationStrategy organizationStrategy;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private LocaleBean localeBean;

    /**
     * Разрешить исходящий в ЦН адрес по схеме "локальная адресная база -> адрес центра начислений"
     * Делегирует всю работу AddressService.resolveOutgoingAddress().
     * @param request
     */
    @Transactional
    public void resolveOutgoingAddress(AbstractAccountRequest request, long userOrganizationId) {
        addressService.resolveOutgoingAddress(request, calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId));
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
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(Subsidy subsidy, long userOrganizationId) throws DBException {
        return acquireAccountDetailsByAddress(subsidy, subsidy.getOutgoingDistrict(), subsidy.getOutgoingStreetType(),
                subsidy.getOutgoingStreet(), subsidy.getOutgoingBuildingNumber(), subsidy.getOutgoingBuildingCorp(),
                subsidy.getOutgoingApartment(), (Date) subsidy.getField(SubsidyDBF.DAT1), userOrganizationId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId)
            throws DBException {
        return acquireAccountDetailsByAddress(dwellingCharacteristics, dwellingCharacteristics.getOutgoingDistrict(),
                dwellingCharacteristics.getOutgoingStreetType(),
                dwellingCharacteristics.getOutgoingStreet(), dwellingCharacteristics.getOutgoingBuildingNumber(),
                dwellingCharacteristics.getOutgoingBuildingCorp(),
                dwellingCharacteristics.getOutgoingApartment(), dwellingCharacteristics.getDate(), userOrganizationId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAddress(FacilityServiceType facilityServiceType, long userOrganizationId)
            throws DBException {
        return acquireAccountDetailsByAddress(facilityServiceType, facilityServiceType.getOutgoingDistrict(),
                facilityServiceType.getOutgoingStreetType(),
                facilityServiceType.getOutgoingStreet(), facilityServiceType.getOutgoingBuildingNumber(),
                facilityServiceType.getOutgoingBuildingCorp(),
                facilityServiceType.getOutgoingApartment(), facilityServiceType.getDate(), userOrganizationId);
    }

    @Transactional
    public String resolveOutgoingDistrict(AbstractRequest request, long userOrganizationId) {
        request.setStatus(RequestStatus.LOADED);

        CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId);

        List<DistrictCorrection> districtCorrections = addressCorrectionBean.getDistrictCorrections(null, null,
                calculationContext.getCalculationCenterId(), userOrganizationId);

        if (districtCorrections.isEmpty()){
            DomainObject organization = organizationStrategy.findById(request.getOrganizationId(), true);

            Long districtId = organization.getAttribute(IOrganizationStrategy.DISTRICT).getValueId();
            DomainObject district = districtStrategy.findById(districtId, true);



            if (district != null){
                return districtStrategy.displayDomainObject(district, localeBean.getSystemLocale());
            }

        } else if (districtCorrections.size() == 1) {
            return districtCorrections.get(0).getCorrection();
        }

        return null;
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountDetailsByAccount(AbstractRequest request, String district, String account,
            long userOrganizationId) throws DBException, UnknownAccountNumberTypeException {
        return adapter.acquireAccountDetailsByAccount(calculationCenterBean.getContextWithAnyCalculationCenter(userOrganizationId),
                request, district, account);
    }
}
