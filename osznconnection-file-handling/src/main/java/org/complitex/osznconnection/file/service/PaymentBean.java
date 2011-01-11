package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Обработка записей файла запроса начислений
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless(name = "PaymentBean")
public class PaymentBean extends AbstractRequestBean {

    private static final Logger log = LoggerFactory.getLogger(PaymentBean.class);
    public static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    public enum OrderBy {

        ACCOUNT(PaymentDBF.OWN_NUM_SR.name()),
        FIRST_NAME(PaymentDBF.F_NAM.name()),
        MIDDLE_NAME(PaymentDBF.M_NAM.name()),
        LAST_NAME(PaymentDBF.SUR_NAM.name()),
        CITY(PaymentDBF.N_NAME.name()),
        STREET(PaymentDBF.VUL_NAME.name()),
        BUILDING(PaymentDBF.BLD_NUM.name()),
        CORP(PaymentDBF.CORP_NUM.name()),
        APARTMENT(PaymentDBF.FLAT.name()),
        STATUS("status");
        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }
    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;
    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;
    @EJB(beanName = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @Transactional
    public int count(PaymentExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<Payment> find(PaymentExample example) {
        List<Payment> payments = sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
        return payments;
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertPaymentList", abstractRequests);
    }

    public List<AbstractRequest> getPayments(RequestFile requestFile) {
        List<AbstractRequest> payments = sqlSession().selectList(MAPPING_NAMESPACE + ".selectPayments", requestFile.getId());
        return payments;
    }

    @Transactional
    public void insert(Payment payment) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertPayment", payment);
    }

    @Transactional
    public void update(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", payment);
    }

    /**
     * Получить все payment записи в файле с id из списка ids
     * @param fileId
     * @param ids
     * @return
     */
    @Transactional
    public List<Payment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    /**
     * Получить все id payment записей в файле
     * @param fileId
     * @return
     */
    @Transactional
    private List<Long> findIdsForOperation(long fileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", fileId);
    }

    /**
     * Получить все id payment записей в файле для связывания
     * @param fileId
     * @return
     */
    @Transactional
    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId);
    }

    /**
     * Получить все id payment записей в файле для обработки
     * @param fileId
     * @return
     */
    @Transactional
    public List<Long> findIdsForProcessing(long fileId) {
        return findIdsForOperation(fileId);
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
     * Возвращает кол-во необработанных записей
     * @param fileId
     * @return
     */
    private int unprocessedCount(long fileId) {
        return countByFile(fileId, RequestStatus.unprocessedStatuses());
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
     * @param fileId
     * @return Связан ли файл
     */
    @Transactional
    public boolean isPaymentFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    /**
     *
     * @param fileId
     * @return Обработан ли файл
     */
    @Transactional
    public boolean isPaymentFileProcessed(long fileId) {
        return unprocessedCount(fileId) == 0;
    }

    @Transactional
    public void markCorrected(long fileId, String city) {
        markCorrected(fileId, city, null, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String street) {
        markCorrected(fileId, city, street, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String street, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("city", city);
        params.put("street", street);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", params);
    }

    /**
     * Обновляет номер л/c для payment записи, всех связанных benefit записей, и записывает в локальную таблицу номеров л/c.
     * @param payment
     */
    @Transactional
    public void updateAccountNumber(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", payment);
        benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());

        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();

        personAccountLocalBean.saveOrUpdate(payment.getAccountNumber(), (String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.N_NAME), null, (String) payment.getField(PaymentDBF.VUL_NAME), null,
                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                (String) payment.getField(PaymentDBF.FLAT), (String) payment.getField(PaymentDBF.OWN_NUM_SR),
                payment.getOrganizationId(), calculationCenterId);

        long paymentFileId = payment.getRequestFileId();
        long benefitFileId = requestFileGroupBean.getBenefitFileId(paymentFileId);
        if (isPaymentFileBound(paymentFileId) && benefitBean.isBenefitFileBound(benefitFileId)) {
            requestFileGroupBean.updateStatus(paymentFileId, RequestFileGroup.STATUS.BOUND);
        }
    }

    /**
     * очищает колонки которые заполняются во время связывания и обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeBinding(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("status", RequestStatus.CITY_UNRESOLVED_LOCALLY);
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding", params);
        clearWarnings(fileId, RequestFile.TYPE.PAYMENT);
    }

    /**
     * очищает колонки которые заполняются во время обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeProcessing(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("statuses", RequestStatus.unboundStatuses());
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing", params);
        clearWarnings(fileId, RequestFile.TYPE.PAYMENT);
    }
}
