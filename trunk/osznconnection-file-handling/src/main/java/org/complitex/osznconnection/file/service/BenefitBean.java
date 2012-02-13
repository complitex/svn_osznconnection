package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.BenefitExample;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import org.complitex.osznconnection.file.entity.BenefitData;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработка записей файла запроса возмещения по льготам 
 *
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
@Stateless(name = "BenefitBean")
public class BenefitBean extends AbstractRequestBean {

    private static final Logger log = LoggerFactory.getLogger(BenefitBean.class);
    public static final String MAPPING_NAMESPACE = BenefitBean.class.getName();
    @EJB
    private PaymentBean paymentBean;
    @EJB
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private PrivilegeCorrectionBean privilegeCorrectionBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;
    @EJB
    private ServiceProviderAdapter adapter;

    public enum OrderBy {

        ACCOUNT(BenefitDBF.OWN_NUM_SR.name()),
        FIRST_NAME(BenefitDBF.F_NAM.name()),
        MIDDLE_NAME(BenefitDBF.M_NAM.name()),
        LAST_NAME(BenefitDBF.SUR_NAM.name()),
        CITY("city"),
        STREET("street"),
        BUILDING("building_number"),
        CORP("building_corp"),
        APARTMENT("apartment"),
        PRIVILEGE(BenefitDBF.PRIV_CAT.name()),
        STATUS("status");
        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public int count(BenefitExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    /**
     * Возвращает кол-во несвязанных записей в файле.
     * @param fileId
     * @return
     */
    private int unboundCount(long fileId) {
        return countByFile(fileId, RequestStatus.unboundStatuses());
    }

    /**
     * @param fileId
     * @return Связан ли файл
     */
    @Transactional
    public boolean isBenefitFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    @Transactional
    public List<Benefit> find(BenefitExample example) {
        List<Benefit> benefits = sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
        return benefits;
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefitList", abstractRequests);
    }

    @SuppressWarnings({"unchecked"})
    public List<AbstractRequest> getBenefits(RequestFile requestFile) {
        List<AbstractRequest> benefits = sqlSession().selectList(MAPPING_NAMESPACE + ".selectBenefits", requestFile.getId());
        return benefits;
    }

    @Transactional
    public void insert(Benefit benefit) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefit", benefit);
    }

    /**
     * Когда у payment записи в UI вручную меняют адрес, у этой записи и у всех соотвествующих benefit записей статус проставляется в ADDRESS_CORRECTED.
     * Данный метод проставляет статус для benefit записей.
     * @param paymentId
     */
    @Transactional
    public void markCorrected(long paymentFileId) {
        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", paymentFileId);
    }

    /**
     * Обновляет номер л/c для всех benefit записей которые соответствуют payment записи c paymentId.
     * @param paymentId id payment записи
     * @param accountNumber номер л/c
     */
    @Transactional
    public void updateAccountNumber(long paymentId, String accountNumber) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("paymentId", paymentId);
        params.put("accountNumber", accountNumber);
        params.put("status", RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", params);
    }

    /**
     * Обновляет статус для всех benefit записей из файла, а именно копирует статус из соответствующей payment записи.
     * @param fileId
     * @param statuses
     */
    @Transactional
    public void updateBindingStatus(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("statuses", RequestStatus.unboundStatuses());
        sqlSession().update(MAPPING_NAMESPACE + ".updateBindingStatus", params);
    }

    /**
     * Заполняет некоторые поля в benefit записях, соответствующих payment c id = paymentId.
     * Вызывается при обработке payment файла.
     * См. ProcessingRequestBean.process().
     * @param benefit
     */
    @Transactional
    public void populateBenefit(Benefit benefit) {
        sqlSession().update(MAPPING_NAMESPACE + ".populateBenefit", benefit);
    }

    /**
     * Возвращает кол-во необработанных записей
     * @param fileId
     * @return
     */
    private int unprocessedCount(long fileId) {
        return countByFile(fileId, RequestStatus.unprocessedStatuses());
    }

    /**
     *
     * @param fileId
     * @return Обработан ли файл
     */
    @Transactional
    public boolean isBenefitFileProcessed(long fileId) {
        return unprocessedCount(fileId) == 0;
    }

    /**
     * Возвращает кол-во записей со статусами из списка statuses
     * @param fileId
     * @param statuses
     * @return
     */
    private int countByFile(long fileId, Set<RequestStatus> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    /**
     * Получает дату из поля DAT1 в записи payment, у которой account number = accountNumber и
     * кроме того поле FROG больше 0(только benefit записи соответствующие таким payment записям нужно обрабатывать).
     * См. ProccessingRequestBean.processBenefit().
     * @param accountNumber
     * @param fileId
     * @return
     */
    @Transactional
    public Date findDat1(String accountNumber, long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("accountNumber", accountNumber);
        return (Date) sqlSession().selectOne(MAPPING_NAMESPACE + ".findDat1", params);
    }

    /**
     * Получает все не null account numbers в файле.
     * @param fileId
     * @return
     */
    @Transactional
    public List<String> getAllAccountNumbers(long fileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".allAccountNumbers", fileId);
    }

    /**
     * Достает все записи benefit по номеру л/c из файла.
     * @param accountNumber
     * @param fileId
     * @return
     */
    @Transactional
    public List<Benefit> findByAccountNumber(String accountNumber, long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("accountNumber", accountNumber);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findByAccountNumber", params);
    }

//    @Transactional
//    public boolean isMarkEqualToBenefitCount(String accountNumber, long fileId) {
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("fileId", fileId);
//        params.put("accountNumber", accountNumber);
//        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".isMarkEqualToBenefitCount", params) == 0;
//    }
    @Transactional
    public void update(Benefit benefit) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", benefit);
    }

    @Transactional
    public void updateStatusByAccountNumber(long fileId, String accountNumber, RequestStatus status) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", accountNumber);
        params.put("fileId", fileId);
        params.put("status", status);
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatusByAccountNumber", params);
    }

    public List<Benefit> findByOZN(Payment payment) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findByOZN", payment);
    }

    /**
     * очищает колонки которые заполняются во время связывания и обработки для записей benefit
     * @param fileId
     */
    @Transactional
    public void clearBeforeBinding(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("status", RequestStatus.LOADED);
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding", params);
        clearWarnings(fileId, RequestFile.TYPE.BENEFIT);
    }

    /**
     * очищает колонки которые заполняются во время обработки для записей benefit
     * @param fileId
     */
    @Transactional
    public void clearBeforeProcessing(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("statuses", RequestStatus.unboundStatuses());
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing", params);
        clearWarnings(fileId, RequestFile.TYPE.BENEFIT);
    }

    public Collection<BenefitData> getBenefitData(Benefit benefit) throws DBException {
        long osznId = benefit.getOrganizationId();
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getInfo();
        Date dat1 = findDat1(benefit.getAccountNumber(), benefit.getRequestFileId());
        Collection<BenefitData> benefitData = adapter.getBenefitData(calculationCenterInfo, benefit, dat1);

        Collection<BenefitData> notConnectedBenefitData = null;
        if (benefitData != null && !benefitData.isEmpty()) {
            notConnectedBenefitData = Lists.newArrayList();
            List<Benefit> benefits = findByAccountNumber(benefit.getAccountNumber(), benefit.getRequestFileId());

            for (BenefitData benefitDataItem : benefitData) {
                boolean suitable = true;

                String benefitOrderFam = benefitDataItem.getOrderFamily();
                Integer benefitOrderFamAsInt = null;
                try {
                    benefitOrderFamAsInt = Integer.valueOf(benefitOrderFam);
                } catch (NumberFormatException e) {
                }

                for (Benefit benefitItem : benefits) {
                    Integer benefitItemOrderFam = (Integer) benefitItem.getField(BenefitDBF.ORD_FAM);
                    if (benefitItemOrderFam != null && benefitItemOrderFam.equals(benefitOrderFamAsInt)) {
                        suitable = false;
                    }
                }

                if (suitable) {
                    String osznBenefitCode = null;
                    Long internalPrivilege = privilegeCorrectionBean.findInternalPrivilege(benefitDataItem.getCode(),
                            calculationCenterInfo.getOrganizationId());
                    if (internalPrivilege != null) {
                        osznBenefitCode = privilegeCorrectionBean.findPrivilegeCode(internalPrivilege, osznId);
                    }
                    benefitDataItem.setPrivilegeObjectId(internalPrivilege);
                    benefitDataItem.setOsznPrivilegeCode(osznBenefitCode);
                    benefitDataItem.setCalcCenterId(calculationCenterInfo.getOrganizationId());
                    notConnectedBenefitData.add(benefitDataItem);
                }
            }
        }
        return notConnectedBenefitData;
    }

    public void connectBenefit(Benefit benefit, final BenefitData selectedBenefitData, boolean checkBenefitData) throws DBException {
        String osznBenefitCode = selectedBenefitData.getOsznPrivilegeCode();
        benefit.setField(BenefitDBF.PRIV_CAT, Integer.valueOf(osznBenefitCode));
        benefit.setField(BenefitDBF.ORD_FAM, Integer.valueOf(selectedBenefitData.getOrderFamily()));
        benefit.setStatus(RequestStatus.PROCESSED);

        update(benefit);

        if (checkBenefitData) {
            Collection<BenefitData> leftBenefitData = getBenefitData(benefit);
            if (leftBenefitData == null || leftBenefitData.isEmpty()) {
                updateStatusByAccountNumber(benefit.getRequestFileId(), benefit.getAccountNumber(), RequestStatus.PROCESSED);
            }
        } else {
            updateStatusByAccountNumber(benefit.getRequestFileId(), benefit.getAccountNumber(), RequestStatus.PROCESSED);
        }

        long benefitFileId = benefit.getRequestFileId();
        long paymentFileId = requestFileGroupBean.getPaymentFileId(benefit.getRequestFileId());
        if (isBenefitFileProcessed(benefitFileId) && paymentBean.isPaymentFileProcessed(paymentFileId)) {
            requestFileGroupBean.updateStatus(benefitFileId, RequestFileStatus.FILLED);
        }
    }
}
