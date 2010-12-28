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
    private List<Correction> findAddressLocalCorrections(String entityTable, Long parentId, String correction, long organizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entityTable", entityTable);
        params.put("parentId", parentId);
        params.put("correction", correction);
        params.put("organizationId", organizationId);

        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findAddressLocalCorrections", params);
        return corrections;
    }

    /**
     * Найти id локального города в таблице коррекций.
     * @param city
     * @param organizationId
     * @return
     */
    public List<Correction> findCityLocalCorrections(String city, long organizationId) {
        return findAddressLocalCorrections("city", null, city, organizationId);
    }

    /**
     * Найти id локальной улицы в таблице коррекций.
     * @param parent
     * @param street
     * @param parent
     * @return
     */
    public List<StreetCorrection> findStreetLocalCorrections(Correction parent, Long streetTypeCorrectionId, String street) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parent.getId());
        params.put("correction", street);
        params.put("streetTypeCorrectionId", streetTypeCorrectionId);
        params.put("organizationId", parent.getOrganizationId());
        List<StreetCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreetLocalCorrections", params);
        return corrections;
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
    public List<BuildingCorrection> findBuildingLocalCorrections(Correction parent, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parent.getId());
        params.put("correction", buildingNumber);
        params.put("correctionCorp", buildingCorp);
        params.put("organizationId", parent.getOrganizationId());

        List<BuildingCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findBuildingLocalCorrections", params);
        return corrections;
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
    private List<Correction> findAddressRemoteCorrections(String entityTable, long organizationId, long internalObjectId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setEntity(entityTable);
        example.setObjectId(internalObjectId);
        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findAddressRemoteCorrections", example);
        return corrections;
    }

    /**
     * Найти данные о коррекции для города.
     * @param organizationId
     * @param internalCityId
     * @return
     */
    public List<Correction> findCityRemoteCorrections(long organizationId, long internalCityId) {
        return findAddressRemoteCorrections("city", organizationId, internalCityId);
    }

    /**
     * Найти данные о коррекции для улицы.
     * @param organizationId
     * @param internalStreetId
     * @return
     */
    public List<StreetCorrection> findStreetRemoteCorrections(long organizationId, long internalStreetId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalStreetId);
        List<StreetCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreetRemoteCorrections", example);
        return corrections;
    }

    /**
     * Найти данные о коррекции для дома.
     * @param organizationId
     * @param internalBuildingId
     * @return
     */
    @Transactional
    public List<BuildingCorrection> findBuildingRemoteCorrections(long organizationId, long internalBuildingId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalBuildingId);
        List<BuildingCorrection> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findBuildingRemoteCorrections", example);
        return corrections;
    }

    /**
     * Найти данные о коррекции для района.
     * @param calculationCenterId
     * @param osznId
     * @return
     */
    @Transactional
    public List<Correction> findDistrictRemoteCorrections(long calculationCenterId, long osznId) {
        Map<String, Long> params = ImmutableMap.of("calculationCenterId", calculationCenterId, "osznId", osznId);
        List<Correction> corrections = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findDistrictRemoteCorrections", params);
        return corrections;
    }

    /**
     * Найти данные о коррекции для типа улицы.
     * @param calculationCenterId
     * @param internalStreetTypeId
     * @return
     */
    public List<Correction> findStreetTypeRemoteCorrections(long calculationCenterId, long internalStreetTypeId) {
        return findAddressRemoteCorrections("street_type", calculationCenterId, internalStreetTypeId);
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
    private List<Long> findInternalObjectIds(String entity, String correction, long attributeTypeId, Long parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("correction", correction != null ? correction.trim() : correction);
        params.put("attributeTypeId", attributeTypeId);
        params.put("parentId", parentId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalObjectIds", params);
        return ids;
    }

    /**
     * Найти город в локальной адресной базе.
     * См. findInternalObjectId()
     * 
     * @param city
     * @return
     */
    @Transactional
    public List<Long> findInternalCityIds(String city) {
        return findInternalObjectIds("city", city, 400, null);
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
    public List<Long> findInternalStreetIds(Long streetTypeId, String street, long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("correction", street);
        params.put("parentId", cityId);
        params.put("streetTypeId", streetTypeId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalStreetIds", params);
        return ids;
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
    @Transactional
    public List<Long> findInternalBuildingIds(String buildingNumber, String buildingCorp, Long streetId, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", buildingNumber == null ? "" : prepareBuildingData(buildingNumber));
        if (!Strings.isEmpty(buildingCorp)) {
            buildingCorp = prepareBuildingData(buildingCorp);
        }
        params.put("corp", Strings.isEmpty(buildingCorp) ? null : buildingCorp);
        long parentId = streetId != null ? streetId : cityId;
        long parentEntityId = streetId != null ? 300 : 400;
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);
        List<Long> ids = sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findInternalBuildingIds", params);
        return ids;
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
    public Correction findCityCorrectionById(Long id) {
        Correction correction = (Correction) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findCityCorrectionById", id);

        if (correction != null) {
            correction.setEntity("city");
        }

        return correction;
    }

    @Transactional
    public List<Correction> findCityCorrections(CorrectionExample example) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findCityCorrections", example);
    }

    @Transactional
    public StreetCorrection findStreetCorrectionById(Long id) {
        StreetCorrection streetCorrection = (StreetCorrection) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreetCorrectionById", id);

        if (streetCorrection != null) {
            streetCorrection.setEntity("street");
        }
        return streetCorrection;
    }

    @Transactional
    public List<StreetCorrection> findStreetCorrections(CorrectionExample example) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreetCorrections", example);
    }

    @Transactional
    public List<Correction> findStreetTypeCorrections(long organizationId) {
        return sqlSession().selectList(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findStreetTypeCorrections", organizationId);
    }

    @Transactional
    public Correction findDistrictCorrectionById(Long id) {
        Correction correction = (Correction) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findDistrictCorrectionById", id);

        if (correction != null) {
            correction.setEntity("district");
        }

        return correction;
    }

    @Transactional
    public BuildingCorrection findBuildingCorrectionById(Long id) {
        BuildingCorrection correction = (BuildingCorrection) sqlSession().selectOne(ADDRESS_BEAN_MAPPING_NAMESPACE + ".findBuildingCorrectionById", id);

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
