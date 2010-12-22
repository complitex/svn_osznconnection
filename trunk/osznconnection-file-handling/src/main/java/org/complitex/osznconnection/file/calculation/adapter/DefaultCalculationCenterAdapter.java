/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.OwnershipCorrectionBean;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.service.TarifBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.*;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.PaymentAndBenefitData;
import org.complitex.osznconnection.file.service.warning.RequestWarningBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;

/**
 * Класс по умолчанию для взаимодействия с ЦН.
 * @author Artem
 */
@Stateless(name = "org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter")
@TransactionManagement(TransactionManagementType.BEAN)
public class DefaultCalculationCenterAdapter extends AbstractCalculationCenterAdapter {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCalculationCenterAdapter.class);
    protected static final String RESOURCE_BUNDLE = DefaultCalculationCenterAdapter.class.getName();
    protected static final String MAPPING_NAMESPACE = DefaultCalculationCenterAdapter.class.getName();
    @EJB(beanName = "OwnershipCorrectionBean")
    private OwnershipCorrectionBean ownershipCorrectionBean;
    @EJB(beanName = "TarifBean")
    private TarifBean tarifBean;
    @EJB(beanName = "PrivilegeCorrectionBean")
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
    @Override
    public void prepareCity(Payment payment, String city, String cityCode) {
        payment.setOutgoingCity(city);
    }

    @Override
    public void prepareDistrict(Payment payment, String district, String districtCode) {
        payment.setOutgoingDistrict(district);
    }

    @Override
    public void prepareStreet(Payment payment, String street, String streetCode) {
        payment.setOutgoingStreet(street);
    }

    @Override
    public void prepareStreetType(Payment payment, String streetType, String streetTypeCode) {
        payment.setOutgoingStreetType(streetType);
    }

    @Override
    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, String buildingCode) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param payment
     * @param apartment
     * @param apartmentCode
     */
    @Override
    public void prepareApartment(Payment payment, String apartment, String apartmentCode) {
        String flat = (String) payment.getField(PaymentDBF.FLAT);
        if (flat != null) {
            flat = flat.trim();
        }
        if (Strings.isEmpty(flat)) {
            flat = "";
        }
        payment.setOutgoingApartment(flat);
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
     * @param payment запрос начислений
     */
    @Override
    public void acquirePersonAccount(Payment payment) throws DBException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pDistrName", payment.getOutgoingDistrict());
        params.put("pStSortName", payment.getOutgoingStreetType());
        params.put("pStreetName", payment.getOutgoingStreet());
        params.put("pHouseNum", payment.getOutgoingBuildingNumber());
        params.put("pHousePart", payment.getOutgoingBuildingCorp());
        params.put("pFlatNum", payment.getOutgoingApartment());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        String result = null;
        try {
            result = (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".acquirePersonAccount", params);
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            log.info("acquirePersonAccount. Parameters : {}, result : {}", params, result);
        }

        if (result.equals("0")) {
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
        } else if (result.equals("-1")) {
            payment.setStatus(RequestStatus.MORE_ONE_ACCOUNTS);
        } else if (result.equals("-2")) {
            payment.setStatus(RequestStatus.APARTMENT_NOT_FOUND);
        } else if (result.equals("-3")) {
            payment.setStatus(RequestStatus.BUILDING_CORP_NOT_FOUND);
        } else if (result.equals("-4")) {
            payment.setStatus(RequestStatus.BUILDING_NOT_FOUND);
        } else if (result.equals("-5")) {
            payment.setStatus(RequestStatus.STREET_NOT_FOUND);
        } else if (result.equals("-6")) {
            payment.setStatus(RequestStatus.STREET_TYPE_NOT_FOUND);
        } else if (result.equals("-7")) {
            payment.setStatus(RequestStatus.DISTRICT_NOT_FOUND);
        } else {
            if (Strings.isEmpty(result)) {
                log.error("acquirePersonAccount. Unexpected result code: {}. Payment id: {}", payment.getId());
                logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETMNCODEBYADDRESS",
                        result));
                payment.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
            } else {
                payment.setAccountNumber(result);
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        }
    }

    /**
     * Получить детали по л/c, если acquirePersonAccount() возвратила код -1 : больше 1 л/с.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETACCATTRS.
     * Используется для уточнения в UI номера л/c, когда больше одного человека в ЦН, имеющие разные номера л/c,
     * привязаны к одному адресу и для поиска номеров л/c в PaymentLookupPanel.
     * См. также PaymentLookupBean.getAccounts().
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт,
     * и драйвер с соотвествии со стандартом JDBC рассматривает закрытый курсор как ошибку и выбрасывает исключение.
     * 
     * @param payment
     * @return
     */
    @Override
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) throws DBException {
        List<AccountDetail> accountCorrectionDetails = null;

        Map<String, Object> params = Maps.newHashMap();
        params.put("pDistrName", payment.getOutgoingDistrict());
        params.put("pStSortName", payment.getOutgoingStreetType());
        params.put("pStreetName", payment.getOutgoingStreet());
        params.put("pHouseNum", payment.getOutgoingBuildingNumber());
        params.put("pHousePart", payment.getOutgoingBuildingCorp());
        params.put("pFlatNum", payment.getOutgoingApartment());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            log.info("acquireAccountCorrectionDetails. Parameters : {}", params);
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("acquireAccountCorrectionDetails. Result code is null. Payment id: {}", payment.getId());
            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETACCATTRS", "null"));
            payment.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    accountCorrectionDetails = (List<AccountDetail>) params.get("details");
                    if (accountCorrectionDetails == null || accountCorrectionDetails.isEmpty()) {
                        log.error("acquireAccountCorrectionDetails. Result code is 1 but account details data is null or empty. Payment id: {}",
                                payment.getId());
                        logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(), "GETACCATTRS"));
                        payment.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
                    }
                    break;
                case 0:
                    payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                case -2:
                    payment.setStatus(RequestStatus.APARTMENT_NOT_FOUND);
                    break;
                case -3:
                    payment.setStatus(RequestStatus.BUILDING_CORP_NOT_FOUND);
                    break;
                case -4:
                    payment.setStatus(RequestStatus.BUILDING_NOT_FOUND);
                    break;
                case -5:
                    payment.setStatus(RequestStatus.STREET_NOT_FOUND);
                    break;
                case -6:
                    payment.setStatus(RequestStatus.STREET_TYPE_NOT_FOUND);
                    break;
                case -7:
                    payment.setStatus(RequestStatus.DISTRICT_NOT_FOUND);
                    break;
                default:
                    log.error("acquireAccountCorrectionDetails. Unexpected result code: {}. Payment id: {}", resultCode, payment.getId());
                    logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETACCATTRS",
                            resultCode));
                    payment.setStatus(RequestStatus.BINDING_INVALID_FORMAT);
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
     * @param payment
     * @param benefits
     * @param calculationCenterId
     */
    @Override
    public void processPaymentAndBenefit(Payment payment, List<Benefit> benefits, long calculationCenterId)
            throws DBException {
        payment.setField(PaymentDBF.OPP, "00000001");

        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", payment.getAccountNumber());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".processPaymentAndBenefit", params);
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            log.info("processPaymentAndBenefit. Parameters : {}", params);
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("processPaymentAndBenefit. Result code is null. Payment id: {}", payment.getId());
            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETCHARGEANDPARAMS", "null"));
            payment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    List<PaymentAndBenefitData> paymentAndBenefitDatas = (List<PaymentAndBenefitData>) params.get("data");
                    if (paymentAndBenefitDatas != null && !paymentAndBenefitDatas.isEmpty()) {
                        PaymentAndBenefitData data = paymentAndBenefitDatas.get(0);
                        if (paymentAndBenefitDatas.size() > 1) {
                            log.warn("processPaymentAndBenefit. Size of list of paymentAndBenefitData is more than 1. Only first entry will be used.");
                            logBean.warn(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "payment_and_benefit_data_size", localeBean.getSystemLocale(),
                                    "GETCHARGEANDPARAMS"));
                        }
                        processData(calculationCenterId, payment, benefits, data);
                    } else {
                        log.error("processPaymentAndBenefit. Result code is 1 but paymentAndBenefitData is null or empty. Payment id: {}",
                                payment.getId());
                        logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETCHARGEANDPARAMS"));
                        payment.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case -1:
                    payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("processPaymentAndBenefit. Unexpected result code: {}. Payment id: {}", resultCode, payment.getId());
                    logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETCHARGEANDPARAMS",
                            resultCode));
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
     * @param payment
     * @param benefits
     * @param benefitData данные пришедшие из ЦН.
     */
    protected void processData(long calculationCenterId, Payment payment, List<Benefit> benefits, PaymentAndBenefitData data) {
        //payment
        payment.setField(PaymentDBF.FROG, data.getPercent());
        payment.setField(PaymentDBF.FL_PAY, data.getCharge());
        payment.setField(PaymentDBF.NM_PAY, data.getNormCharge());
        payment.setField(PaymentDBF.DEBT, data.getSaldo());
        payment.setField(PaymentDBF.NORM_F_1, data.getReducedArea());
        payment.setField(PaymentDBF.NUMB, data.getLodgerCount());
        payment.setField(PaymentDBF.MARK, data.getUserCount());

        Double tarif = data.getTarif();
        Integer CODE2_1 = getCODE2_1(tarif, payment.getOrganizationId());
        if (CODE2_1 == null) {
            payment.setStatus(RequestStatus.TARIF_CODE2_1_NOT_FOUND);

            log.error("Couldn't find tarif code by calculation center's tarif: '{}' and calculation center id: {}", tarif, calculationCenterId);

            RequestWarning warning = new RequestWarning(payment.getId(), RequestFile.TYPE.PAYMENT, RequestWarningStatus.TARIF_NOT_FOUND);
            warning.addParameter(new RequestWarningParameter(0, tarif));
            warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
            warningBean.save(warning);

            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.EDIT,
                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
        } else {
            payment.setField(PaymentDBF.CODE2_1, CODE2_1);
            payment.setStatus(RequestStatus.PROCESSED);
        }

        //benefits
        if (benefits != null && !benefits.isEmpty()) {
            String calcCenterOwnershipCode = data.getOwnership();
            Long internalOwnershipId = findInternalOwnership(calcCenterOwnershipCode, calculationCenterId);
            if (internalOwnershipId == null) {
                log.error("Couldn't find in corrections internal ownership object by calculation center's ownership code: '{}' "
                        + "and calculation center id: {}", calcCenterOwnershipCode, calculationCenterId);

                for (Benefit benefit : benefits) {
                    RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                            RequestWarningStatus.OWNERSHIP_OBJECT_NOT_FOUND);
                    warning.addParameter(new RequestWarningParameter(0, calcCenterOwnershipCode));
                    warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                    warningBean.save(warning);

                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                }
            } else {
                long osznId = payment.getOrganizationId();
                String osznOwnershipCode = findOSZNOwnershipCode(internalOwnershipId, osznId);
                if (osznOwnershipCode == null) {
                    log.error("Couldn't find in corrections oszn's ownership code by internal ownership object id: {} "
                            + "and oszn id: {}", internalOwnershipId, osznId);

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
                            benefit.setField(BenefitDBF.OWN_FRM, osznOwnershipCode);
                        }
                    }
                }
            }
            for (Benefit benefit : benefits) {
                benefit.setField(BenefitDBF.CM_AREA, payment.getField(PaymentDBF.NORM_F_1));
                benefit.setField(BenefitDBF.HOSTEL, data.getRoomCount());
            }
        }
    }

    protected Long findInternalOwnership(String calculationCenterOwnership, long calculationCenterId) {
        return ownershipCorrectionBean.findInternalOwnership(calculationCenterOwnership, calculationCenterId);
    }

    protected String findOSZNOwnershipCode(Long internalOwnership, long osznId) {
        return ownershipCorrectionBean.findOwnershipCode(internalOwnership, osznId);
    }

    /**
     * Получить тариф.
     * См. TarifBean.getCODE2_1().
     *
     * @param T11_CS_UNI
     * @param organizationId
     * @return
     */
    protected Integer getCODE2_1(Double T11_CS_UNI, long organizationId) {
        return tarifBean.getCODE2_1(T11_CS_UNI, organizationId);
    }

    @Override
    public Collection<BenefitData> getBenefitData(Benefit benefit, Date dat1) throws DBException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", benefit.getAccountNumber());
        params.put("dat1", dat1);

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".getBenefitData", params);
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            log.info("getBenefitData. Parameters : {}", params);
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("getBenefitData. Result code is null. Benefit id: {}, dat1: {}", benefit.getId(), dat1);
            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                    "GETPRIVS", "null"));
            benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
        } else {
            switch (resultCode) {
                case 1:
                    List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
                    if (benefitData != null && !benefitData.isEmpty()) {
                        if (checkOrderFam("getBenefitData", benefitData, Lists.newArrayList(benefit), dat1) &&
                                checkBenefitCode("getBenefitData", benefitData, Lists.newArrayList(benefit), dat1)) {
                            Collection<BenefitData> emptyList = getEmptyBenefitData(benefitData);
                            if (emptyList != null && !emptyList.isEmpty()) {
                                logEmptyBenefitData("getBenefitData", Lists.newArrayList(benefit), dat1);
                            }

                            Collection<BenefitData> finalBenefitData = getBenefitDataWithMinPriv("getBenefitData", benefitData);
                            finalBenefitData.addAll(emptyList);
                            return finalBenefitData;
                        }
                    } else {
                        log.error("getBenefitData. Result code is 1 but benefit data is null or empty. Benefit id: {}, dat1: {}",
                                benefit.getId(), dat1);
                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                "GETPRIVS"));
                        benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    }
                    break;
                case -1:
                    benefit.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("getBenefitData. Unexpected result code: {}. Benefit id: {}, dat1: {}", new Object[]{resultCode, benefit.getId(), dat1});
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETPRIVS",
                            resultCode));
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        }
        return null;
    }

    protected static class BenefitDataId implements Serializable {

        private String inn;
        private String name;
        private String passport;

        public BenefitDataId(String inn, String name, String passport) {
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

    protected boolean checkOrderFam(String method, List<BenefitData> benefitData, List<Benefit> benefits, Date dat1) {
        String accountNumber = benefits.get(0).getAccountNumber();
        for (BenefitData data : benefitData) {
            if (Strings.isEmpty(data.getOrderFamily())) {
                log.error(method + ". Order fam is null. Account number: {}, dat1: {}", accountNumber, dat1);
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_order_fam_null", localeBean.getSystemLocale(), "GETPRIVS",
                            accountNumber, dat1));
                }
                return false;
            }
        }

        Map<String, BenefitData> orderFams = Maps.newHashMap();
        for (BenefitData data : benefitData) {
            String orderFam = data.getOrderFamily();
            BenefitData dublicate = orderFams.get(orderFam);
            if (dublicate != null) {
                log.error(method + ". Order fam is not unique. At least two benefit data have the same order fam. First: {}, second {}. "
                        + "Account number: {}, dat1: {}", new Object[]{data, dublicate, accountNumber, dat1});
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_order_fam_not_unique", localeBean.getSystemLocale(), "GETPRIVS",
                            accountNumber, dat1));
                }
                return false;
            } else {
                orderFams.put(orderFam, data);
            }
        }
        return true;
    }

    protected boolean checkBenefitCode(String method, List<BenefitData> benefitData, List<Benefit> benefits, Date dat1){
        String accountNumber = benefits.get(0).getAccountNumber();
        for (BenefitData data : benefitData) {
            if (Strings.isEmpty(data.getCode())) {
                log.error(method + ". BenefitData's code is null. Account number: {}, dat1: {}", accountNumber, dat1);
                for (Benefit benefit : benefits) {
                    benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_code_null", localeBean.getSystemLocale(), "GETPRIVS",
                            accountNumber, dat1));
                }
                return false;
            }
        }
        return true;
    }

    protected void logEmptyBenefitData(String method, List<Benefit> benefits, Date dat1) {
        String accountNumber = benefits.get(0).getAccountNumber();
        log.error(method + ". Inn, name and passport of benefit data are null. "
                + "Account number: {}, dat1: {}", accountNumber, dat1);
        for (Benefit benefit : benefits) {
            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "benefit_id_empty", localeBean.getSystemLocale(), "GETPRIVS",
                    accountNumber, dat1));
        }
    }

    protected Map<BenefitDataId, Collection<BenefitData>> groupBenefitData(String method, Collection<BenefitData> benefitData) {
        Map<BenefitDataId, Collection<BenefitData>> groupMap = Maps.newHashMap();
        for (BenefitData data : benefitData) {
            BenefitDataId id = new BenefitDataId(data.getInn(), data.getFirstName() + data.getMiddleName() + data.getLastName(),
                    data.getPassportSerial() + data.getPassportNumber());
            Collection<BenefitData> list = groupMap.get(id);
            if (list == null) {
                list = Lists.newArrayList();
                groupMap.put(id, list);
            }
            list.add(data);
        }
        return groupMap;
    }

    protected Collection<BenefitData> getBenefitDataWithMinPriv(String method, Collection<BenefitData> benefitData) {
        Collection<BenefitData> nonEmptyList = getNonEmptyBenefitData(benefitData);
        Map<BenefitDataId, Collection<BenefitData>> groupMap = groupBenefitData(method, nonEmptyList);
        Collection<BenefitData> benefitDataWithMinPriv = Lists.newArrayList();
        for (Map.Entry<BenefitDataId, Collection<BenefitData>> group : groupMap.entrySet()) {
            BenefitData min = Collections.min(group.getValue(), BENEFIT_DATA_COMPARATOR);
            benefitDataWithMinPriv.add(min);
        }
        return benefitDataWithMinPriv;
    }

    protected Collection<BenefitData> getEmptyBenefitData(Collection<BenefitData> benefitData) {
        return Lists.newArrayList(Iterables.filter(benefitData, new Predicate<BenefitData>() {

            @Override
            public boolean apply(BenefitData data) {
                return data.isEmpty();
            }
        }));
    }

    protected Collection<BenefitData> getNonEmptyBenefitData(Collection<BenefitData> benefitData) {
        Collection<BenefitData> nonEmptyList = Lists.newArrayList(benefitData);
        nonEmptyList.removeAll(getEmptyBenefitData(benefitData));
        return nonEmptyList;
    }

    protected List<BenefitData> getBenefitDataByINN(List<BenefitData> benefitDatas, final String inn) {
        return Lists.newArrayList(Iterables.filter(benefitDatas, new Predicate<BenefitData>() {

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
    @Override
    public void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId) throws DBException {
        String accountNumber = benefits.get(0).getAccountNumber();

        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", accountNumber);
        params.put("dat1", dat1);

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".processBenefit", params);
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            log.info("processBenefit. Parameters : {}", params);
        }

        Integer resultCode = (Integer) params.get("resultCode");
        if (resultCode == null) {
            log.error("processBenefit. Result code is null. Account number: {}, dat1: {}", accountNumber, dat1);
            for (Benefit benefit : benefits) {
                logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(),
                        "GETPRIVS", "null"));
                benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
            }
        } else {
            switch (resultCode) {
                case 1:
                    List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
                    if (benefitData != null && !benefitData.isEmpty()) {
                        if (checkOrderFam("processBenefit", benefitData, benefits, dat1) &&
                                checkBenefitCode("processBenefit", benefitData, benefits, dat1)) {
                            processBenefitData(calculationCenterId, benefits, benefitData, dat1);
                        }
                    } else {
                        log.error("processBenefit. Result code is 1 but benefit data is null or empty. Account number: {}, dat1: {}",
                                accountNumber, dat1);
                        for (Benefit benefit : benefits) {
                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_inconsistent", localeBean.getSystemLocale(),
                                    "GETPRIVS"));
                            benefit.setStatus(RequestStatus.PROCESSING_INVALID_FORMAT);
                        }
                    }
                    break;
                case -1:
                    setStatus(benefits, RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                    break;
                default:
                    log.error("processBenefit. Unexpected result code: {}. Account number: {}, dat1: {}", new Object[]{resultCode, accountNumber, dat1});
                    for (Benefit benefit : benefits) {
                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.GETTING_DATA,
                                ResourceUtil.getFormatString(RESOURCE_BUNDLE, "result_code_unexpected", localeBean.getSystemLocale(), "GETPRIVS",
                                resultCode));
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
    protected void processBenefitData(long calculationCenterId, List<Benefit> benefits, List<BenefitData> benefitData, Date dat1) {
        long osznId = benefits.get(0).getOrganizationId();

        Collection<BenefitData> emptyList = getEmptyBenefitData(benefitData);
        if (emptyList != null && !emptyList.isEmpty()) {
            logEmptyBenefitData("processBenefit", benefits, dat1);
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
                String osznBenefitCode = findOSZNPrivilegeCode(internalPrivilegeId, osznId);
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

    protected String findOSZNPrivilegeCode(Long internalPrivilege, long osznId) {
        return privilegeCorrectionBean.findPrivilegeCode(internalPrivilege, osznId);
    }

    protected List<Benefit> findByPassportNumber(List<Benefit> benefits, final String passportNumber) {
        return Lists.newArrayList(Iterables.filter(benefits, new Predicate<Benefit>() {

            @Override
            public boolean apply(Benefit benefit) {
                return passportNumber.equals(benefit.getField(BenefitDBF.PSP_NUM));
            }
        }));
    }

    protected List<Benefit> findByINN(List<Benefit> benefits, final String inn) {
        return Lists.newArrayList(Iterables.filter(benefits, new Predicate<Benefit>() {

            @Override
            public boolean apply(Benefit benefit) {
                return inn.equals(benefit.getField(BenefitDBF.IND_COD));
            }
        }));
    }
}
