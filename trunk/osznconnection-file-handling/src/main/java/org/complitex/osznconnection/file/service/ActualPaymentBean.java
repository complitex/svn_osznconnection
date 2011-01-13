/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.example.ActualPaymentExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class ActualPaymentBean extends AbstractRequestBean {

    private static final Logger log = LoggerFactory.getLogger(ActualPaymentBean.class);
    public static final String MAPPING_NAMESPACE = ActualPaymentBean.class.getName();

    public enum OrderBy {

        FIRST_NAME(ActualPaymentDBF.F_NAM.name()),
        MIDDLE_NAME(ActualPaymentDBF.M_NAM.name()),
        LAST_NAME(ActualPaymentDBF.SUR_NAM.name()),
        CITY(ActualPaymentDBF.N_NAME.name()),
        STREET(ActualPaymentDBF.VUL_NAME.name()),
        BUILDING(ActualPaymentDBF.BLD_NUM.name()),
        CORP(ActualPaymentDBF.CORP_NUM.name()),
        APARTMENT(ActualPaymentDBF.FLAT.name()),
        STATUS("status");
        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }
    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;
    @EJB(beanName = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @Transactional
    public int count(ActualPaymentExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<ActualPayment> find(ActualPaymentExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertActualPaymentList", abstractRequests);
    }

    public List<AbstractRequest> getActualPayments(RequestFile requestFile) {
        List<AbstractRequest> payments = sqlSession().selectList(MAPPING_NAMESPACE + ".selectActualPayments", requestFile.getId());
        return payments;
    }

    @Transactional
    public void insert(ActualPayment actualPayment) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertActualPayment", actualPayment);
    }

    @Transactional
    public void update(ActualPayment actualPayment) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", actualPayment);
    }

    @Transactional
    public List<ActualPayment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    @Transactional
    private List<Long> findIdsForOperation(long fileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", fileId);
    }

    @Transactional
    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId);
    }

    @Transactional
    public List<Long> findIdsForProcessing(long fileId) {
        return findIdsForOperation(fileId);
    }

    private int unboundCount(long fileId) {
        return countByFile(fileId, RequestStatus.unboundStatuses());
    }

    private int unprocessedCount(long fileId) {
        return countByFile(fileId, RequestStatus.unprocessedStatuses());
    }

    private int countByFile(long fileId, Set<RequestStatus> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    @Transactional
    public boolean isActualPaymentFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    @Transactional
    public boolean isActualPaymentFileProcessed(long fileId) {
        return unprocessedCount(fileId) == 0;
    }

    @Transactional
    public void markCorrected(long fileId, String city) {
        markCorrected(fileId, city, null, null, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String streetType, String streetCode) {
        markCorrected(fileId, city, streetType, streetCode, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String streetType, String streetCode, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("city", city);
        params.put("streetType", streetType);
        params.put("streetCode", streetCode);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", params);
    }

    @Transactional
    public void updateAccountNumber(ActualPayment actualPayment){
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", actualPayment);
    }

    @Transactional
    public void clearBeforeBinding(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("status", RequestStatus.CITY_UNRESOLVED_LOCALLY);
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding", params);
        clearWarnings(fileId, RequestFile.TYPE.ACTUAL_PAYMENT);
    }

    @Transactional
    public void clearBeforeProcessing(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("statuses", RequestStatus.unboundStatuses());
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing", params);
        clearWarnings(fileId, RequestFile.TYPE.ACTUAL_PAYMENT);
    }
}
