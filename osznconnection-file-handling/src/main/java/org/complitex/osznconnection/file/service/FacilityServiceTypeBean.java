/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.FacilityServiceTypeExample;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static org.complitex.osznconnection.file.entity.FacilityServiceTypeDBF.CDUL;

/**
 *
 * @author Artem
 */
@Stateless
public class FacilityServiceTypeBean extends AbstractRequestBean {

    public static final String NS = FacilityServiceTypeBean.class.getName();
    private static final Map<Long, Set<FacilityServiceTypeDBF>> UPDATE_FIELD_MAP = of();

    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    public enum OrderBy {

        RAH(FacilityServiceTypeDBF.RAH.name()),
        IDCODE(FacilityServiceTypeDBF.IDCODE.name()),
        FIRST_NAME("first_name"),
        MIDDLE_NAME("middle_name"),
        LAST_NAME("last_name"),
        STREET_CODE(CDUL.name()),
        STREET_REFERENCE("street_reference"),
        BUILDING(FacilityServiceTypeDBF.HOUSE.name()),
        CORP(FacilityServiceTypeDBF.BUILD.name()),
        APARTMENT(FacilityServiceTypeDBF.APT.name()),
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
        sqlSession().delete(NS + ".deleteFacilityServiceType", requestFileId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(NS + ".insertFacilityServiceTypeList", abstractRequests);
    }

    @Transactional
    public int count(FacilityServiceTypeExample example) {
        return sqlSession().selectOne(NS + ".count", example);
    }

    @Transactional
    public List<FacilityServiceType> find(FacilityServiceTypeExample example) {
        List<FacilityServiceType> list = sqlSession().selectList(NS + ".find", example);

        loadFacilityStreet(list);

        return list;
    }

    @Transactional
    public boolean isFacilityServiceTypeFileBound(long fileId) {
        return unboundCount(fileId) == 0;
    }

    private int unboundCount(long fileId) {
        return countByFile(fileId, RequestStatus.unboundStatuses());
    }

    private int countByFile(long fileId, Set<RequestStatus> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return sqlSession().selectOne(NS + ".countByFile", params);
    }

    @Transactional
    public void update(FacilityServiceType facilityServiceType) {
        sqlSession().update(NS + ".update", facilityServiceType);
    }

    @Transactional
    public void updateAccountNumber(FacilityServiceType facilityServiceType) {
        sqlSession().update(NS + ".updateAccountNumber", facilityServiceType);
    }

    @Transactional
    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId);
    }

    @Transactional
    private List<Long> findIdsForOperation(long fileId) {
        return sqlSession().selectList(NS + ".findIdsForOperation", fileId);
    }

    @Transactional
    public List<FacilityServiceType> findForOperation(long fileId, List<Long> ids) {
        List<FacilityServiceType> list = sqlSession().selectList(NS + ".findForOperation",
                of("requestFileId", fileId, "ids", ids));

        loadFacilityStreet(list);

        return list;
    }

    @Transactional
    public void clearBeforeBinding(long fileId, Set<Long> serviceProviderTypeIds) {
        Map<String, String> updateFieldMap = null;
        if (serviceProviderTypeIds != null && !serviceProviderTypeIds.isEmpty()) {
            updateFieldMap = Maps.newHashMap();
            for (FacilityServiceTypeDBF field : getUpdateableFields(serviceProviderTypeIds)) {
                updateFieldMap.put(field.name(), "-1");
            }
        }

        sqlSession().update(NS + ".clearBeforeBinding",
                of("status", RequestStatus.LOADED, "fileId", fileId, "updateFieldMap", updateFieldMap));
        clearWarnings(fileId, RequestFile.TYPE.FACILITY_SERVICE_TYPE);
    }

    private Set<FacilityServiceTypeDBF> getUpdateableFields(Set<Long> serviceProviderTypeIds) {
        final Set<FacilityServiceTypeDBF> updateableFields = Sets.newHashSet();

        for (long serviceProviderTypeId : serviceProviderTypeIds) {
            Set<FacilityServiceTypeDBF> fields = UPDATE_FIELD_MAP.get(serviceProviderTypeId);
            if (fields != null) {
                updateableFields.addAll(fields);
            }
        }
        return Collections.unmodifiableSet(updateableFields);
    }

    @Transactional
    public void markCorrected(long fileId) {
        markCorrected(fileId, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String streetTypeCode, String streetCode) {
        markCorrected(fileId, streetTypeCode, streetCode, null, null);
    }

    @Transactional
    public void markCorrected(long fileId, String streetTypeCode, String streetCode, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("fileId", fileId);
        params.put("streetCode", streetCode);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        params.put("streetTypeCode", streetTypeCode);

        sqlSession().update(NS + ".markCorrected", params);
    }

    public List<AbstractRequest> getFacilityServiceType(long requestFileId) {
        return sqlSession().selectList(NS + ".selectFacilityServiceType", requestFileId);
    }

    public void loadFacilityStreet(List<FacilityServiceType> list){
        for (FacilityServiceType f : list){
            FacilityStreet facilityStreet = facilityReferenceBookBean.getFacilityStreet(f.getRequestFileId(), f.getStringField(CDUL));

            f.setStreetReference(facilityStreet.getStringField(FacilityStreetDBF.KL_NAME));
            f.setStreetTypeReference(facilityStreet.getStreetType());
            f.setStreetTypeReferenceCode(facilityStreet.getStreetTypeCode());
        }
    }
}
