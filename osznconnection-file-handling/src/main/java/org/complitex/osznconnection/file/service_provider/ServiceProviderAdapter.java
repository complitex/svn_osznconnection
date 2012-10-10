/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.ActualPaymentData;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.BenefitData;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.entity.DwellingCharacteristics;
import org.complitex.osznconnection.file.entity.FacilityServiceType;
import org.complitex.osznconnection.file.entity.FacilityServiceTypeDBF;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentAndBenefitData;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.RequestWarning;
import org.complitex.osznconnection.file.entity.RequestWarningParameter;
import org.complitex.osznconnection.file.entity.RequestWarningStatus;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.service.OwnershipCorrectionBean;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.service.SubsidyTarifBean;
import org.complitex.osznconnection.file.service.warning.RequestWarningBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.ServiceProviderAccountNumberParseException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;
import org.complitex.dictionary.oracle.OracleErrors;
import org.complitex.osznconnection.service_provider_type.strategy.ServiceProviderTypeStrategy;
import static org.complitex.osznconnection.file.service_provider.util.ServiceProviderAccountNumberParser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.complitex.dictionary.util.StringUtil.removeWhiteSpaces;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ServiceProviderAdapter {

    private static final Logger log = LoggerFactory.getLogger(ServiceProviderAdapter.class);
    private static final String RESOURCE_BUNDLE = ServiceProviderAdapter.class.getName();
    private static final String MAPPING_NAMESPACE = ServiceProviderAdapter.class.getName();
    @EJB
    private ServiceProviderSqlSessionFactoryBean sqlSessionFactoryBean;
    @EJB
    private OwnershipCorrectionBean ownershipCorrectionBean;
    @EJB
    private SubsidyTarifBean subsidyTarifBean;
    @EJB
    private PrivilegeCorrectionBean privilegeCorrectionBean;
    @EJB
    private LogBean logBean;
    @EJB
    private LocaleBean localeBean;
    @EJB
    private RequestWarningBean warningBean;
    @EJB
    private WebWarningRenderer webWarningRenderer;

    /**
     * Группа методов для проставления "внешнего адреса", т.е. адреса для ЦН, по полному названию и коду коррекции
     * элемента адреса, полученному из таблиц коррекций.
     */
    public void prepareCity(Payment payment, String city, String cityCode) {
        payment.setOutgoingCity(city);
    }

    public void prepareDistrict(Payment payment, String district, String districtCode) {
        payment.setOutgoingDistrict(district);
    }

    public void prepareStreet(Payment payment, String street, String streetCode) {
        payment.setOutgoingStreet(street);
    }

    public void prepareStreetType(Payment payment, String streetType, String streetTypeCode) {
        payment.setOutgoingStreetType(streetType);
    }

    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, String buildingCode) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param apartment
     * @param apartmentCode
     */
    public void prepareApartment(Payment payment, String apartment, String apartmentCode) {
        String flat = payment.getStringField(PaymentDBF.FLAT);
        payment.setOutgoingApartment(removeWhiteSpaces(flat));
    }

    public void prepareCity(ActualPayment actualPayment, String city, String cityCode) {
        actualPayment.setOutgoingCity(city);
    }

    public void prepareDistrict(ActualPayment actualPayment, String district, String districtCode) {
        actualPayment.setOutgoingDistrict(district);
    }

    public void prepareStreet(ActualPayment actualPayment, String street, String streetCode) {
        actualPayment.setOutgoingStreet(street);
    }

    public void prepareStreetType(ActualPayment actualPayment, String streetType, String streetTypeCode) {
        actualPayment.setOutgoingStreetType(streetType);
    }

    public void prepareBuilding(ActualPayment actualPayment, String buildingNumber, String buildingCorp, String buildingCode) {
        actualPayment.setOutgoingBuildingNumber(buildingNumber);
        actualPayment.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param actualPayment
     * @param apartment
     * @param apartmentCode
     */
    public void prepareApartment(ActualPayment actualPayment, String apartment, String apartmentCode) {
        String flat = actualPayment.getStringField(ActualPaymentDBF.FLAT);
        actualPayment.setOutgoingApartment(removeWhiteSpaces(flat));
    }

    public void prepareCity(Subsidy subsidy, String city, String cityCode) {
        subsidy.setOutgoingCity(city);
    }

    public void prepareDistrict(Subsidy subsidy, String district, String districtCode) {
        subsidy.setOutgoingDistrict(district);
    }

    public void prepareStreet(Subsidy subsidy, String street, String streetCode) {
        subsidy.setOutgoingStreet(street);
    }

    public void prepareStreetType(Subsidy subsidy, String streetType, String streetTypeCode) {
        subsidy.setOutgoingStreetType(streetType);
    }

    public void prepareBuilding(Subsidy subsidy, String buildingNumber, String buildingCorp, String buildingCode) {
        subsidy.setOutgoingBuildingNumber(buildingNumber);
        subsidy.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param subsidy
     * @param apartment
     * @param apartmentCode
     */
    public void prepareApartment(Subsidy subsidy, String apartment, String apartmentCode) {
        String flat = subsidy.getStringField(SubsidyDBF.FLAT);
        subsidy.setOutgoingApartment(removeWhiteSpaces(flat));
    }

    public void prepareCity(DwellingCharacteristics dwellingCharacteristics, String city, String cityCode) {
        dwellingCharacteristics.setOutgoingCity(city);
    }

    public void prepareDistrict(DwellingCharacteristics dwellingCharacteristics, String district, String districtCode) {
        dwellingCharacteristics.setOutgoingDistrict(district);
    }

    public void prepareStreet(DwellingCharacteristics dwellingCharacteristics, String street, String streetCode) {
        dwellingCharacteristics.setOutgoingStreet(street);
    }

    public void prepareStreetType(DwellingCharacteristics dwellingCharacteristics, String streetType, String streetTypeCode) {
        dwellingCharacteristics.setOutgoingStreetType(streetType);
    }

    public void prepareBuilding(DwellingCharacteristics dwellingCharacteristics, String buildingNumber, String buildingCorp, String buildingCode) {
        dwellingCharacteristics.setOutgoingBuildingNumber(buildingNumber);
        dwellingCharacteristics.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param dwellingCharacteristics
     * @param apartment
     * @param apartmentCode
     */
    public void prepareApartment(DwellingCharacteristics dwellingCharacteristics, String apartment, String apartmentCode) {
        String flat = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT);
        dwellingCharacteristics.setOutgoingApartment(removeWhiteSpaces(flat));
    }

    public void prepareCity(FacilityServiceType facilityServiceType, String city, String cityCode) {
        facilityServiceType.setOutgoingCity(city);
    }

    public void prepareDistrict(FacilityServiceType facilityServiceType, String district, String districtCode) {
        facilityServiceType.setOutgoingDistrict(district);
    }

    public void prepareStreet(FacilityServiceType facilityServiceType, String street, String streetCode) {
        facilityServiceType.setOutgoingStreet(street);
    }

    public void prepareStreetType(FacilityServiceType facilityServiceType, String streetType, String streetTypeCode) {
        facilityServiceType.setOutgoingStreetType(streetType);
    }

    public void prepareBuilding(FacilityServiceType facilityServiceType, String buildingNumber, String buildingCorp, String buildingCode) {
        facilityServiceType.setOutgoingBuildingNumber(buildingNumber);
        facilityServiceType.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param facilityServiceType
     * @param apartment
     * @param apartmentCode
     */
    public void prepareApartment(FacilityServiceType facilityServiceType, String apartment, String apartmentCode) {
        String flat = facilityServiceType.getStringField(FacilityServiceTypeDBF.APT);
        facilityServiceType.setOutgoingApartment(removeWhiteSpaces(flat));
    }

    /**
     * Получить номер личного счета в ЦН.
     * 
     * Обработка возвращаемых значений при получении л/с.
     * 0 - нет л/с,
     * -1 - больше 1 л/с, когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
     * -2 - нет квартиры,
     * -3 - нет корпуса,
     * -4 - нет дома,
     * -5 - нет улицы,
     * -6 - нет типа улицы,
     * -7 - нет района,
     * остальное - номер л/с
     *
     */
    public void acquirePersonAccount(CalculationContext calculationContext,
            RequestFile.TYPE requestFileType, AbstractRequest request, String lastName,
            String serviceProviderAccountNumber, String district, String streetType, String street, String buildingNumber,
            String buildingCorp, String apartment, Date date, Boolean updatePUAccount) throws DBException {

        if (Strings.isEmpty(serviceProviderAccountNumber)) {
            serviceProviderAccountNumber = "0";
        }
        serviceProviderAccountNumber = serviceProviderAccountNumber.trim();

        List<AccountDetail> accountDetails = acquireAccountDetailsByAddress(calculationContext, request,
                district, streetType, street, buildingNumber, buildingCorp, apartment, date);
        if (accountDetails == null || accountDetails.isEmpty()) {
            return;
        }

        Collection<AccountDetail> errorDetails = newArrayList();
        for (AccountDetail accountDetail : accountDetails) {
            ServiceProviderAccountNumberInfo serviceProviderAccountNumberInfo = null;
            try {
                serviceProviderAccountNumberInfo = parse(accountDetail.getServiceProviderAccountNumberInfo());
            } catch (ServiceProviderAccountNumberParseException e) {
                errorDetails.add(accountDetail);
                continue;
            }
            if (serviceProviderAccountNumber.length() < serviceProviderAccountNumberInfo.getServiceProviderAccountNumber().length()) {
                continue;
            }
            if (serviceProviderAccountNumber.equals(serviceProviderAccountNumberInfo.getServiceProviderAccountNumber())
                    || matches(serviceProviderAccountNumber, serviceProviderAccountNumberInfo.getServiceProviderAccountNumber())
                    || matches(serviceProviderAccountNumber, serviceProviderAccountNumberInfo.getServiceProviderId(),
                    serviceProviderAccountNumberInfo.getServiceProviderAccountNumber())
                    || matches(serviceProviderAccountNumber, lastName,
                    serviceProviderAccountNumberInfo.getServiceProviderAccountNumber(), accountDetail.getOwnerName())
                    || isMegabankAccount(serviceProviderAccountNumber, accountDetail.getMegabankAccountNumber())
                    || isCalcCenterAccount(serviceProviderAccountNumber, accountDetail.getAccountNumber())) {
                request.setAccountNumber(accountDetail.getAccountNumber());
                request.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
                return;
            }
        }
        if (accountDetails.size() == 1) {

            // если установлена опция перезаписи номера л/с ПУ номером л/с МН и номер л/с ПУ в файле запроса равен 0
            // и получена только одна запись из МН для данного адреса, то запись считаем связанной
            if (updatePUAccount && 0 == Integer.valueOf(serviceProviderAccountNumber) && errorDetails.isEmpty()) {

                request.setAccountNumber(accountDetails.get(0).getAccountNumber());
                request.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);

            } else if (!errorDetails.isEmpty()) {
                log.error("acquirePersonAccount. Parsing of service provider account number was failed for following account details: {}. "
                        + "Request id: {}, request class: {}, calculation center: {}",
                        new Object[]{errorDetails, request.getId(), request.getClass(), calculationContext});

                List<String> errorServiceProviderAccountNumbers = newArrayList(transform(errorDetails, new Function<AccountDetail, String>() {

                    @Override
                    public String apply(AccountDetail errorDetail) {
                        return errorDetail.getServiceProviderAccountNumberInfo();
                    }
                }));
                List<String> accountNumbers = newArrayList(transform(errorDetails, new Function<AccountDetail, String>() {

                    @Override
                    public String apply(AccountDetail errorDetail) {
                        return errorDetail.getAccountNumber();
                    }
                }));
                Object errorServiceProviderAccountNumbersParam = errorServiceProviderAccountNumbers.size() == 1
                        ? errorServiceProviderAccountNumbers.get(0) : errorServiceProviderAccountNumbers;
                Object accountNumbersParam = accountNumbers.size() == 1 ? accountNumbers.get(0) : accountNumbers;
                errorServiceProviderAccountNumbersParam = errorServiceProviderAccountNumbersParam == null
                        ? "null" : errorServiceProviderAccountNumbersParam;
                accountNumbersParam = accountNumbersParam == null ? "null" : accountNumbersParam;

                RequestWarning warning = new RequestWarning(request.getId(), requestFileType,
                        RequestWarningStatus.PU_ACCOUNT_NUMBER_INVALID_FORMAT);
                warning.addParameter(new RequestWarningParameter(0, errorServiceProviderAccountNumbersParam));
                warning.addParameter(new RequestWarningParameter(1, accountNumbersParam));
                warningBean.save(warning);

                logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "service_provider_account_number_parsing_error",
                        localeBean.getSystemLocale(),
                        errorServiceProviderAccountNumbersParam, accountNumbersParam, calculationContext));
                request.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
            } else {
                request.setStatus(RequestStatus.ACCOUNT_NUMBER_MISMATCH);
            }
        } else {
            request.setStatus(RequestStatus.MORE_ONE_ACCOUNTS);
        }
    }

    private boolean isMegabankAccount(String realPuAccountNumber, String megabankAccount) {
        return (realPuAccountNumber.length() == 9) && realPuAccountNumber.equals(megabankAccount);
    }

    private boolean isCalcCenterAccount(String realPuAccountNumber, String calcCenterAccount) {
        return (realPuAccountNumber.length() == 10) && realPuAccountNumber.equals(calcCenterAccount);
    }

    /**
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETACCATTRS.
     * Используется для уточнения в UI номера л/c, когда больше одного человека в ЦН, имеющие разные номера л/c,
     * привязаны к одному адресу и для поиска номеров л/c в PaymentLookupPanel.
     * См. также PaymentLookupBean.getAccounts().
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт,
     * и драйвер в соответствии со стандартом JDBC рассматривает закрытый курсор как ошибку и выбрасывает исключение.
     *
     * @return
     */
    public List<AccountDetail> acquireAccountDetailsByAddress(CalculationContext calculationContext,
            AbstractRequest request, String district, String streetType, String street,
            String buildingNumber, String buildingCorp, String apartment, Date date) throws DBException {
        List<AccountDetail> accountCorrectionDetails = null;

        Map<String, Object> params = newHashMap();
        params.put("pDistrName", district);
        params.put("pStSortName", streetType);
        params.put("pStreetName", street);
        params.put("pHouseNum", buildingNumber);
        params.put("pHousePart", buildingCorp);
        params.put("pFlatNum", apartment);
        params.put("date", date);

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationContext.getDataSource()).selectOne(MAPPING_NAMESPACE + ".acquireAccountDetailsByAddress", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("acquireAccountDetailsByAddress. Calculation center: {}, parameters : {}", calculationContext, params);
            if (log.isDebugEnabled()) {
                log.debug("acquireAccountDetailsByAddress. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("acquireAccountDetailsByAddress. Result code is null. Request id: {}, request class: {}, calculation center: {}",
                    new Object[]{request.getId(), request.getClass(), calculationContext});
            logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETACCATTRS", "null", calculationContext));
            request.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    accountCorrectionDetails = (List<AccountDetail>) params.get("details");
                    if (accountCorrectionDetails == null || accountCorrectionDetails.isEmpty()) {
                        log.error("acquireAccountDetailsByAddress. Result code is 1 but account details data is null or empty. Request id: {}, "
                                + "request class: {}, calculation center: {}",
                                new Object[]{request.getId(), request.getClass(), calculationContext});
                        logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETACCATTRS", calculationContext));
                        request.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
                    }
                    break;
                case 0:
                    request.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                case -2:
                    request.setStatus(RequestStatus.APARTMENT_NOT_FOUND);
                    break;
                case -3:
                    request.setStatus(RequestStatus.BUILDING_CORP_NOT_FOUND);
                    break;
                case -4:
                    request.setStatus(RequestStatus.BUILDING_NOT_FOUND);
                    break;
                case -5:
                    request.setStatus(RequestStatus.STREET_NOT_FOUND);
                    break;
                case -6:
                    request.setStatus(RequestStatus.STREET_TYPE_NOT_FOUND);
                    break;
                case -7:
                    request.setStatus(RequestStatus.DISTRICT_NOT_FOUND);
                    break;
                default:
                    log.error("acquireAccountDetailsByAddress. Unexpected result code: {}. Request id: {}, request class: {}, "
                            + "calculation center: {}",
                            new Object[]{resultCode, request.getId(), request.getClass(), calculationContext});
                    logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                            "GETACCATTRS", resultCode, calculationContext));
                    request.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
            }
        }
        return accountCorrectionDetails;
    }

    /**
     * Обработать payment и заполнить некоторые поля в соответствующих данному payment benefit записях.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETCHARGEANDPARAMS.
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт, и драйвер с соотвествии со стандартом JDBC рассматривает
     * закрытый курсор как ошибку и выбрасывает исключение.
     *
     * @param benefits
     */
    public void processPaymentAndBenefit(CalculationContext calculationContext, Payment payment,
            List<Benefit> benefits) throws DBException {

        /* Set OPP field */
        char[] opp = new char[8];
        for (int i = 0; i < 8; opp[i++] = '0');
        for (long spt : calculationContext.getServiceProviderTypeIds()) {
            if (spt >= 1 && spt <= 8) {
                int i = 8 - (int) spt;
                opp[i] = '1';
            }
        }
        payment.setField(PaymentDBF.OPP, String.valueOf(opp));

        Map<String, Object> params = newHashMap();
        params.put("accountNumber", payment.getAccountNumber());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationContext.getDataSource()).selectOne(MAPPING_NAMESPACE + ".processPaymentAndBenefit", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("processPaymentAndBenefit. Calculation center: {}, parameters : {}", calculationContext, params);
            if (log.isDebugEnabled()) {
                log.debug("processPaymentAndBenefit. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("processPaymentAndBenefit. Result code is null. Payment id: {}, calculation center: {}",
                    payment.getId(), calculationContext);
            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETCHARGEANDPARAMS", "null", calculationContext));
            payment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    List<PaymentAndBenefitData> paymentAndBenefitDatas = (List<PaymentAndBenefitData>) params.get("data");
                    if (paymentAndBenefitDatas != null && !paymentAndBenefitDatas.isEmpty()) {
                        PaymentAndBenefitData data = paymentAndBenefitDatas.get(0);
                        if (paymentAndBenefitDatas.size() > 1) {
                            log.warn("processPaymentAndBenefit. Size of list of paymentAndBenefitData is more than 1. Only first entry will be used."
                                    + "Calculation center: {}", calculationContext);
                            logBean.warn(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "data_size_more_one", localeBean.getSystemLocale(),
                                    "GETCHARGEANDPARAMS", calculationContext));
                        }
                        processPaymentAndBenefitData(calculationContext, payment, benefits, data);
                    } else {
                        log.error("processPaymentAndBenefit. Result code is 1 but paymentAndBenefitData is null or empty. Payment id: {},"
                                + "calculation center: {}",
                                payment.getId(), calculationContext);
                        logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETCHARGEANDPARAMS", calculationContext));
                        payment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case -1:
                    payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("processPaymentAndBenefit. Unexpected result code: {}. Payment id: {}, calculation center: {}",
                            new Object[]{resultCode, payment.getId(), calculationContext});
                    logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                            "GETCHARGEANDPARAMS", resultCode, calculationContext));
                    payment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        }
    }

    /**
     * Заполнить payment и benefit записи данными из processPaymentAndBenefit().
     * Для payment:
     * Все поля кроме CODE2_1 проставляются напрямую из data в payment.
     * Поле CODE2_1 заполняются не напрямую, а по таблице тарифов(метод TarifBean.getCODE2_1()).
     * Если в тарифах не нашли, то проставляем статус RequestStatus.TARIF_CODE2_1_NOT_FOUND и значение из ЦН(T11_CS_UNI) в
     * сalculationCenterCode2_1 для дальнейшего отображения в UI деталей. Иначе тариф сохраняется в payment и он считается обработанным.
     *
     * Для benefit:
     * Все поля проставляются напрямую, кроме:
     * поле OWN_FRM проставляется из таблицы коррекций для форм власти(ownership). На данный момент для всех форм власти в ЦН существуют коррекции,
     * поэтому ситуации с не найденной коррекцией нет.
     *
     * @param calculationCenterId id ЦН
     * @param benefits
     */
    protected void processPaymentAndBenefitData(CalculationContext calculationContext, Payment payment,
            List<Benefit> benefits, PaymentAndBenefitData data) {
        //payment
        //fields common for all service provider types
        payment.setField(PaymentDBF.FROG, data.getPercent());
        payment.setField(PaymentDBF.FL_PAY, data.getApartmentFeeCharge());
        payment.setField(PaymentDBF.NM_PAY, data.getNormCharge());
        payment.setField(PaymentDBF.DEBT, data.getSaldo());
        payment.setField(PaymentDBF.NUMB, data.getLodgerCount());
        payment.setField(PaymentDBF.MARK, data.getUserCount());
        payment.setField(PaymentDBF.NORM_F_1, data.getReducedArea());

        /*
         * Если модуль начислений предоставляет более одной услуги, то возможна ситуация, когда 
         * по одной услуге тариф обработан успешно, т.е. метод handleTarif(...) вернул true, а по другой 
         * услуге тариф обработан с ошибкой. В этом случае обработка тарифов для оставшихся услуг не происходит,
         * ошибка логируется и программа переходит к обработке benefits записей, соответствующих заданному payment.
         */
        //статус успешности обработки тарифов.
        boolean tarifHandled = true;
        //тариф МН, при обработке которого возникла ошибка.
        BigDecimal errorTarif = null;

        //apartment fee
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.APARTMENT_FEE)) {
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_1, data.getApartmentFeeTarif())) {
                tarifHandled = false;
                errorTarif = data.getApartmentFeeTarif();
            }
        }
        //heating
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.HEATING)) {
            payment.setField(PaymentDBF.NORM_F_2, data.getHeatingArea());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_2, data.getHeatingTarif())) {
                tarifHandled = false;
                errorTarif = data.getHeatingTarif();
            }
        }
        //hot water
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.HOT_WATER_SUPPLY)) {
            payment.setField(PaymentDBF.NORM_F_3, data.getChargeHotWater());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_3, data.getHotWaterTarif())) {
                tarifHandled = false;
                errorTarif = data.getHotWaterTarif();
            }
        }
        //cold water
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.COLD_WATER_SUPPLY)) {
            payment.setField(PaymentDBF.NORM_F_4, data.getChargeColdWater());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_4, data.getColdWaterTarif())) {
                tarifHandled = false;
                errorTarif = data.getColdWaterTarif();
            }
        }
        //gas
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.GAS_SUPPLY)) {
            payment.setField(PaymentDBF.NORM_F_5, data.getChargeGas());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_5, data.getGasTarif())) {
                tarifHandled = false;
                errorTarif = data.getGasTarif();
            }
        }
        //power
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.POWER_SUPPLY)) {
            payment.setField(PaymentDBF.NORM_F_6, data.getChargePower());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_6, data.getPowerTarif())) {
                tarifHandled = false;
                errorTarif = data.getPowerTarif();
            }
        }
        //garbage disposal
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.GARBAGE_DISPOSAL)) {
            payment.setField(PaymentDBF.NORM_F_7, data.getChargeGarbageDisposal());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_7, data.getGarbageDisposalTarif())) {
                tarifHandled = false;
                errorTarif = data.getGarbageDisposalTarif();
            }
        }
        //drainage
        if (tarifHandled
                && calculationContext.getServiceProviderTypeIds().contains(ServiceProviderTypeStrategy.DRAINAGE)) {
            payment.setField(PaymentDBF.NORM_F_8, data.getChargeDrainage());
            if (!handleSubsidyTarif(calculationContext, payment, PaymentDBF.CODE2_8, data.getDrainageTarif())) {
                tarifHandled = false;
                errorTarif = data.getDrainageTarif();
            }
        }

        /*
         * Логирование ошибки обработки тарифа.
         */
        if (!tarifHandled) {
            payment.setStatus(RequestStatus.SUBSIDY_TARIF_CODE_NOT_FOUND);

            log.error("Couldn't find subsidy tarif code by calculation center's tarif: '{}', "
                    + "calculation center id: {} and user organization id: {}",
                    new Object[]{
                        errorTarif,
                        calculationContext.getCalculationCenterId(),
                        calculationContext.getUserOrganizationId()
                    });

            RequestWarning warning = new RequestWarning(payment.getId(), RequestFile.TYPE.PAYMENT, RequestWarningStatus.SUBSIDY_TARIF_NOT_FOUND);
            warning.addParameter(new RequestWarningParameter(0, errorTarif));
            warning.addParameter(new RequestWarningParameter(1, "organization", calculationContext.getCalculationCenterId()));
            warningBean.save(warning);

            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.EDIT,
                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
        }

        //benefits
        if (benefits != null && !benefits.isEmpty()) {
            String calcCenterOwnershipCode = data.getOwnership();
            Long internalOwnershipId = findInternalOwnership(calcCenterOwnershipCode, calculationContext.getCalculationCenterId());
            if (internalOwnershipId == null) {
                log.error("Couldn't find in corrections internal ownership object by calculation center's ownership code: '{}' "
                        + "and calculation center id: {}", calcCenterOwnershipCode, calculationContext.getCalculationCenterId());

                for (Benefit benefit : benefits) {
                    RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                            RequestWarningStatus.OWNERSHIP_OBJECT_NOT_FOUND);
                    warning.addParameter(new RequestWarningParameter(0, calcCenterOwnershipCode));
                    warning.addParameter(new RequestWarningParameter(1, "organization", calculationContext.getCalculationCenterId()));
                    warningBean.save(warning);

                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                }
            } else {
                final long osznId = payment.getOrganizationId();
                String osznOwnershipCode = findOSZNOwnershipCode(internalOwnershipId, osznId, calculationContext.getUserOrganizationId());
                if (osznOwnershipCode == null) {
                    log.error("Couldn't find in corrections oszn's ownership code by internal ownership object id: {}"
                            + ", oszn id: {} and user organization id: {}",
                            new Object[]{internalOwnershipId, osznId, calculationContext.getUserOrganizationId()});

                    for (Benefit benefit : benefits) {
                        RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                RequestWarningStatus.OWNERSHIP_CODE_NOT_FOUND);
                        warning.addParameter(new RequestWarningParameter(0, "ownership", internalOwnershipId));
                        warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                        warningBean.save(warning);

                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                    }
                } else {
                    Integer ownershipCodeAsInt = null;
                    try {
                        ownershipCodeAsInt = Integer.valueOf(osznOwnershipCode);
                    } catch (NumberFormatException e) {
                        log.error("Couldn't transform OWN_FRM value '{}' from correction to integer value. Oszn id: {}, internal ownership id: {}",
                                new Object[]{osznOwnershipCode, osznId, internalOwnershipId});

                        for (Benefit benefit : benefits) {
                            RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                    RequestWarningStatus.OWNERSHIP_CODE_INVALID);
                            warning.addParameter(new RequestWarningParameter(0, osznOwnershipCode));
                            warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                            warning.addParameter(new RequestWarningParameter(2, "ownership", internalOwnershipId));
                            warningBean.save(warning);

                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                        }
                    }

                    if (ownershipCodeAsInt != null) {
                        for (Benefit benefit : benefits) {
                            benefit.setField(BenefitDBF.OWN_FRM, ownershipCodeAsInt);
                        }
                    }
                }
            }
            for (Benefit benefit : benefits) {
                benefit.setField(BenefitDBF.CM_AREA, payment.getStringField(PaymentDBF.NORM_F_1));
                benefit.setField(BenefitDBF.HOSTEL, data.getRoomCount());
            }
        }
    }

    protected boolean handleSubsidyTarif(CalculationContext calculationContext, Payment payment, PaymentDBF field, BigDecimal rawTarif) {
        String tarifCode = getSubsidyTarifCode(rawTarif, payment.getOrganizationId(), calculationContext.getUserOrganizationId());
        if (tarifCode == null) {
            return false;
        } else {
            payment.setField(field, tarifCode);
            payment.setStatus(RequestStatus.PROCESSED);
            return true;
        }
    }

    protected Long findInternalOwnership(String calculationCenterOwnership, long calculationCenterId) {
        return ownershipCorrectionBean.findInternalOwnership(calculationCenterOwnership, calculationCenterId);
    }

    protected String findOSZNOwnershipCode(Long internalOwnership, long osznId, long userOrganizationId) {
        return ownershipCorrectionBean.findOwnershipCode(internalOwnership, osznId, userOrganizationId);
    }

    /**
     * Получить тариф.
     * @param T11_CS_UNI
     * @return
     */
    protected String getSubsidyTarifCode(BigDecimal T11_CS_UNI, long osznId, long userOrganizationId) {
        return subsidyTarifBean.getCode2(T11_CS_UNI, osznId, userOrganizationId);
    }

    public Collection<BenefitData> getBenefitData(CalculationContext calculationContext, Benefit benefit, Date dat1)
            throws DBException {
        Map<String, Object> params = newHashMap();
        params.put("accountNumber", benefit.getAccountNumber());
        params.put("dat1", dat1);

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationContext.getDataSource()).selectOne(MAPPING_NAMESPACE + ".getBenefitData", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("getBenefitData. Calculation center: {}, parameters : {}", calculationContext, params);
            if (log.isDebugEnabled()) {
                log.debug("getBenefitData. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("getBenefitData. Result code is null. Benefit id: {}, dat1: {}, calculation center: {}",
                    new Object[]{benefit.getId(), dat1, calculationContext});
            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETPRIVS", "null", calculationContext));
            benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
                    if (benefitData != null && !benefitData.isEmpty()) {
                        if (checkOrderFam(calculationContext, "getBenefitData", benefitData, newArrayList(benefit), dat1)
                                && checkBenefitCode(calculationContext, "getBenefitData", benefitData, newArrayList(benefit), dat1)) {
                            Collection<BenefitData> emptyList = getEmptyBenefitData(benefitData);
                            if (emptyList != null && !emptyList.isEmpty()) {
                                logEmptyBenefitData(calculationContext, "getBenefitData", newArrayList(benefit), dat1);
                            }

                            Collection<BenefitData> finalBenefitData = getBenefitDataWithMinPriv("getBenefitData", benefitData);
                            finalBenefitData.addAll(emptyList);
                            return finalBenefitData;
                        }
                    } else {
                        log.error("getBenefitData. Result code is 1 but benefit data is null or empty. Benefit id: {}, dat1: {}, "
                                + "calculation center: {}",
                                new Object[]{benefit.getId(), dat1, calculationContext});
                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETPRIVS", calculationContext));
                        benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case -1:
                    benefit.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("getBenefitData. Unexpected result code: {}. Benefit id: {}, dat1: {}, calculation center: {}",
                            new Object[]{resultCode, benefit.getId(), dat1, calculationContext});
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                            "GETPRIVS", resultCode, calculationContext));
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        }
        return null;
    }

    protected static class BenefitDataId implements Serializable {

        private String inn;
        private String name;
        private String passport;

        protected BenefitDataId(String inn, String name, String passport) {
            this.inn = inn;
            this.name = name;
            this.passport = passport;
        }

        public String getInn() {
            return inn;
        }

        public void setInn(String inn) {
            this.inn = inn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassport() {
            return passport;
        }

        public void setPassport(String passport) {
            this.passport = passport;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BenefitDataId other = (BenefitDataId) obj;
            return Strings.isEqual(this.inn, other.inn) && Strings.isEqual(this.name, other.name) && Strings.isEqual(this.passport, other.passport);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (!Strings.isEmpty(this.inn) ? this.inn.hashCode() : 0);
            hash = 43 * hash + (!Strings.isEmpty(this.name) ? this.name.hashCode() : 0);
            hash = 43 * hash + (!Strings.isEmpty(this.passport) ? this.passport.hashCode() : 0);
            return hash;
        }
    }

    protected boolean checkOrderFam(CalculationContext calculationContext, String method, List<BenefitData> benefitData,
            List<Benefit> benefits, Date dat1) {
        String accountNumber = benefits.get(0).getAccountNumber();
        for (BenefitData data : benefitData) {
            if (Strings.isEmpty(data.getOrderFamily())) {
                log.error(method + ". Order fam is null. Account number: {}, dat1: {}, calculation center: {}",
                        new Object[]{accountNumber, dat1, calculationContext});
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_order_fam_null", localeBean.getSystemLocale(),
                            "GETPRIVS", accountNumber, dat1, calculationContext));
                }
                return false;
            }
        }

        Map<String, BenefitData> orderFams = newHashMap();
        for (BenefitData data : benefitData) {
            String orderFam = data.getOrderFamily();
            BenefitData dublicate = orderFams.get(orderFam);
            if (dublicate != null) {
                log.error(method + ". Order fam is not unique. At least two benefit data have the same order fam. First: {}, second {}. "
                        + "Account number: {}, dat1: {}, calculation center: {}",
                        new Object[]{data, dublicate, accountNumber, dat1, calculationContext});
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_order_fam_not_unique", localeBean.getSystemLocale(),
                            "GETPRIVS", accountNumber, dat1, calculationContext));
                }
                return false;
            } else {
                orderFams.put(orderFam, data);
            }
        }
        return true;
    }

    protected boolean checkBenefitCode(CalculationContext calculationContext, String method, List<BenefitData> benefitData,
            List<Benefit> benefits, Date dat1) {
        String accountNumber = benefits.get(0).getAccountNumber();
        for (BenefitData data : benefitData) {
            if (Strings.isEmpty(data.getCode())) {
                log.error(method + ". BenefitData's code is null. Account number: {}, dat1: {}, calculation center: {}",
                        new Object[]{accountNumber, dat1, calculationContext});
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_code_null", localeBean.getSystemLocale(),
                            "GETPRIVS", accountNumber, dat1, calculationContext));
                }
                return false;
            }
        }
        return true;
    }

    protected void logEmptyBenefitData(CalculationContext calculationCenterInfo, String method, List<Benefit> benefits, Date dat1) {
        String accountNumber = benefits.get(0).getAccountNumber();
        log.error(method + ". Inn, name and passport of benefit data are null. "
                + "Account number: {}, dat1: {}, calculation center: {}",
                new Object[]{accountNumber, dat1, calculationCenterInfo});
        for (Benefit benefit : benefits) {
            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_id_empty", localeBean.getSystemLocale(),
                    "GETPRIVS", accountNumber, dat1, calculationCenterInfo));
        }
    }

    protected Map<BenefitDataId, Collection<BenefitData>> groupBenefitData(String method, Collection<BenefitData> benefitData) {
        Map<BenefitDataId, Collection<BenefitData>> groupMap = newHashMap();
        for (BenefitData data : benefitData) {
            BenefitDataId id = new BenefitDataId(data.getInn(), data.getFirstName() + data.getMiddleName() + data.getLastName(),
                    data.getPassportSerial() + data.getPassportNumber());
            Collection<BenefitData> list = groupMap.get(id);
            if (list == null) {
                list = newArrayList();
                groupMap.put(id, list);
            }
            list.add(data);
        }
        return groupMap;
    }

    protected Collection<BenefitData> getBenefitDataWithMinPriv(String method, Collection<BenefitData> benefitData) {
        Collection<BenefitData> nonEmptyList = getNonEmptyBenefitData(benefitData);
        Map<BenefitDataId, Collection<BenefitData>> groupMap = groupBenefitData(method, nonEmptyList);
        Collection<BenefitData> benefitDataWithMinPriv = newArrayList();
        for (Map.Entry<BenefitDataId, Collection<BenefitData>> group : groupMap.entrySet()) {
            BenefitData min = Collections.min(group.getValue(), BENEFIT_DATA_COMPARATOR);
            benefitDataWithMinPriv.add(min);
        }
        return benefitDataWithMinPriv;
    }

    protected Collection<BenefitData> getEmptyBenefitData(Collection<BenefitData> benefitData) {
        return newArrayList(filter(benefitData, new Predicate<BenefitData>() {

            @Override
            public boolean apply(BenefitData data) {
                return data.isEmpty();
            }
        }));
    }

    protected Collection<BenefitData> getNonEmptyBenefitData(Collection<BenefitData> benefitData) {
        Collection<BenefitData> nonEmptyList = newArrayList(benefitData);
        nonEmptyList.removeAll(getEmptyBenefitData(benefitData));
        return nonEmptyList;
    }

    protected List<BenefitData> getBenefitDataByINN(List<BenefitData> benefitDatas, final String inn) {
        return newArrayList(filter(benefitDatas, new Predicate<BenefitData>() {

            @Override
            public boolean apply(BenefitData benefitData) {
                return benefitData.getInn().equals(inn);
            }
        }));
    }

    private static class BenefitDataComparator implements Comparator<BenefitData> {

        @Override
        public int compare(BenefitData o1, BenefitData o2) {
            String benefitCode1 = o1.getCode();
            Integer i1 = null;
            try {
                i1 = Integer.parseInt(benefitCode1);
            } catch (NumberFormatException e) {
            }

            String benefitCode2 = o2.getCode();
            Integer i2 = null;
            try {
                i2 = Integer.parseInt(benefitCode2);
            } catch (NumberFormatException e) {
            }

            if (i1 != null && i2 != null) {
                return i1.compareTo(i2);
            } else {
                return benefitCode1.compareTo(benefitCode2);
            }
        }
    }
    private static final BenefitDataComparator BENEFIT_DATA_COMPARATOR = new BenefitDataComparator();

    /**
     * Обработать группу benefit записей с одинаковым account number.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETPRIVS.
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт, и драйвер с соответствии со стандартом JDBC рассматривает
     * закрытый курсор как ошибку и выбрасывает исключение.
     *
     * @param dat1 дата из поля DAT1 payment записи, соответствующей группе benefits записей со значением в поле FROG большим 0
     * @param benefits группа benefit записей
     * @param calculationCenterId id ЦН
     */
    public void processBenefit(CalculationContext calculationContext, Date dat1,
            List<Benefit> benefits) throws DBException {
        String accountNumber = benefits.get(0).getAccountNumber();

        Map<String, Object> params = newHashMap();
        params.put("accountNumber", accountNumber);
        params.put("dat1", dat1);

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationContext.getDataSource()).selectOne(MAPPING_NAMESPACE + ".processBenefit", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("processBenefit. Calculation center: {}, parameters : {}", calculationContext, params);
            if (log.isDebugEnabled()) {
                log.debug("processBenefit. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("processBenefit. Result code is null. Account number: {}, dat1: {}, calculation center: {}",
                    new Object[]{accountNumber, dat1, calculationContext});
            for (Benefit benefit : benefits) {
                logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                        "GETPRIVS", "null", calculationContext));
                benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        } else {
            switch (resultCode) {
                case 1:
                    List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
                    if (benefitData != null && !benefitData.isEmpty()) {
                        if (checkOrderFam(calculationContext, "processBenefit", benefitData, benefits, dat1)
                                && checkBenefitCode(calculationContext, "processBenefit", benefitData, benefits, dat1)) {
                            processBenefitData(calculationContext, benefits, benefitData, dat1);
                        }
                    } else {
                        log.error("processBenefit. Result code is 1 but benefit data is null or empty. Account number: {}, dat1: {},"
                                + " calculation center: {}",
                                new Object[]{accountNumber, dat1, calculationContext});
                        for (Benefit benefit : benefits) {
                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                    "GETPRIVS", calculationContext));
                            benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                        }
                    }
                    break;
                case -1:
                    setStatus(benefits, RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("processBenefit. Unexpected result code: {}. Account number: {}, dat1: {}, calculation center: {}",
                            new Object[]{resultCode, accountNumber, dat1, calculationContext});
                    for (Benefit benefit : benefits) {
                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                                "GETPRIVS", resultCode, calculationContext));
                        benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
            }
        }
    }

    /**
     * Из требований заказчика: "Среди жильцов на обрабатываемом л/с по идентификационному коду (IND_COD) пытаемся
     * найти носителя льготы, если не нашли, то ищем по номеру паспорта (без серии). Если нашли, проставляем категорию
     * льготы, если не нашли - отмечаем все записи, относящиеся к данному л/с как ошибку связывания. Надо иметь ввиду,
     * что на одной персоне может быть более одной льготы. В этом случае надо брать льготу с меньшим номером категории."
     *
     * Алгоритм:
     * Для каждой записи из data выделяем текущий ИНН(обязательно не NULL) и текущий номер паспорта.
     * Если этот ИНН еще не обрабатывался, то ищем среди benefits с таким же ИНН.
     * Если не нашли, то ищем с текущим номером паспорта.
     * В итоге получим некоторое подмножество benefits - theSameBenefits.
     * Если theSameBenefits не пусто, то выделим из data записи с текущим ИНН - список theSameMan.
     * Если же theSameBenefits пусто, то помечаем все записи benefits статусом RequestStatus.WRONG_ACCOUNT_NUMBER
     * и выходим.
     * В theSameMan найдем запись с наименьшим кодом привилегии  - min. Порядок сравнения: пытаемся преобразовать
     * строковые значения кодов привилегий в числа и сравнить как числа, иначе сравниваем как строки.
     * По полученному наименьшему коду привилегии(cmBenefitCode) ищем методом getOSZNPrivilegeCode код привилегии
     * для ОСЗН(osznBenefitCode) в таблице коррекций привилегий.
     * Если нашли код привилегии(osznBenefitCode != null), то проставляем во все записи в benefits: в поле PRIV_CAT
     * - osznBenefitCode, в поле ORD_FAM - порядок льготы из min(min.get("ORD_FAM"))
     * Если не нашли код привилегии, то все записи в theSameBenefits помечаются статусом RequestStatus.BENEFIT_NOT_FOUND.
     * Наконец все записи benefits, для которых код не был проставлен в RequestStatus.BENEFIT_NOT_FOUND помечаются
     * статусом RequestStatus.PROCESSED.
     *
     * @param calculationCenterId id ЦН
     * @param benefits Список benefit записей с одинаковым номером л/c
     * @param benefitData Список записей данных из ЦН
     */
    protected void processBenefitData(CalculationContext calculationContext, List<Benefit> benefits,
            List<BenefitData> benefitData, Date dat1) {

        final long calculationCenterId = calculationContext.getCalculationCenterId();
        final long userOrganizationId = calculationContext.getUserOrganizationId();
        final long osznId = benefits.get(0).getOrganizationId();

        Collection<BenefitData> emptyList = getEmptyBenefitData(benefitData);
        if (emptyList != null && !emptyList.isEmpty()) {
            logEmptyBenefitData(calculationContext, "processBenefit", benefits, dat1);
            setStatus(benefits, RequestStatus.BENEFIT_OWNER_NOT_ASSOCIATED);
            return;
        }

        Collection<BenefitData> benefitDataWithMinPriv = getBenefitDataWithMinPriv("processBenefit", benefitData);

        for (BenefitData data : benefitDataWithMinPriv) {
            String inn = data.getInn();
            String passport = data.getPassportNumber();
            Collection<Benefit> foundBenefits = null;

            if (!Strings.isEmpty(inn)) {
                foundBenefits = findByINN(benefits, inn);
            }
            if ((foundBenefits == null || foundBenefits.isEmpty()) && !Strings.isEmpty(passport)) {
                foundBenefits = findByPassportNumber(benefits, passport);
            }

            if (foundBenefits == null || foundBenefits.isEmpty()) {
                setStatus(benefits, RequestStatus.BENEFIT_OWNER_NOT_ASSOCIATED);
                return;
            }

            //set benefit code
            String calcCenterBenefitCode = data.getCode();
            Long internalPrivilegeId = findInternalPrivilege(calcCenterBenefitCode, calculationCenterId);
            if (internalPrivilegeId == null) {
                log.error("Couldn't find in corrections internal privilege object by calculation center's privilege code: '{}' "
                        + "and calculation center id: {}", calcCenterBenefitCode, calculationCenterId);

                for (Benefit benefit : foundBenefits) {
                    benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);

                    RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                            RequestWarningStatus.PRIVILEGE_OBJECT_NOT_FOUND);
                    warning.addParameter(new RequestWarningParameter(0, calcCenterBenefitCode));
                    warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                    warningBean.save(warning);

                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                }
            } else {
                String osznBenefitCode = findOSZNPrivilegeCode(internalPrivilegeId, osznId, userOrganizationId);
                if (osznBenefitCode == null) {
                    log.error("Couldn't find in corrections oszn's privilege code by internal privilege object id: {} "
                            + "and oszn id: {}", internalPrivilegeId, osznId);

                    for (Benefit benefit : foundBenefits) {
                        benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);

                        RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                RequestWarningStatus.PRIVILEGE_CODE_NOT_FOUND);
                        warning.addParameter(new RequestWarningParameter(0, "privilege", internalPrivilegeId));
                        warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                        warningBean.save(warning);

                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                    }
                } else {
                    //set benefit code and ord fam into benefit.
                    Integer benefitCodeAsInt = null;
                    try {
                        benefitCodeAsInt = Integer.valueOf(osznBenefitCode);
                    } catch (NumberFormatException e) {
                        log.error("Couldn't transform privilege code '{}' from correction to integer value. Oszn id: {}, "
                                + "internal privilege id: {}", new Object[]{osznBenefitCode, osznId, internalPrivilegeId});

                        for (Benefit benefit : foundBenefits) {
                            RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                    RequestWarningStatus.PRIVILEGE_CODE_INVALID);
                            warning.addParameter(new RequestWarningParameter(0, osznBenefitCode));
                            warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                            warning.addParameter(new RequestWarningParameter(2, "privilege", internalPrivilegeId));
                            warningBean.save(warning);

                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                        }
                    }

                    Integer ordFamAsInt = null;
                    try {
                        ordFamAsInt = Integer.valueOf(data.getOrderFamily());
                    } catch (NumberFormatException e) {
                        log.error("Couldn't transform ord fam value '{}' from calculation center to integer value.", data.getOrderFamily());

                        for (Benefit benefit : foundBenefits) {
                            RequestWarning warning = new RequestWarning(RequestFile.TYPE.BENEFIT, RequestWarningStatus.ORD_FAM_INVALID);
                            warning.addParameter(new RequestWarningParameter(0, data.getOrderFamily()));
                            warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                            warningBean.save(warning);

                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                        }
                    }

                    if (benefitCodeAsInt == null) {
                        setStatus(foundBenefits, RequestStatus.PROCESSING_INVALID_FORMAT);
                    } else {
                        for (Benefit benefit : foundBenefits) {
                            benefit.setField(BenefitDBF.PRIV_CAT, benefitCodeAsInt);
                        }
                    }

                    if (ordFamAsInt == null) {
                        setStatus(foundBenefits, RequestStatus.PROCESSING_INVALID_FORMAT);
                    } else {
                        for (Benefit benefit : foundBenefits) {
                            benefit.setField(BenefitDBF.ORD_FAM, ordFamAsInt);
                        }
                    }
                }
            }
        }

        for (Benefit benefit : benefits) {
            if (benefit.getStatus() != RequestStatus.BENEFIT_NOT_FOUND && benefit.getStatus() != RequestStatus.PROCESSING_INVALID_FORMAT) {
                benefit.setStatus(RequestStatus.PROCESSED);
            }
        }
    }

    protected void setStatus(Collection<Benefit> benefits, RequestStatus status) {
        for (Benefit benefit : benefits) {
            benefit.setStatus(status);
        }
    }

    protected Long findInternalPrivilege(String calculationCenterPrivilege, long calculationCenterId) {
        return privilegeCorrectionBean.findInternalPrivilege(calculationCenterPrivilege, calculationCenterId);
    }

    protected String findOSZNPrivilegeCode(Long internalPrivilege, long osznId, long userOrganizationId) {
        return privilegeCorrectionBean.findPrivilegeCode(internalPrivilege, osznId, userOrganizationId);
    }

    protected List<Benefit> findByPassportNumber(List<Benefit> benefits, final String passportNumber) {
        return newArrayList(filter(benefits, new Predicate<Benefit>() {

            @Override
            public boolean apply(Benefit benefit) {
                return passportNumber.equals(benefit.getStringField(BenefitDBF.PSP_NUM));
            }
        }));
    }

    protected List<Benefit> findByINN(List<Benefit> benefits, final String inn) {
        return newArrayList(filter(benefits, new Predicate<Benefit>() {

            @Override
            public boolean apply(Benefit benefit) {
                return inn.equals(benefit.getStringField(BenefitDBF.IND_COD));
            }
        }));
    }
    private static final int OSZN_ACCOUNT_TYPE = 0;
    private static final int MEGABANK_ACCOUNT_TYPE = 1;
    private static final int CALCULATION_CENTER_ACCOUNT_TYPE = 2;

    public List<AccountDetail> acquireAccountDetailsByAccount(CalculationContext calculationCenterInfo, AbstractRequest request,
            String district, String account) throws DBException, UnknownAccountNumberTypeException {

        int accountType = determineAccountType(account);
        List<AccountDetail> accountCorrectionDetails = null;

        Map<String, Object> params = newHashMap();
        params.put("pDistrName", district);
        params.put("pAccCode", account);
        params.put("pAccCodeType", accountType);

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationCenterInfo.getDataSource()).selectOne(MAPPING_NAMESPACE + ".getAttrsByAccCode", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("acquireAccountDetailsByAccount. Calculation center: {}, parameters : {}", calculationCenterInfo, params);
            if (log.isDebugEnabled()) {
                log.debug("acquireAccountDetailsByAccount. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("acquireAccountDetailsByAccount. Result code is null. Request id: {}, request class: {}, calculation center: {}",
                    new Object[]{request.getId(), request.getClass(), calculationCenterInfo});
            logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETATTRSBYACCCODE", "null", calculationCenterInfo));
            request.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    accountCorrectionDetails = (List<AccountDetail>) params.get("details");
                    if (accountCorrectionDetails == null || accountCorrectionDetails.isEmpty()) {
                        log.error("acquireAccountDetailsByAccount. Result code is 1 but account details data is null or empty. "
                                + "Request id: {}, request class: {}, calculation center: {}",
                                new Object[]{request.getId(), request.getClass(), calculationCenterInfo});
                        logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETATTRSBYACCCODE", calculationCenterInfo));
                        request.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case 0:
                    request.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                case -1:
                    log.error("acquireAccountDetailsByAccount. Result code is -1 but account type code is {}. Request id: {}, request class: {}"
                            + ", calculation center: {}",
                            new Object[]{accountType, request.getId(), request.getClass(), calculationCenterInfo});
                    logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "wrong_account_type_code", localeBean.getSystemLocale(),
                            "GETATTRSBYACCCODE", accountType, calculationCenterInfo));
                    request.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    break;
                case -2:
                    request.setStatus(RequestStatus.DISTRICT_NOT_FOUND);
                    break;
                default:
                    log.error("acquireAccountDetailsByAccount. Unexpected result code: {}. Request id: {}, request class: {}"
                            + ", calculation center: {}",
                            new Object[]{resultCode, request.getId(), request.getClass(), calculationCenterInfo});
                    logBean.error(Module.NAME, getClass(), request.getClass(), request.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                            "GETATTRSBYACCCODE", resultCode, calculationCenterInfo));
                    request.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        }
        return accountCorrectionDetails;
    }

    protected int determineAccountType(String accountNumber) throws UnknownAccountNumberTypeException {
        if (Strings.isEmpty(accountNumber)) {
            throw new UnknownAccountNumberTypeException();
        }

        if (accountNumber.length() == 10 && accountNumber.startsWith("100")) {
            return CALCULATION_CENTER_ACCOUNT_TYPE;
        }
        if (accountNumber.length() == 9 && accountNumber.startsWith("1")) {
            return MEGABANK_ACCOUNT_TYPE;
        }
        if (accountNumber.length() < 9) {
            return OSZN_ACCOUNT_TYPE;
        }

        throw new UnknownAccountNumberTypeException();
    }

    public void processActualPayment(CalculationContext calculationCenterInfo, ActualPayment actualPayment, Date date)
            throws DBException {
        Map<String, Object> params = newHashMap();
        params.put("accountNumber", actualPayment.getAccountNumber());
        params.put("date", date);

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        try {
            sqlSession(calculationCenterInfo.getDataSource()).selectOne(MAPPING_NAMESPACE + ".processActualPayment", params);
        } catch (Exception e) {
            if (!OracleErrors.isCursorClosedError(e)) {
                throw new DBException(e);
            }
        } finally {
            log.info("processActualPayment. Calculation center: {}, parameters : {}", calculationCenterInfo, params);
            if (log.isDebugEnabled()) {
                log.debug("processActualPayment. Time of operation: {} sec.", (System.nanoTime() - startTime) / 1000000000F);
            }
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("processActualPayment. Result code is null. ActualPayment id: {}, calculation center: {}",
                    actualPayment.getId(), calculationCenterInfo);
            logBean.error(Module.NAME, getClass(), ActualPayment.class, actualPayment.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETFACTCHARGEANDTARIF", "null", calculationCenterInfo));
            actualPayment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    List<ActualPaymentData> actualPaymentDatas = (List<ActualPaymentData>) params.get("data");
                    if (actualPaymentDatas != null && !actualPaymentDatas.isEmpty()) {
                        ActualPaymentData data = actualPaymentDatas.get(0);
                        if (actualPaymentDatas.size() > 1) {
                            log.warn("processActualPayment. Size of list of actualPaymentData is more than 1. Only first entry will be used."
                                    + "Calculation center: {}", calculationCenterInfo);
                            logBean.warn(Module.NAME, getClass(), ActualPayment.class, actualPayment.getId(), EVENT.GETTING_DATA,
                                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "data_size_more_one", localeBean.getSystemLocale(),
                                    "GETFACTCHARGEANDTARIF", calculationCenterInfo));
                        }
                        processActualPaymentData(actualPayment, data, calculationCenterInfo.getServiceProviderTypeIds());
                    } else {
                        log.error("processActualPayment. Result code is 1 but actualPaymentData is null or empty. ActualPayment id: {}"
                                + ", calculation center: {}",
                                actualPayment.getId(), calculationCenterInfo);
                        logBean.error(Module.NAME, getClass(), ActualPayment.class, actualPayment.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETFACTCHARGEANDTARIF", calculationCenterInfo));
                        actualPayment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case 0:
                    actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("processActualPayment. Unexpected result code: {}. ActualPayment id: {}, calculation center: {}",
                            new Object[]{resultCode, actualPayment.getId(), calculationCenterInfo});
                    logBean.error(Module.NAME, getClass(), ActualPayment.class, actualPayment.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                            "GETFACTCHARGEANDTARIF", resultCode, calculationCenterInfo));
                    actualPayment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        }
    }

    protected void processActualPaymentData(ActualPayment actualPayment, ActualPaymentData data, Set<Long> serviceProviderTypeIds) {
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.APARTMENT_FEE)) {
            actualPayment.setField(ActualPaymentDBF.P1, data.getApartmentFeeCharge());
            actualPayment.setField(ActualPaymentDBF.N1, data.getApartmentFeeTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.HEATING)) {
            actualPayment.setField(ActualPaymentDBF.P2, data.getHeatingCharge());
            actualPayment.setField(ActualPaymentDBF.N2, data.getHeatingTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.HOT_WATER_SUPPLY)) {
            actualPayment.setField(ActualPaymentDBF.P3, data.getHotWaterCharge());
            actualPayment.setField(ActualPaymentDBF.N3, data.getHotWaterTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.COLD_WATER_SUPPLY)) {
            actualPayment.setField(ActualPaymentDBF.P4, data.getColdWaterCharge());
            actualPayment.setField(ActualPaymentDBF.N4, data.getColdWaterCharge());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.GAS_SUPPLY)) {
            actualPayment.setField(ActualPaymentDBF.P5, data.getGasCharge());
            actualPayment.setField(ActualPaymentDBF.N5, data.getGasTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.POWER_SUPPLY)) {
            actualPayment.setField(ActualPaymentDBF.P6, data.getPowerCharge());
            actualPayment.setField(ActualPaymentDBF.N6, data.getPowerTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.GARBAGE_DISPOSAL)) {
            actualPayment.setField(ActualPaymentDBF.P7, data.getGarbageDisposalCharge());
            actualPayment.setField(ActualPaymentDBF.N7, data.getGarbageDisposalTarif());
        }
        if (serviceProviderTypeIds.contains(ServiceProviderTypeStrategy.DRAINAGE)) {
            actualPayment.setField(ActualPaymentDBF.P8, data.getDrainageCharge());
            actualPayment.setField(ActualPaymentDBF.N8, data.getDrainageTarif());
        }
        actualPayment.setStatus(RequestStatus.PROCESSED);
    }

    protected SqlSession sqlSession(String dataSource) {
        return sqlSessionFactoryBean.getSqlSessionManager(dataSource);
    }
}