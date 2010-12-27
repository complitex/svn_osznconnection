/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.osznconnection.file.entity.StreetCorrection;

/**
 * Класс для работы с коррекциями адресов.
 * @author Artem
 */
@Stateless(name = "AddressCorrectionBean")
public class AddressCorrectionBean extends CorrectionBean {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);
    private static final String ADDRESS_BEAN_MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();
    @EJB
    private LocaleBean localeBean;

    /**
     * Находит id внутреннего объекта системы в таблице коррекций
     * по коррекции(value), сущности(entityTable), ОСЗН(organizationId) и id родительского объекта.
     * При поиске для значения коррекции применяется SQL функция TRIM().
     * @param entityTable
     * @param buildingNumber
     * @param organizationId
     * @param parentId
     * @return
     */
    @Transactional
    private Correction findCorrectionAddress(final String entityTable, final Long parentId, final String correction,
            final long organizationId) {
        Map<String, Object> params = new HashMap<String, Object>() {

            {
                put("entityTable", entityTable);
                put("parentId", parentId);
                put("correction", correction);
                put("organizationId", organizationId);
            }
        };

        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findCorrectionAddress", params);
        if (corrections.isEmpty()) {
            return null;
        }
        if (corrections.size() == 1) {
            return corrections.get(0);
        }

        throw new RuntimeException("More one correction found. Parameters: [entityTable = " + entityTable + ", parentId = " + parentId + ", correction = "
                + correction + ", organizationId = " + organizationId + "]. Corrections are in inconsistent state.");
    }

    /**
     * Найти id локального города в таблице коррекций.
     * См. findCorrectionAddressId()
     * @param city
     * @param organizationId
     * @return
     */
    public Correction findCorrectionCity(String city, long organizationId) {
        return findCorrectionAddress("city", null, city, organizationId);
    }

    /**
     * Найти id локальной улицы в таблице коррекций.
     * См. findCorrectionAddressId()
     * @param parent
     * @param street
     * @param parent
     * @return
     */
    public StreetCorrection findCorrectionStreet(Correction parent, Long streetTypeCorrectionId, String street) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("parentId", parent.getId());
        params.put("correction", street);
        params.put("streetTypeCorrectionId", streetTypeCorrectionId);
        params.put("organizationId", parent.getOrganizationId());
        List<StreetCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findCorrectionStreet", params);
        if (corrections.isEmpty()) {
            return null;
        }
        if (corrections.size() == 1) {
            return corrections.get(0);
        }

        throw new RuntimeException("More one correction found. Parameters: [entityTable = street, parentId = " + parent.getParentId()
                + ", correction = " + street + ", street type correction id = " + streetTypeCorrectionId + ", organizationId = "
                + parent.getOrganizationId() + "]. Corrections are in inconsistent state.");
    }

    /**
     * Найти id локального дома в таблице коррекций.
     * При поиске для номера и корпуса дома применяется SQL функция TRIM().
     * @param parent
     * @param correction
     * @param buildingNumber
     * @return
     */
    @Transactional
    public BuildingCorrection findCorrectionBuilding(final Correction parent, final String buildingNumber, final String buildingCorp) {
        Map<String, Object> params = new HashMap<String, Object>() {

            {
                put("parentId", parent.getId());
                put("correction", buildingNumber);
                put("correctionCorp", buildingCorp);
                put("organizationId", parent.getOrganizationId());
            }
        };

        List<BuildingCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findCorrectionBuilding", params);
        if (corrections.isEmpty()) {
            return null;
        }
        if (corrections.size() == 1) {
            return corrections.get(0);
        }

        throw new RuntimeException("More one correction found. Parameters: [entityTable = building, parentId = " + parent.getParentId()
                + ", correction = " + buildingNumber + ", correction corp =  " + buildingCorp + ", organizationId = "
                + parent.getOrganizationId() + "]. Corrections are in inconsistent state.");
    }

    /**
     * Находит данные о коррекции(полное название, код) по сущности(entityTable), ОСЗН(organizationId) и id внутреннего объекта системы(internalObjectId)
     * Используется для разрешения адреса для ЦН.
     * @param entityTable
     * @param organizationId
     * @param internalObjectId
     * @return
     */
    @Transactional
    private Correction findOutgoingAddress(String entityTable, long organizationId, long internalObjectId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setEntity(entityTable);
        example.setObjectId(internalObjectId);
        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findOutgoingAddress", example);
        if (corrections != null && corrections.size() == 1) {
            Correction result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для города.
     * См. findOutgoingAddress()
     * @param organizationId
     * @param internalCityId
     * @return
     */
    public Correction findOutgoingCity(long organizationId, long internalCityId) {
        return findOutgoingAddress("city", organizationId, internalCityId);
    }

    /**
     * Найти данные о коррекции для улицы.
     * См. findOutgoingAddress()
     * @param organizationId
     * @param internalStreetId
     * @return
     */
    public StreetCorrection findOutgoingStreet(long organizationId, long internalStreetId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalStreetId);
        List<StreetCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findOutgoingStreet", example);
        if (corrections != null && corrections.size() == 1) {
            StreetCorrection result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для дома.
     * @param organizationId
     * @param internalBuildingId
     * @return
     */
    @Transactional
    public BuildingCorrection findOutgoingBuilding(long organizationId, long internalBuildingId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalBuildingId);
        List<BuildingCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findOutgoingBuilding", example);
        if (corrections != null && corrections.size() == 1) {
            BuildingCorrection result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для района.
     * @param calculationCenterId
     * @param osznId
     * @return
     */
    @Transactional
    public Correction findOutgoingDistrict(long calculationCenterId, long osznId) {
        Map<String, Long> params = ImmutableMap.of("calculationCenterId", calculationCenterId, "osznId", osznId);
        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findOutgoingDistrict", params);
        if (corrections != null && corrections.size() == 1) {
            Correction result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для типа улицы.
     * @param calculationCenterId
     * @param internalStreetTypeId
     * @return
     */
    public Correction findOutgoingStreetType(long calculationCenterId, long internalStreetTypeId) {
        return findOutgoingAddress("street_type", calculationCenterId, internalStreetTypeId);
    }

    /**
     * Вставка коррекции дома.
     * Если значение корпуса дома null, то вставляется пустая строка.
     * @param correction
     */
    @Transactional
    public void insertBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }

        sqlSession().insert(ADDRESS_BEAN_MAPPING_NAMESPACE + ".insertBuilding", correction);
    }

    public Correction createCityCorrection(String city, long cityObjectId, long organizationId, long internalOrganizationId) {
        Correction correction = new Correction("city");
        correction.setParentId(null);
        correction.setCorrection(city);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setObjectId(cityObjectId);
        return correction;
    }

    public StreetCorrection createStreetCorrection(String street, long cityCorrectionId, long streetObjectId, long organizationId,
            long internalOrganizationId) {
        StreetCorrection correction = new StreetCorrection();
        correction.setParentId(cityCorrectionId);
        correction.setCorrection(street);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setObjectId(streetObjectId);
        return correction;
    }

    public BuildingCorrection createBuildingCorrection(String number, String corp, long streetCorrectionId, long buildingObjectId, long organizationId,
            long internalOrganizationId) {
        BuildingCorrection correction = new BuildingCorrection();
        correction.setParentId(streetCorrectionId);
        correction.setCorrection(number);
        correction.setCorrectionCorp(corp);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setObjectId(buildingObjectId);
        return correction;
    }

    /**
     * Найти id внутреннего объекта системы. Поиск идет по сущности(entity), коррекции(correction),
     * типу атрибута по которому сравнивать коррекцию(attributeTypeId), id родильского обьекта(parentId), типу сущности(entityTypeId).
     * При поиске к значению коррекции(correction) применяются SQL функции TRIM() и TO_CYRILLIC()
     * @param entity
     * @param correction
     * @param buildingNumber
     * @param parentId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    private Long findInternalObjectId(String entity, String correction, long attributeTypeId, Long parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("correction", correction != null ? correction.trim() : correction);
        params.put("attributeTypeId", attributeTypeId);
        params.put("parentId", parentId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalObjectId", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти город в локальной адресной базе.
     * См. findInternalObjectId()
     * 
     * @param city
     * @return
     */
    @Transactional
    public Long findInternalCity(String city) {
        return findInternalObjectId("city", city, 400, null);
    }

    /**
     * Найти улицу в локальной адресной базе.
     * См. findInternalObjectId()
     *
     * @param street
     * @param cityId
     * @return
     */
    @Transactional
    public Long findInternalStreet(Long streetTypeId, String street, long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("correction", street);
        params.put("parentId", cityId);
        params.put("streetTypeId", streetTypeId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalStreetId", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти дом в локальной адресной базе.
     * При поиске к значению номера(buildingNumber) и корпуса(buildingCorp) дома применяются SQL функции TRIM() и TO_CYRILLIC()
     * @param buildingNumber
     * @param buildingCorp
     * @param streetId
     * @param cityId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @Transactional
    public Long findInternalBuilding(String buildingNumber, String buildingCorp, Long streetId, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", buildingNumber == null ? "" : prepareBuildingData(buildingNumber));
        if (!Strings.isEmpty(buildingCorp)) {
            params.put("corp", prepareBuildingData(buildingCorp));
        }
        long parentId = streetId != null ? streetId : cityId;
        long parentEntityId = streetId != null ? 300 : 400;
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalBuilding", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Удалить ВСЕ(не только начальные и конечные) пробелы в номере и корпусе дома
     * @param data номер либо корпус дома
     * @return
     */
    private String prepareBuildingData(String data) {
        char[] chars = data.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            if (c != ' ') {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Transactional
    public void updateBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }
        sqlSession().update(ADDRESS_BEAN_MAPPING_NAMESPACE + ".updateBuilding", correction);
    }

    @Transactional
    public Correction getCityCorrection(Long id) {
        Correction correction = (Correction) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectCityCorrection", id);

        if (correction != null) {
            correction.setEntity("city");
        }

        return correction;
    }

    @Transactional
    public List<Correction> getCityCorrections(CorrectionExample example) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectCityCorrections", example);
    }

    @Transactional
    public StreetCorrection getStreetCorrection(Long id) {
        StreetCorrection streetCorrection = (StreetCorrection) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectStreetCorrection", id);

        if (streetCorrection != null) {
            streetCorrection.setEntity("street");
        }
        return streetCorrection;
    }

    @Transactional
    public List<StreetCorrection> getStreetCorrections(CorrectionExample example) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectStreetCorrections", example);
    }

    @Transactional
    public List<Correction> getStreetTypeCorrections(long organizationId) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectStreetTypeCorrections", organizationId);
    }

    @Transactional
    public Correction getDistrictCorrection(Long id) {
        Correction correction = (Correction) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectDistrictCorrection", id);

        if (correction != null) {
            correction.setEntity("district");
        }

        return correction;
    }

    @Transactional
    public BuildingCorrection getBuildingCorrection(Long id) {
        BuildingCorrection correction = (BuildingCorrection) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".selectBuildingCorrection", id);

        if (correction != null) {
            correction.setEntity("building");
        }

        return correction;
    }

    @Transactional
    public List<BuildingCorrection> findBuildings(CorrectionExample example) {
        List<BuildingCorrection> list = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findBuildings", example);

        Strategy cityStrategy = strategyFactory.getStrategy("city");
        Strategy streetStrategy = strategyFactory.getStrategy("street");
        Strategy buildingStrategy = strategyFactory.getStrategy("building");

        for (Correction c : list) {
            try {
                DomainObject building = buildingStrategy.findById(c.getObjectId());
                SearchComponentState state = buildingStrategy.getSearchComponentStateForParent(building.getParentId(), "building_address", null);
                DomainObject street = state.get("street");
                DomainObject city = state.get("city");
                Locale locale = localeBean.convert(localeBean.getLocale(example.getLocaleId()));
                String displayBuilding = buildingStrategy.displayDomainObject(building, locale);
                String displayStreet = streetStrategy.displayDomainObject(street, locale);
                String displayCity = cityStrategy.displayDomainObject(city, locale);

                c.setDisplayObject(displayCity + ", " + displayStreet + ", " + displayBuilding);
            } catch (Exception e) {
                log.warn("[Полный адрес не найден]", e);
                c.setDisplayObject("[Полный адрес не найден]");
            }
        }

        return list;
    }

    @Transactional
    public int countBuildings(CorrectionExample example) {
        return (Integer) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".countBuildings", example);
    }

    @Transactional
    public List<StreetCorrection> findStreets(CorrectionExample example) {
        example.setParentEntity("city");
        List<StreetCorrection> streets = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreets", example);

        Strategy streetStrategy = strategyFactory.getStrategy("street");
        Strategy cityStrategy = strategyFactory.getStrategy("city");
        for (Correction c : streets) {
            try {
                DomainObject street = streetStrategy.findById(c.getObjectId());
                DomainObject city = cityStrategy.findById(street.getParentId());
                Locale locale = localeBean.convert(localeBean.getLocale(example.getLocaleId()));
                String displayCity = cityStrategy.displayDomainObject(city, locale);
                String displayStreet = streetStrategy.displayDomainObject(street, locale);
                c.setDisplayObject(displayCity + ", " + displayStreet);
            } catch (Exception e) {
                log.warn("[Полный адрес не найден]", e);
                c.setDisplayObject("[Полный адрес не найден]");
            }
        }
        return streets;
    }

    @Transactional
    public List<Correction> findDistricts(CorrectionExample example) {
        example.setParentEntity("city");
        List<Correction> districts = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findDistricts", example);
        Strategy districtStrategy = strategyFactory.getStrategy("district");
        Strategy cityStrategy = strategyFactory.getStrategy("city");

        for (Correction c : districts) {
            try {
                DomainObject district = districtStrategy.findById(c.getObjectId());
                DomainObject city = cityStrategy.findById(district.getParentId());
                Locale locale = localeBean.convert(localeBean.getLocale(example.getLocaleId()));
                String displayCity = cityStrategy.displayDomainObject(city, locale);
                String displayDistrict = districtStrategy.displayDomainObject(district, locale);
                c.setDisplayObject(displayCity + ", " + displayDistrict);
            } catch (Exception e) {
                log.warn("[Полный адрес не найден]", e);
                c.setDisplayObject("[Полный адрес не найден]");
            }
        }
        return districts;
    }

    @Transactional
    public boolean checkBuildingExistence(BuildingCorrection buildingCorrection) {
        return (Integer) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".checkBuildingExistence", buildingCorrection) > 0;
    }

    @Transactional
    public boolean checkStreetExistence(StreetCorrection streetCorrection) {
        return (Integer) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".checkStreetExistence", streetCorrection) > 0;
    }

    @Transactional
    public void insertStreet(StreetCorrection streetCorrection) {
        sqlSession().insert(ADDRESS_BEAN_MAPPING_NAMESPACE + ".insertStreet", streetCorrection);
    }

    @Transactional
    public void updateStreet(StreetCorrection streetCorrection) {
        sqlSession().update(ADDRESS_BEAN_MAPPING_NAMESPACE + ".updateStreet", streetCorrection);
    }
}
