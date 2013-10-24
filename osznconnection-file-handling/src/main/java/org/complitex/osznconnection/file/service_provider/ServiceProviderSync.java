package org.complitex.osznconnection.file.service_provider;

import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.service_provider.entity.BuildingSync;
import org.complitex.osznconnection.file.service_provider.entity.DistrictSync;
import org.complitex.osznconnection.file.service_provider.entity.StreetSync;
import org.complitex.osznconnection.file.service_provider.entity.StreetTypeSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:43
 */
@Stateless
public class ServiceProviderSync extends AbstractBean{
    private final static String NS = ServiceProviderSync.class.getName();
    private final static Logger log = LoggerFactory.getLogger(ServiceProviderSync.class);

    /**
     * function z$runtime_sz_utl.getDistricts(
     *     pCityName varchar2,    -- Название нас.пункта
     *     pCityType varchar2,    -- Тип нас.пункта (краткое название)
     *     pDate date,            -- Дата актуальности
     *     Cur out TCursor
     * ) return integer;
     * поля курсора:
     * DistrName - varchar2,          -- Название района
     * DistrID - varchar2,            -- Код района (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - неизвестный тип нас.пункта, -2 - неизвестный нас.пункт
     */
    @SuppressWarnings("unchecked")
    public List<DistrictSync> getDistrictSyncs(CalculationContext calculationContext, String cityName,
                                               String cityTypeName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("cityName", cityName);
        param.put("cityTypeName", cityTypeName);
        param.put("date", date);

        try {
            sqlSession(calculationContext.getDataSource()).selectOne(NS + ".selectDistrictSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка районов", e);
        }

        return (List<DistrictSync>) param.get("out");
    }

    /**
     * function z$runtime_sz_utl.getStreetTypes(
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * StrTypeName - varchar2,          -- Полное название типа улицы
     * ShStrTypeName - varchar2,       -- Краткое название типа улицы
     * StreetTypeID - varchar2,          -- Код типа улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - ошибка
     */
    @SuppressWarnings("unchecked")
    public List<StreetTypeSync> getStreetTypeSyncs(CalculationContext calculationContext){
        Map<String, Object> param = new HashMap<>();

        try {
            sqlSession(calculationContext.getDataSource()).selectOne(NS + ".selectStreetTypeSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка типов улиц", e);
        }

        return (List<StreetTypeSync>) param.get("out");
    }

    /**
     * function z$runtime_sz_utl.getStreets(
     * pCityName varchar2,    -- Название нас.пункта
     * pCityType varchar2,     -- Название типа нас.пункта
     * pDate date,                -- Дата актуальности
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * StreetName - varchar2,          -- Название улицы
     * StreetType - varchar2,          -- Тип улицы (краткое название)
     * StreetID - varchar2,              -- Код улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - неизвестный тип нас.пункта, -2 - неизвестный нас.пункт
     */
    @SuppressWarnings("unchecked")
    public List<StreetSync> getStreetSyncs(CalculationContext calculationContext, String cityName,
                                           String cityTypeName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("cityName", cityName);
        param.put("cityTypeName", cityTypeName);
        param.put("date", date);

        try {
            sqlSession(calculationContext.getDataSource()).selectOne(NS + ".selectStreetSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка улиц", e);
        }

        return (List<StreetSync>) param.get("out");
    }

    /**
     * function z$runtime_sz_utl.getBuildings(
     * DistrName varchar2,        -- Название района нас.пункта
     * pStreetName varchar2,    -- Название улицы
     * pStreetType varchar2,     -- Тип улицы (краткое название)
     * pDate date,                   -- Дата актуальности
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * BldNum - varchar2,          -- Номер дома
     * BldPart - varchar2,          -- Номер корпуса
     * BldID - varchar2,             -- Код дома (ID)
     * StreetID - varchar2,         -- Код улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -3 - неизвестный район нас.пункта, -4 - неизвестный тип улицы, -5 - неизвестная улица
     */
    @SuppressWarnings("unchecked")
    public List<BuildingSync> getBuildingSyncs(CalculationContext calculationContext, String districtName,
                                               String streetTypeName, String streetName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("districtName", districtName);
        param.put("streetName", streetName);
        param.put("streetTypeName", streetTypeName);
        param.put("date", date);

        try {
            sqlSession(calculationContext.getDataSource()).selectOne(NS + ".selectBuildingSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка домов", e);
        }

        return (List<BuildingSync>) param.get("out");
    }

}
