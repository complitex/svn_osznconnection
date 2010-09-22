/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.EntityTypeCorrection;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.entity.example.ObjectCorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressCorrectionBean extends CorrectionBean {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();

    @Transactional
    private Long findCorrectionAddressId(String entityTable, String value, long organizationId, Long parentId) {
        ObjectCorrection parameter = new ObjectCorrection(entityTable, value, organizationId, parentId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findCorrectionAddressId", parameter);
    }

    public Long findCorrectionCity(String city, long organizationId) {
        return findCorrectionAddressId("city", city, organizationId, null);
    }

    public Long findCorrectionStreet(long cityId, String street, long organizationId) {
        return findCorrectionAddressId("street", street, organizationId, cityId);
    }

    @Transactional
    public Long findCorrectionBuilding(long streetId, String buildingNumber, String buildingCorp, long organizationId) {
        BuildingCorrection parameter = new BuildingCorrection(buildingNumber, buildingCorp, organizationId, streetId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findCorrectionBuilding", parameter);
    }

    public Long findCorrectionApartment(long buildingId, String apartment, long organizationId) {
        return findCorrectionAddressId("apartment", apartment, organizationId, buildingId);
    }

    @Transactional
    private ObjectCorrection findOutgoingAddress(String entityTable, long organizationId, long internalObjectId) {
        ObjectCorrection parameter = new ObjectCorrection(organizationId, internalObjectId, entityTable);
        ObjectCorrection result = (ObjectCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingAddress", parameter);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    public ObjectCorrection findOutgoingCity(long organizationId, long internalCityId) {
        return findOutgoingAddress("city", organizationId, internalCityId);
    }

    public ObjectCorrection findOutgoingStreet(long organizationId, long internalStreetId) {
        return findOutgoingAddress("street", organizationId, internalStreetId);
    }

    @Transactional
    public BuildingCorrection findOutgoingBuilding(long organizationId, long internalBuildingId) {
        BuildingCorrection parameter = new BuildingCorrection(organizationId, internalBuildingId);
        BuildingCorrection result = (BuildingCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingBuilding", parameter);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    public ObjectCorrection findOutgoingApartment(long organizationId, long internalApartmentId) {
        return findOutgoingAddress("apartment", organizationId, internalApartmentId);
    }

    @Transactional
    public ObjectCorrection findOutgoingDistrict(long calculationCenterId, long osznId) {
        Map<String, Long> params = ImmutableMap.of("calculationCenterId", calculationCenterId, "osznId", osznId);
        ObjectCorrection result = (ObjectCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingDistrict", params);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    private EntityTypeCorrection findOutgoingEntityType(long entityTypeId, long calculationCenterId) {
        EntityTypeCorrection parameter = new EntityTypeCorrection(calculationCenterId, entityTypeId);
        EntityTypeCorrection result = (EntityTypeCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingEntityType", parameter);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    public EntityTypeCorrection findOutgoingStreetType(long calculationCenterId, long internalStreetTypeId) {
        return findOutgoingEntityType(internalStreetTypeId, calculationCenterId);
    }

    @Transactional
    private void insert(String entityTable, String value, long objectId, long organizationId) {
        ObjectCorrection correction = new ObjectCorrection(value, organizationId, objectId, entityTable);
        insert(correction);
    }

    public void insertCorrectionApartment(String apartment, long objectId, long organizationId) {
        insert("apartment", apartment, objectId, organizationId);
    }

    @Transactional
    public void insertCorrectionBuilding(String buildingNumber, String buildingCorp, long objectId, long organizationId) {
        BuildingCorrection correction = new BuildingCorrection(buildingNumber, buildingCorp, organizationId, null);
        correction.setInternalObjectId(objectId);
        insertBuilding(correction);
    }

    @Transactional
    public void insertBuilding(BuildingCorrection correction) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuilding", correction);
    }

    public void insertCorrectionStreet(String street, long objectId, long organizationId) {
        insert("street", street, objectId, organizationId);
    }

    public void insertCorrectionCity(String city, long objectId, long organizationId) {
        insert("city", city, objectId, organizationId);
    }

    private Long findInternalObjectId(String entity, String correction, long attributeTypeId, Long parentId, Long entityTypeId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("correction", correction != null ? correction.trim() : correction);
        params.put("attributeTypeId", attributeTypeId);
        params.put("parentId", parentId);
        params.put("entityTypeId", entityTypeId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalObjectId", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    @Transactional
    public Long findInternalCity(String city) {
        return findInternalObjectId("city", city, 400, null, null);
    }

    @Transactional
    public Long findInternalStreet(String street, Long cityId, Long entityTypeId) {
        return findInternalObjectId("street", street, 300, cityId, entityTypeId);
    }

    @Transactional
    public Long findInternalBuilding(String buildingNumber, String buildingCorp, Long streetId, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", buildingNumber != null ? buildingNumber.trim() : buildingNumber);
        params.put("streetId", streetId);
        if (!Strings.isEmpty(buildingCorp)) {
            params.put("corp", buildingCorp.trim());
        }
        params.put("parentId", cityId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalBuilding", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    @Transactional
    public void updateBuilding(BuildingCorrection correction) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateBuilding", correction);
    }

    @Transactional
    public BuildingCorrection findBuildingById(long buildingCorrectionId) {
        List<BuildingCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findBuildingById", buildingCorrectionId);
        if (corrections != null && (corrections.size() == 1)) {
            return corrections.get(0);
        }
        return null;
    }

    @Transactional
    public List<BuildingCorrection> findBuildings(ObjectCorrectionExample example) {
        return (List<BuildingCorrection>) super.find(example, MAPPING_NAMESPACE + ".findBuildings");
    }
}
