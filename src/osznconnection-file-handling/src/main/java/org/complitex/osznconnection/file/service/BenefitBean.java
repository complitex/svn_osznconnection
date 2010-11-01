package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.BenefitExample;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обработка записей файла запроса возмещения по льготам 
 *
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
@Stateless(name = "BenefitBean")
public class BenefitBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = BenefitBean.class.getName();

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
    private int boundCount(long fileId) {
        return countByFile(fileId, RequestStatus.notBoundStatuses());
    }

    /**
     * @param fileId
     * @return Связан ли файл
     */
    @Transactional
    public boolean isBenefitFileBound(long fileId) {
        return boundCount(fileId) == 0;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<Benefit> find(BenefitExample example) {
        return (List<Benefit>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
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
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectBenefits", requestFile.getId());
    }

    @Transactional
    public void insert(Benefit benefit) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefit", benefit);
    }

    @Transactional
    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteBenefits", requestFile.getId());
    }

    /**
     * Когда у payment записи в UI вручную меняют адрес, у этой записи и у всех соотвествующих benefit записей статус проставляется в ADDRESS_CORRECTED.
     * Данный метод проставляет статус для benefit записей.
     * @param paymentId
     */
    @Transactional
    public void addressCorrected(long paymentId) {
        sqlSession().update(MAPPING_NAMESPACE + ".addressCorrected", paymentId);
    }

    /**
     * Обновляет номер л/c для всех benefit записей которые соответствуют payment записи c paymentId.
     * @param paymentId id payment записи
     * @param accountNumber номер л/c
     */
    @Transactional
    public void updateAccountNumber(long paymentId, String accountNumber) {
        Map<String, Object> params = new HashMap<String, Object>();
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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fileId", fileId);
        params.put("statuses", RequestStatus.notBoundStatuses());
        sqlSession().update(MAPPING_NAMESPACE + ".updateBindingStatus", params);
    }

    /**
     * Заполняет некоторые поля в benefit записях, соответствующих payment c id = paymentId.
     * Вызывается при обработке payment файла.
     * См. ProcessingRequestBean.process().
     * @param paymentId
     * @param benefit
     */
    @Transactional
    public void populateBenefit(long paymentId, Benefit benefit) {
        Map<String, Object> params = benefit.getDbfFields();
        params.put("paymentId", paymentId);
        sqlSession().update(MAPPING_NAMESPACE + ".populateBenefit", params);
    }

    /**
     * Возвращает кол-во необработанных записей
     * @param fileId
     * @return
     */
    private int processedCount(long fileId) {
        return countByFile(fileId, RequestStatus.notProcessedStatuses());
    }

     /**
     *
     * @param fileId
     * @return Обработан ли файл
     */
    @Transactional
    public boolean isBenefitFileProcessed(long fileId) {
        return processedCount(fileId) == 0;
    }

    /**
     * Возвращает кол-во записей со статусами из списка statuses
     * @param fileId
     * @param statuses
     * @return
     */
    private int countByFile(long fileId, List<RequestStatus> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
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

    @SuppressWarnings({"unchecked"})
    @Transactional
    public Date findDat1(final Benefit benefit) {
        return (Date) sqlSession().selectOne(MAPPING_NAMESPACE + ".findDat1",
                new HashMap<String, Object>(){{
                    put("fileId", benefit.getRequestFileId());
                    put("accountNumber", benefit.getAccountNumber());
                }});
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

    /**
     * очищает колонки которые заполняются во время связывания и обработки для записей benefit
     * @param fileId
     */
    @Transactional
    public void clearBeforeBinding(long fileId) {
        Benefit parameter = new Benefit();
        parameter.setRequestFileId(fileId);
        parameter.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding", parameter);
    }

    /**
     * очищает колонки которые заполняются во время обработки для записей benefit
     * @param fileId
     */
    @Transactional
    public void clearBeforeProcessing(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("statuses", RequestStatus.notBoundStatuses());
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing", params);
    }
}
