/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.StrategyFactoryStatic;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class DefaultCalculationCenterAdapter extends AbstractCalculationCenterAdapter {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCalculationCenterAdapter.class);

    protected static final String MAPPING_NAMESPACE = DefaultCalculationCenterAdapter.class.getName();

    @Override
    public void prepareCity(Payment payment, String city, Long cityId) {
        payment.setOutgoingCity(city);
    }

    @Override
    public void prepareStreet(Payment payment, String street, Long streetId) {
        payment.setOutgoingStreet(street);
    }

    @Override
    public void prepareStreetType(Payment payment, String streetType, Long streetTypeId) {
        payment.setOutgoingStreetType(streetType);
    }

    @Override
    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, Long buildingId) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    @Override
    public void prepareApartment(Payment payment, String apartment, Long apartmentId) {
        payment.setOutgoingApartment(apartment);
    }

    @Override
    public void acquirePersonAccount(Payment payment) {
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            String districtName = getDistrictName(getCurrentCalculationCenterId());
            params.put("pDistrName", districtName);
            params.put("pStSortName", payment.getOutgoingStreetType());
            params.put("pStreetName", payment.getOutgoingStreet());
            params.put("pHouseNum", payment.getOutgoingBuildingNumber());
            params.put("pHousePart", payment.getOutgoingBuildingCorp());
            params.put("pFlatNum", payment.getOutgoingApartment());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            String result = (String) session.selectOne(MAPPING_NAMESPACE + ".acquirePersonAccount", params);
            processPersonAccountResult(payment, result);

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    protected void processPersonAccountResult(Payment payment, String result) {
        if (result.equals("0")) {
            payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
        } else if (result.equals("-1")) {
            payment.setStatus(Status.MORE_ONE_ACCOUNTS);
        } else if (result.equals("-2")) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED);
        } else if (result.equals("-3")) {
            payment.setStatus(Status.BUILDING_CORP_UNRESOLVED);
        } else if (result.equals("-4")) {
            payment.setStatus(Status.BUILDING_UNRESOLVED);
        } else if (result.equals("-5")) {
            payment.setStatus(Status.STREET_UNRESOLVED);
        } else if (result.equals("-6")) {
            payment.setStatus(Status.STREET_TYPE_UNRESOLVED);
        } else if (result.equals("-7")) {
            payment.setStatus(Status.DISTRICT_NOT_FOUND);
        } else {
            if (Strings.isEmpty(result)) {
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            } else {
                payment.setAccountNumber(result);
                payment.setStatus(Status.ACCOUNT_NUMBER_RESOLVED);
            }
        }
    }

//    protected boolean processAccountCorrectionDetailsResult(Payment payment, String result) {
//        boolean error = true;
//        if (result.equals("0")) {
//            payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
//        } else if (result.equals("-2")) {
//            payment.setStatus(Status.APARTMENT_UNRESOLVED);
//        } else if (result.equals("-3")) {
//            payment.setStatus(Status.BUILDING_CORP_UNRESOLVED);
//        } else if (result.equals("-4")) {
//            payment.setStatus(Status.BUILDING_UNRESOLVED);
//        } else if (result.equals("-5")) {
//            payment.setStatus(Status.STREET_UNRESOLVED);
//        } else if (result.equals("-6")) {
//            payment.setStatus(Status.STREET_TYPE_UNRESOLVED);
//        } else if (result.equals("-7")) {
//            payment.setStatus(Status.DISTRICT_NOT_FOUND);
//        } else {
//            error = false;
//        }
//        return error;
//    }
    @Override
    public List<AccountCorrectionDetail> acquireAccountCorrectionDetails(Payment payment) {
        List<AccountCorrectionDetail> accountCorrectionDetails = null;
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            String districtName = getDistrictName(getCurrentCalculationCenterId());
            params.put("pDistrName", districtName);
            params.put("pStSortName", payment.getOutgoingStreetType());
            params.put("pStreetName", payment.getOutgoingStreet());
            params.put("pHouseNum", payment.getOutgoingBuildingNumber());
            params.put("pHousePart", payment.getOutgoingBuildingCorp());
            params.put("pFlatNum", payment.getOutgoingApartment());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
//                if (processAccountCorrectionDetailsResult(payment, String.valueOf(resultCode))) {
                accountCorrectionDetails = (List<AccountCorrectionDetail>) params.get("details");
                if (accountCorrectionDetails != null) {
                    boolean isIncorrectResult = false;
                    for (AccountCorrectionDetail detail : accountCorrectionDetails) {
                        if (Strings.isEmpty(detail.getAccountNumber())) {
                            isIncorrectResult = true;
                            break;
                        }
                    }
                    if (isIncorrectResult) {
                        accountCorrectionDetails = null;
                    }
                }
//                }
            } catch (Exception e) {
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            }

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return accountCorrectionDetails;
    }

    protected long getCurrentCalculationCenterId() {
        return getEjbBean(CalculationCenterBean.class).getCurrentCalculationCenterInfo().getId();
    }

    protected String getDistrictName(long calculationCenterId) {
        OrganizationStrategy organizationStrategy = getOrganizationStrategy();
        DomainObject oszn = organizationStrategy.findById(calculationCenterId);
        return organizationStrategy.getDistrictName(oszn);
    }

    protected OrganizationStrategy getOrganizationStrategy() {
        return (OrganizationStrategy) StrategyFactoryStatic.getStrategy("organization");
    }

    @Override
    public void processPayment(Payment payment) {
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("accountNumber", payment.getAccountNumber());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".processPayment", params);
                List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("data");
                if (data != null && (data.size() == 1)) {
                    setPaymentData(payment, data.get(0));
                    payment.setStatus(Status.PROCESSED);
                } else {
                   payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("", e);
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            }

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    protected void setPaymentData(Payment original, Map<String, Object> fromDb) {
        original.setField(PaymentDBF.FROG, fromDb.get("FROG"));
        original.setField(PaymentDBF.FL_PAY, fromDb.get("FL_PAY"));
        original.setField(PaymentDBF.NM_PAY, fromDb.get("NM_PAY"));
        original.setField(PaymentDBF.DEBT, fromDb.get("DEBT"));
        original.setField(PaymentDBF.NORM_F_1, fromDb.get("NORM_F_1"));
        original.setField(PaymentDBF.NUMB, fromDb.get("NUMB"));
        original.setField(PaymentDBF.MARK, fromDb.get("MARK"));
    }
}