package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.SubsidyExample;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        DAT1(SubsidyDBF.DAT1.name()),
        DAT2(SubsidyDBF.DAT2.name()),
        NUMM(SubsidyDBF.NUMM.name()),
        NM_PAY(SubsidyDBF.NM_PAY.name()),
        SUMMA(SubsidyDBF.SUMMA.name()),
        SUBS(SubsidyDBF.SUBS.name()),
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
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<Subsidy> find(SubsidyExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void updateAccountNumberForSimilarSubs(Subsidy subsidy) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumberForSimislarSubs", subsidy);
    }

    public boolean isSubsidyFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    private int unboundCount(long fileId) {
        return countByFile(fileId, RequestStatus.unboundStatuses());
    }

    public boolean isSubsidyFileFilled(Long requestFileId){
        return countByFile(requestFileId, RequestStatus.unprocessedStatuses()) == 0;
    }

    private int countByFile(long fileId, Set<RequestStatus> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
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
            for (SubsidyDBF field : getUpdatableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), "-1");
            }
        }

        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding",
                ImmutableMap.of("status", RequestStatus.LOADED, "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFileType.SUBSIDY);
    }

    private Set<SubsidyDBF> getUpdatableFields(Set<Long> serviceProviderTypeIds) {
        final Set<SubsidyDBF> updatableFields = Sets.newHashSet();

        for (long serviceProviderTypeId : serviceProviderTypeIds) {
            Set<SubsidyDBF> fields = UPDATE_FIELD_MAP.get(serviceProviderTypeId);
            if (fields != null) {
                updatableFields.addAll(fields);
            }
        }

        return Collections.unmodifiableSet(updatableFields);
    }

    @Transactional
    public void markCorrected(Subsidy subsidy, AddressEntity entity) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("fileId", subsidy.getRequestFileId());

        switch (entity){
            case BUILDING:
                params.put("buildingNumber", subsidy.getBuildingNumber());
                params.put("buildingCorp", subsidy.getBuildingCorp());
            case STREET:
                params.put("street", subsidy.getStreet());
            case STREET_TYPE:
                params.put("streetType", subsidy.getStreetType());
            case CITY:
                params.put("city", subsidy.getCity());
        }

        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", params);
    }

    public List<Subsidy> getSubsidies(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectSubsidies", requestFileId);
    }
}
