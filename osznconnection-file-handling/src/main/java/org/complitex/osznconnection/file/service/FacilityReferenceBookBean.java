package org.complitex.osznconnection.file.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.CityCorrection;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.entity.StreetTypeCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.process.FacilityStreetLoadTaskBean;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Locale;

import static com.google.common.collect.ImmutableMap.of;

/**
 *
 * @author Artem
 */
@Stateless
public class FacilityReferenceBookBean extends AbstractBean {

    private static final String RESOURCE_BUNDLE = FacilityReferenceBookBean.class.getName();
    private static final String NS = FacilityReferenceBookBean.class.getName();
    private static final Logger log = LoggerFactory.getLogger(FacilityReferenceBookBean.class);
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private ConfigBean configBean;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private LogBean logBean;

    @EJB
    private LocaleBean localeBean;

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> requests) {
        if (requests != null && !requests.isEmpty()) {
            final RequestFileType requestFileType = requests.get(0).getRequestFileType();
            final String table = getTableName(requestFileType);

            for (AbstractRequest request : requests) {
                sqlSession().insert(NS + ".insertFacilityReferences", of("table", table, "request", request));
            }
        }
    }

    public String getTableName(RequestFileType requestFileType) {
        switch (requestFileType) {
            case FACILITY_STREET_TYPE:
                return "facility_street_type_reference";
            case FACILITY_STREET:
                return "facility_street_reference";
            case FACILITY_TARIF:
                return "facility_tarif_reference";
            default:
                throw new IllegalStateException("Illegal request file type: " + requestFileType);
        }
    }

    @Transactional
    public void delete(long requestFileId, RequestFileType requestFileType) {
        sqlSession().delete(NS + ".deleteFacilityReferences", of("requestFileId", requestFileId, "table",
                getTableName(requestFileType)));
    }

    //FacilityStreetType

    public List<FacilityStreetType> getFacilityStreetTypes(FilterWrapper<FacilityStreetType> filterWrapper){
        return sqlSession().selectList(NS + ".selectFacilityStreetTypes", filterWrapper);
    }

    public Integer getFacilityStreetTypesCount(FilterWrapper<FacilityStreetType> filterWrapper){
        return sqlSession().selectOne(NS + ".selectFacilityStreetTypesCount", filterWrapper);
    }

    //FacilityStreet

    public List<FacilityStreet> getFacilityStreets(FilterWrapper<FacilityStreet> filterWrapper){
        return sqlSession().selectList(NS + ".selectFacilityStreets", filterWrapper);
    }

    public Integer getFacilityStreetsCount(FilterWrapper<FacilityStreet> filterWrapper){
        return sqlSession().selectOne(NS + ".selectFacilityStreetsCount", filterWrapper);
    }

    public FacilityStreet getFacilityStreet(String streetCode, Long osznId, Long userOrganizationId){
        return sqlSession().selectOne(NS + ".selectFacilityStreetByCode", of("streetCode", streetCode, "osznId", osznId,
                "userOrganizationId", userOrganizationId));
    }

    public FacilityStreet getFacilityStreet(Long requestFileId, String streetCode){
        return sqlSession().selectOne(NS + ".selectFacilityStreetByRequestFile", of("requestFileId", requestFileId,
                "streetCode", streetCode));
    }

    //FacilityTarif

    public List<FacilityTarif> getFacilityTarifs(FilterWrapper<FacilityTarif> filterWrapper){
        return sqlSession().selectList(NS + ".selectFacilityTarifs", filterWrapper);
    }

    public Integer getFacilityTarifsCount(FilterWrapper<FacilityTarif> filterWrapper){
        return sqlSession().selectOne(NS + ".selectFacilityTarifsCount", filterWrapper);
    }

    private List<String> findStreetTypeNames(String streetTypeCode, long osznId, long userOrganizationId) {
        return sqlSession().selectList(NS + ".findStreetTypeNames", of("streetTypeCode", streetTypeCode, "osznId", osznId,
                        "userOrganizationId", userOrganizationId));
    }

    private String printStringValue(String value, Locale locale) {
        return Strings.isEmpty(value) ? ResourceUtil.getString(RESOURCE_BUNDLE, "empty_string_value", locale) : value;
    }

    public void updateStreetCorrections(String streetCode, Long userOrganizationId, Long osznId)
            throws ExecuteException {
        FacilityStreet facilityStreet = getFacilityStreet(streetCode, osznId, userOrganizationId);

        if (facilityStreet != null) {
            updateStreetCorrections(facilityStreet, userOrganizationId, osznId, "");
        }
    }

    public void updateStreetCorrections(FacilityStreet street, Long userOrganizationId, Long osznId,
            final String streetTypeReferenceFileName) throws ExecuteException {
        Locale locale = localeBean.getSystemLocale(); //todo locale?

        String streetName = street.getStringField(FacilityStreetDBF.KL_NAME);
        String streetCode = street.getStringField(FacilityStreetDBF.KL_CODEUL);
        String streetTypeCode = street.getStringField(FacilityStreetDBF.KL_CODEKUL);

        final String defaultCity = configBean.getString(FileHandlingConfig.DEFAULT_REQUEST_FILE_CITY, true);

        Long cityId;
        Correction cityCorrection;

        List<CityCorrection> cityCorrections = addressCorrectionBean.getCityCorrections(
                null, defaultCity, osznId, userOrganizationId);

        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else {
            final String errorKey = cityCorrections.size() > 1 ? "city_corrections.too_many" : "city_corrections.not_found";
            throw new ExecuteException(
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, errorKey, locale, printStringValue(defaultCity, locale)));
        }

        String streetTypeName;
        List<String> streetTypeNames = findStreetTypeNames(streetTypeCode, osznId, userOrganizationId);

        if (streetTypeNames.size() == 1) {
            streetTypeName = streetTypeNames.get(0);
        } else {
            final String errorKey = streetTypeNames.size() > 1 ? "facility_street_type.too_many"
                    : "facility_street_type.not_found";
            throw new ExecuteException(
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, errorKey, locale, printStringValue(streetCode, locale),
                    streetTypeReferenceFileName));
        }

        Long streetTypeId;
        StreetTypeCorrection streetTypeCorrection;
        List<StreetTypeCorrection> streetTypeCorrections =
                addressCorrectionBean.getStreetTypeCorrections(null, streetTypeName, osznId, userOrganizationId);

        if (streetTypeCorrections.size() == 1) {
            streetTypeCorrection = streetTypeCorrections.get(0);
            streetTypeId = streetTypeCorrection.getObjectId();
        } else if (streetTypeCorrections.size() > 1) {
            throw new ExecuteException(
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_street_type_corrections.too_many",
                    locale, printStringValue(streetTypeName, locale)));
        } else {
            // искать по внутренней базе типов улиц
            List<Long> streetTypeIds = addressCorrectionBean.getStreetTypeObjectIds(streetTypeName);
            if (streetTypeIds.size() == 1) {
                streetTypeId = streetTypeIds.get(0);
                streetTypeCorrection = new StreetTypeCorrection(streetTypeCode, streetTypeId, streetTypeName.toUpperCase(),
                         osznId, userOrganizationId, OsznOrganizationStrategy.MODULE_ID);

                addressCorrectionBean.save(streetTypeCorrection);
            } else if (streetTypeIds.size() > 1) {
                throw new ExecuteException(
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_internal_street_type.too_many",
                                locale, printStringValue(streetTypeName, locale)));
            } else {
                logBean.error(Module.NAME, FacilityStreetLoadTaskBean.class, FacilityStreet.class, null, street.getId(),
                        Log.EVENT.CREATE, null,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_internal_street_type.not_found",
                        locale, printStringValue(streetTypeName, locale)));
                log.error("No one internal street type was found in local base. Street type name: '{}', oszn id: {}, "
                        + "user organization id: {}", new Object[]{streetTypeName, osznId, userOrganizationId});
                return;
            }
        }

        List<StreetCorrection> streetCorrections =
                addressCorrectionBean.getStreetCorrections(cityCorrection.getObjectId(), streetTypeCorrection.getObjectId(),
                        null, null, streetName, osznId, userOrganizationId);

        if (streetCorrections.size() == 1) {
            StreetCorrection streetCorrection = streetCorrections.get(0);
            if (!Strings.isEqual(streetCode, streetCorrection.getExternalId())) {
                // коды не совпадают, нужно обновить код соответствия.
                streetCorrection.setExternalId(streetCode);

                addressCorrectionBean.save(streetCorrection);
            }
        } else if (streetCorrections.size() > 1) {
            throw new ExecuteException(
                    ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_street_corrections.too_many",
                            locale, printStringValue(cityCorrection.getCorrection(), locale), cityCorrection.getId(),
                            printStringValue(streetTypeCorrection.getCorrection(), locale), streetTypeCorrection.getId(),
                            printStringValue(streetName, locale)));
        } else {
            // искать по внутренней базе улиц
            List<Long> streetIds = streetStrategy.getStreetObjectIds(cityId, streetTypeId, streetName);

            if (streetIds.size() == 1) {
                long streetId = streetIds.get(0); //todo locate and update from address base

                StreetCorrection streetCorrection =  new StreetCorrection(cityId, streetTypeId,
                        streetCode.toUpperCase(), streetId, streetName.toUpperCase(),
                        osznId, userOrganizationId, OsznOrganizationStrategy.MODULE_ID);

                addressCorrectionBean.save(streetCorrection);
            } else {
                final DomainObject internalCity = cityStrategy.findById(cityId, true);
                final String internalCityName = cityStrategy.displayDomainObject(internalCity, locale);
                final DomainObject internalStreetType = streetTypeStrategy.findById(streetTypeId, true);
                final String internalStreetTypeName = streetTypeStrategy.displayDomainObject(internalStreetType, locale);

                if (streetIds.size() > 1) {
                    throw new ExecuteException(
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_internal_street.too_many",
                                    locale, internalCityName, cityId, internalStreetTypeName, streetTypeId,
                                    printStringValue(streetName, locale)));
                } else {
                    logBean.error(Module.NAME, FacilityStreetLoadTaskBean.class, FacilityStreet.class, null, street.getId(),
                            Log.EVENT.CREATE, null,
                            ResourceUtil.getFormatString(RESOURCE_BUNDLE, "facility_internal_street.not_found",
                            locale, internalCityName, cityId, internalStreetTypeName, streetTypeId,
                            printStringValue(streetName, locale)));
                    log.error("No one internal street was found in local base. Internal city id: {}, "
                            + "internal street type id: {}, street name: '{}', oszn id: {}, user organization id: {}",
                            new Object[]{cityId, streetTypeId, streetName, osznId, userOrganizationId});
                }
            }
        }
    }
}
