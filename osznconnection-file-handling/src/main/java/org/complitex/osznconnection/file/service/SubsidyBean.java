/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.entity.example.SubsidyExample;

/**
 *
 * @author Artem
 */
@Stateless
public class SubsidyBean extends AbstractRequestBean {

    public static final String MAPPING_NAMESPACE = SubsidyBean.class.getName();
    private static final Map<Long, Set<SubsidyDBF>> UPDATE_FIELD_MAP = ImmutableMap.of();

    public enum OrderBy {

        RASH(SubsidyDBF.RASH.name()),
        FIRST_NAME("first_name"),
        MIDDLE_NAME("middle_name"),
        LAST_NAME("last_name"),
        CITY(SubsidyDBF.NP_NAME.name()),
        STREET(SubsidyDBF.NAME_V.name()),
        BUILDING(SubsidyDBF.BLD.name()),
        CORP(SubsidyDBF.CORP.name()),
        APARTMENT(SubsidyDBF.FLAT.name()),
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
    public void delete(long requestFileId) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteSubsidies", requestFileId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertSubsidyList", abstractRequests);
    }

    @Transactional
    public int count(SubsidyExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<Subsidy> find(SubsidyExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void updateAccountNumber(Subsidy subsidy) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", subsidy);
    }

    @Transactional
    public boolean isSubsidyFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    private int unboundCount(long fileId) {
        return countByFile(fileId, RequestStatus.unboundStatuses());
    }

    private int countByFile(long fileId, Set<RequestStatus> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    @Transactional
    public void update(Subsidy subsidy) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", subsidy);
    }

    @Transactional
    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId);
    }

    @Transactional
    private List<Long> findIdsForOperation(long fileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", fileId);
    }

    @Transactional
    public List<Subsidy> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    @Transactional
    public void clearBeforeBinding(long fileId, Set<Long> serviceProviderTypeIds) {
        Map<String, String> updateFieldMap = null;
        if (serviceProviderTypeIds != null && !serviceProviderTypeIds.isEmpty()) {
            updateFieldMap = Maps.newHashMap();
            for (SubsidyDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), "-1");
            }
        }

        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding",
                ImmutableMap.of("status", RequestStatus.LOADED, "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFile.TYPE.SUBSIDY);
    }

    private Set<SubsidyDBF> getUpdateableFields(Set<Long> serviceProviderTypeIds) {
        final Set<SubsidyDBF> updateableFields = Sets.newHashSet();

        for (long serviceProviderTypeId : serviceProviderTypeIds) {
            Set<SubsidyDBF> fields = UPDATE_FIELD_MAP.get(serviceProviderTypeId);
            if (fields != null) {
                updateableFields.addAll(fields);
            }
        }

        return Collections.unmodifiableSet(updateableFields);
    }

    @Transactional
    public void markCorrected(long fileId, String city) {
        markCorrected(fileId, city, null, null, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String streetType) {
        markCorrected(fileId, city, streetType, null, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String streetType, String streetCode) {
        markCorrected(fileId, city, streetType, streetCode, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String city, String streetType, String streetCode,
            String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("city", city);
        params.put("streetType", streetType);
        params.put("streetCode", streetCode);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", params);
    }

    public List<AbstractRequest> getSubsidies(long requestFileId) {
        List<AbstractRequest> subsidies = sqlSession().selectList(MAPPING_NAMESPACE + ".selectSubsidies", requestFileId);
        return subsidies;
    }

    public List<Subsidy> findWithTheSameRash(long fileId, String rash) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findWithTheSameRash", ImmutableMap.of("fileId", fileId, "rash", rash));
    }
}
