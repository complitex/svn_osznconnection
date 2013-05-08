/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.DwellingCharacteristicsExample;

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
public class DwellingCharacteristicsBean extends AbstractRequestBean {

    public static final String MAPPING_NAMESPACE = DwellingCharacteristicsBean.class.getName();
    private static final Map<Long, Set<DwellingCharacteristicsDBF>> UPDATE_FIELD_MAP = ImmutableMap.of();

    public enum OrderBy {
        IDCODE(DwellingCharacteristicsDBF.IDCODE.name()),
        FIRST_NAME("first_name"),
        MIDDLE_NAME("middle_name"),
        LAST_NAME("last_name"),
        STREET_CODE(DwellingCharacteristicsDBF.CDUL.name()),
        STREET_REFERENCE("street_reference"),
        BUILDING(DwellingCharacteristicsDBF.HOUSE.name()),
        CORP(DwellingCharacteristicsDBF.BUILD.name()),
        APARTMENT(DwellingCharacteristicsDBF.APT.name()),
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
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteDwellingCharacteristics", requestFileId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertDwellingCharacteristicsList", abstractRequests);
    }

    @Transactional
    public int count(DwellingCharacteristicsExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<DwellingCharacteristics> find(DwellingCharacteristicsExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public boolean isDwellingCharacteristicsFileBound(long fileId) {
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
    public void update(DwellingCharacteristics dwellingCharacteristics) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", dwellingCharacteristics);
    }

    @Transactional
    public void updateAccountNumber(DwellingCharacteristics dwellingCharacteristics) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", dwellingCharacteristics);
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
    public List<DwellingCharacteristics> findForOperation(long fileId, List<Long> ids) {
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
            for (DwellingCharacteristicsDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), "-1");
            }
        }

        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding",
                ImmutableMap.of("status", RequestStatus.LOADED, "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFile.TYPE.DWELLING_CHARACTERISTICS);
    }

    private Set<DwellingCharacteristicsDBF> getUpdateableFields(Set<Long> serviceProviderTypeIds) {
        final Set<DwellingCharacteristicsDBF> updateableFields = Sets.newHashSet();

        for (long serviceProviderTypeId : serviceProviderTypeIds) {
            Set<DwellingCharacteristicsDBF> fields = UPDATE_FIELD_MAP.get(serviceProviderTypeId);
            if (fields != null) {
                updateableFields.addAll(fields);
            }
        }

        return Collections.unmodifiableSet(updateableFields);
    }

    @Transactional
    public void markCorrected(long fileId) {
        markCorrected(fileId, null);
    }

    @Transactional
    public void markCorrected(long fileId, String streetCode) {
        markCorrected(fileId, streetCode, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String streetCode,
            String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("streetCode", streetCode);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        sqlSession().update(MAPPING_NAMESPACE + ".markCorrected", params);
    }

    public List<AbstractRequest> getDwellingCharacteristics(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectDwellingCharacteristics", requestFileId);
    }
}
