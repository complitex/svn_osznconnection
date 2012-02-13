package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.complitex.osznconnection.service_provider_type.strategy.ServiceProviderTypeStrategy;

/**
 * Обработка записей файла запроса начислений
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless
public class PaymentBean extends AbstractRequestBean {

    public static final String MAPPING_NAMESPACE = PaymentBean.class.getName();
    // service provider type id <-> set of actual payment fields that should be updated.
    private static final Map<Long, Set<PaymentDBF>> UPDATE_FIELD_MAP =
            ImmutableMap.<Long, Set<PaymentDBF>>builder().
            put(ServiceProviderTypeStrategy.APARTMENT_FEE, ImmutableSet.of(PaymentDBF.CODE2_1)).
            put(ServiceProviderTypeStrategy.HEATING, ImmutableSet.of(PaymentDBF.NORM_F_2, PaymentDBF.CODE2_2)).
            put(ServiceProviderTypeStrategy.HOT_WATER_SUPPLY, ImmutableSet.of(PaymentDBF.NORM_F_3, PaymentDBF.CODE2_3)).
            put(ServiceProviderTypeStrategy.COLD_WATER_SUPPLY, ImmutableSet.of(PaymentDBF.NORM_F_4, PaymentDBF.CODE2_4)).
            put(ServiceProviderTypeStrategy.GAS_SUPPLY, ImmutableSet.of(PaymentDBF.NORM_F_5, PaymentDBF.CODE2_5)).
            put(ServiceProviderTypeStrategy.POWER_SUPPLY, ImmutableSet.of(PaymentDBF.NORM_F_6, PaymentDBF.CODE2_6)).
            put(ServiceProviderTypeStrategy.GARBAGE_DISPOSAL, ImmutableSet.of(PaymentDBF.NORM_F_7, PaymentDBF.CODE2_7)).
            put(ServiceProviderTypeStrategy.DRAINAGE, ImmutableSet.of(PaymentDBF.NORM_F_8, PaymentDBF.CODE2_8)).
            build();

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

    @Transactional
    public void update(Payment payment, Set<Long> serviceProviderTypeIds) {
        Map<String, Object> updateFieldMap = null;
        if (serviceProviderTypeIds != null && !serviceProviderTypeIds.isEmpty()) {
            updateFieldMap = Maps.newHashMap();
            for (PaymentDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), payment.getField(field));
            }
        }
        payment.setUpdateFieldMap(updateFieldMap);
        update(payment);
    }

    private Set<PaymentDBF> getUpdateableFields(Set<Long> serviceProviderTypeIds) {
        final Set<PaymentDBF> updateableFields = Sets.newHashSet();

        for (long serviceProviderTypeId : serviceProviderTypeIds) {
            Set<PaymentDBF> fields = UPDATE_FIELD_MAP.get(serviceProviderTypeId);
            if (fields != null) {
                updateableFields.addAll(fields);
            }
        }

        return Collections.unmodifiableSet(updateableFields);
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

    @Transactional
    public void updateAccountNumber(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", payment);
    }

    /**
     * очищает колонки которые заполняются во время связывания и обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeBinding(long fileId, Set<Long> serviceProviderTypeIds) {
        Map<String, Object> updateFieldMap = null;
        if (serviceProviderTypeIds != null && !serviceProviderTypeIds.isEmpty()) {
            updateFieldMap = Maps.newHashMap();
            for (PaymentDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), null);
            }
        }

        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding",
                ImmutableMap.of("status", RequestStatus.LOADED, "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFile.TYPE.PAYMENT);
    }

    /**
     * очищает колонки которые заполняются во время обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeProcessing(long fileId, Set<Long> serviceProviderTypeIds) {
        Map<String, Object> updateFieldMap = null;
        if (serviceProviderTypeIds != null && !serviceProviderTypeIds.isEmpty()) {
            updateFieldMap = Maps.newHashMap();
            for (PaymentDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), null);
            }
        }

        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing",
                ImmutableMap.of("statuses", RequestStatus.unboundStatuses(), "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFile.TYPE.PAYMENT);
    }
}
