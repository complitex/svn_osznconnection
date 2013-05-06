/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel.CORRECTED_ENTITY;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Set;

/**
 * Класс разрешает адрес.
 * @author Artem
 */
@Stateless(name = "AddressService")
public class AddressService extends AbstractBean {
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;

    @EJB
    private StrategyFactory strategyFactory;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private ServiceProviderAdapter adapter;

    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    private void resolveLocalAddress(ActualPayment actualPayment, long userOrganizationId) {
        //осзн id
        long osznId = actualPayment.getOrganizationId();

        //Связывание города
        String city = actualPayment.getStringField(ActualPaymentDBF.N_NAME);
        Long cityId = null;
        Correction cityCorrection = null;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else if (cityCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);
            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.findInternalCityIds(city);
            if (cityIds.size() == 1) {
                cityId = cityIds.get(0);
                cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(cityCorrection);
            } else if (cityIds.size() > 1) {
                actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);
                return;
            } else {
                actualPayment.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (cityId != null) {
            actualPayment.setInternalCityId(cityId);
        }

        //связывание типа улицы
        String streetType = actualPayment.getStringField(ActualPaymentDBF.VUL_CAT);
        Long streetTypeId = null;
        Correction streetTypeCorrection = null;
        List<Correction> streetTypeCorrections =
                addressCorrectionBean.findStreetTypeLocalCorrections(streetType, osznId, userOrganizationId);
        if (streetTypeCorrections.size() == 1) {
            streetTypeCorrection = streetTypeCorrections.get(0);
            streetTypeId = streetTypeCorrection.getObjectId();
        } else if (streetTypeCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION);
            return;
        } else {
            List<Long> streetTypeIds = addressCorrectionBean.findInternalStreetTypeIds(streetType);
            if (streetTypeIds.size() == 1) {
                streetTypeId = streetTypeIds.get(0);
                streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                        streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(streetTypeCorrection);
            } else if (streetTypeIds.size() > 1) {
                actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE);
                return;
            } else {
                actualPayment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (streetTypeId != null) {
            actualPayment.setInternalStreetTypeId(streetTypeId);
        }

        //Связывание улицы
        String street = actualPayment.getStringField(ActualPaymentDBF.VUL_NAME);
        String streetCode = actualPayment.getStringField(ActualPaymentDBF.VUL_CODE);
        Long streetId = null;
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(
                cityCorrection.getId(), streetTypeCorrection.getId(), street, osznId, userOrganizationId);
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
            streetId = streetCorrection.getObjectId();
        } else if (streetCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
            return;
        } else {
            List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(streetTypeId, street, cityId);
            if (streetIds.size() == 1) {
                streetId = streetIds.get(0);
                streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), streetCode.toUpperCase(),
                        streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insertStreet(streetCorrection);
            } else if (streetIds.size() > 1) {
                actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET);
                return;
            } else {
                actualPayment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (streetId != null) {
            DomainObject streetObject = strategyFactory.getStrategy("street").findById(streetId, true);
            actualPayment.setInternalStreetId(streetId);
            actualPayment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            actualPayment.setInternalCityId(streetObject.getParentId());
        }

        //Связывание дома
        String buildingNumber = actualPayment.getStringField(ActualPaymentDBF.BLD_NUM);
        String buildingCorp = actualPayment.getStringField(ActualPaymentDBF.CORP_NUM);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
        if (buildingCorrections.size() == 1) {
            buildingCorrection = buildingCorrections.get(0);
            buildingId = buildingCorrection.getObjectId();
        } else if (buildingCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
            return;
        } else {
            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingIds.size() == 1) {
                buildingId = buildingIds.get(0);
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber.toUpperCase(), buildingCorp.toUpperCase(),
                        streetCorrection.getId(), buildingId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID,
                        userOrganizationId);
                addressCorrectionBean.insertBuilding(buildingCorrection);
            } else if (buildingIds.size() > 1) {
                actualPayment.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
                return;
            } else {
                actualPayment.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (buildingId != null) {
            actualPayment.setInternalBuildingId(buildingId);
            IStrategy buildingStrategy = strategyFactory.getStrategy("building");
            Building building = (Building) buildingStrategy.findById(buildingId, true);
            Long internalStreetId = building.getPrimaryStreetId();
            if (streetId != null && !streetId.equals(internalStreetId)) {
                actualPayment.setInternalStreetId(internalStreetId);
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                actualPayment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                Long internalCityId = streetObject.getParentId();
                actualPayment.setInternalCityId(internalCityId);
            }
            actualPayment.setStatus(RequestStatus.CITY_UNRESOLVED);
        }
    }

    private void resolveLocalAddress(Subsidy subsidy, long userOrganizationId) {
        //осзн id
        long osznId = subsidy.getOrganizationId();

        //Связывание города
        String city = subsidy.getStringField(SubsidyDBF.NP_NAME);
        Long cityId = null;
        Correction cityCorrection = null;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else if (cityCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);
            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.findInternalCityIds(city);
            if (cityIds.size() == 1) {
                cityId = cityIds.get(0);
                cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(cityCorrection);
            } else if (cityIds.size() > 1) {
                subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);
                return;
            } else {
                subsidy.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (cityId != null) {
            subsidy.setInternalCityId(cityId);
        }

        //связывание типа улицы
        String streetType = subsidy.getStringField(SubsidyDBF.CAT_V);
        Long streetTypeId = null;
        Correction streetTypeCorrection = null;
        List<Correction> streetTypeCorrections =
                addressCorrectionBean.findStreetTypeLocalCorrections(streetType, osznId, userOrganizationId);
        if (streetTypeCorrections.size() == 1) {
            streetTypeCorrection = streetTypeCorrections.get(0);
            streetTypeId = streetTypeCorrection.getObjectId();
        } else if (streetTypeCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION);
            return;
        } else {
            List<Long> streetTypeIds = addressCorrectionBean.findInternalStreetTypeIds(streetType);
            if (streetTypeIds.size() == 1) {
                streetTypeId = streetTypeIds.get(0);
                streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                        streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(streetTypeCorrection);
            } else if (streetTypeIds.size() > 1) {
                subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE);
                return;
            } else {
                subsidy.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (streetTypeId != null) {
            subsidy.setInternalStreetTypeId(streetTypeId);
        }

        //Связывание улицы
        String street = subsidy.getStringField(SubsidyDBF.NAME_V);
        String streetCode = subsidy.getStringField(SubsidyDBF.VULCOD);
        Long streetId = null;
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(
                cityCorrection.getId(), streetTypeCorrection.getId(), street, osznId, userOrganizationId);
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
            streetId = streetCorrection.getObjectId();
        } else if (streetCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
            return;
        } else {
            List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(streetTypeId, street, cityId);
            if (streetIds.size() == 1) {
                streetId = streetIds.get(0);
                streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), streetCode.toUpperCase(),
                        streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insertStreet(streetCorrection);
            } else if (streetIds.size() > 1) {
                subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET);
                return;
            } else {
                subsidy.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (streetId != null) {
            DomainObject streetObject = strategyFactory.getStrategy("street").findById(streetId, true);
            subsidy.setInternalStreetId(streetId);
            subsidy.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            subsidy.setInternalCityId(streetObject.getParentId());
        }

        //Связывание дома
        String buildingNumber = subsidy.getStringField(SubsidyDBF.BLD);
        String buildingCorp = subsidy.getStringField(SubsidyDBF.CORP);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
        if (buildingCorrections.size() == 1) {
            buildingCorrection = buildingCorrections.get(0);
            buildingId = buildingCorrection.getObjectId();
        } else if (buildingCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
            return;
        } else {
            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingIds.size() == 1) {
                buildingId = buildingIds.get(0);
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber.toUpperCase(),
                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                        streetCorrection.getId(), buildingId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID,
                        userOrganizationId);
                addressCorrectionBean.insertBuilding(buildingCorrection);
            } else if (buildingIds.size() > 1) {
                subsidy.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
                return;
            } else {
                subsidy.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (buildingId != null) {
            subsidy.setInternalBuildingId(buildingId);
            IStrategy buildingStrategy = strategyFactory.getStrategy("building");
            Building building = (Building) buildingStrategy.findById(buildingId, true);
            Long internalStreetId = building.getPrimaryStreetId();
            if (streetId != null && !streetId.equals(internalStreetId)) {
                subsidy.setInternalStreetId(internalStreetId);
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                subsidy.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                Long internalCityId = streetObject.getParentId();
                subsidy.setInternalCityId(internalCityId);
            }
            subsidy.setStatus(RequestStatus.CITY_UNRESOLVED);
        }
    }

    public void resolveLocalStreet(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId) {
        //осзн id
        long osznId = dwellingCharacteristics.getOrganizationId();

        //Связывание города
        String city = dwellingCharacteristics.getCity();
        Long cityId;
        Correction cityCorrection;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else if (cityCorrections.size() > 1) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);
            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.findInternalCityIds(city);
            if (cityIds.size() == 1) {
                cityId = cityIds.get(0);
                cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(cityCorrection);
            } else if (cityIds.size() > 1) {
                dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);
                return;
            } else {
                dwellingCharacteristics.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (cityId != null) {
            dwellingCharacteristics.setInternalCityId(cityId);
        }

        //связывание улицы
        String streetCode = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.CDUL);
        StreetCorrection streetCorrection;

        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByCode(
                cityCorrection.getId(), streetCode, osznId, userOrganizationId);

        if (streetCorrections.isEmpty()){
            try {
                facilityReferenceBookBean.updateStreetCorrections(streetCode, userOrganizationId, osznId);

                streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByCode(
                        cityCorrection.getId(), streetCode, osznId, userOrganizationId);
            } catch (ExecuteException e) {
                log.error("Ошибка создания коррекции", e);
            }
        }

        if (streetCorrections.size() >= 1) {
            //сформируем множество ids улиц
            Set<Long> streetIds = Sets.newHashSet();
            for (StreetCorrection streetCorr : streetCorrections) {
                streetIds.add(streetCorr.getObjectId());
            }

            if (streetIds.size() == 1) { //все улицы имеют ссылаются на одну внутреннюю улицу - берём любую (первую) коррекцию.
                streetCorrection = streetCorrections.get(0);
            } else {
                //улицы ссылаются на разные внутренние улицы. Этого не должно быть, т.к. пара (название улицы, код улицы) уникальна на 
                //множестве соответствий для одной организации(осзн).
                dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
                return;
            }
        } else {
            dwellingCharacteristics.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }

        if (streetCorrection != null) {
            dwellingCharacteristics.setStreetCorrectionId(streetCorrection.getId());
            dwellingCharacteristics.setInternalStreetId(streetCorrection.getObjectId());
            dwellingCharacteristics.setStreet(streetCorrection.getCorrection());
            DomainObject streetObject = streetStrategy.findById(streetCorrection.getObjectId(), true);
            dwellingCharacteristics.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            dwellingCharacteristics.setStreetType(streetCorrection.getStreetTypeCorrection().getCorrection());
            dwellingCharacteristics.setInternalCityId(streetObject.getParentId());
        }
    }

    private void resolveLocalAddress(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId) {
        //осзн id
        long osznId = dwellingCharacteristics.getOrganizationId();

        //Связывание дома
        String buildingNumber = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE);
        String buildingCorp = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                dwellingCharacteristics.getStreetCorrectionId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
        if (buildingCorrections.size() == 1) {
            buildingCorrection = buildingCorrections.get(0);
            buildingId = buildingCorrection.getObjectId();
        } else if (buildingCorrections.size() > 1) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
            return;
        } else {
            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp,
                    dwellingCharacteristics.getInternalStreetId(), dwellingCharacteristics.getInternalCityId());
            if (buildingIds.size() == 1) {
                buildingId = buildingIds.get(0);
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber.toUpperCase(),
                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                        dwellingCharacteristics.getStreetCorrectionId(), buildingId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insertBuilding(buildingCorrection);
            } else if (buildingIds.size() > 1) {
                dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
                return;
            } else {
                dwellingCharacteristics.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (buildingId != null) {
            dwellingCharacteristics.setInternalBuildingId(buildingId);
            IStrategy buildingStrategy = strategyFactory.getStrategy("building");
            Building building = (Building) buildingStrategy.findById(buildingId, true);
            Long internalStreetId = building.getPrimaryStreetId();
            if (dwellingCharacteristics.getInternalStreetId() != null
                    && !dwellingCharacteristics.getInternalStreetId().equals(internalStreetId)) {
                dwellingCharacteristics.setInternalStreetId(internalStreetId);
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                dwellingCharacteristics.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                dwellingCharacteristics.setInternalCityId(streetObject.getParentId());
            }
            dwellingCharacteristics.setStatus(RequestStatus.CITY_UNRESOLVED);
        }
    }

    public void resolveLocalStreet(FacilityServiceType facilityServiceType, long userOrganizationId) {
        //осзн id
        long osznId = facilityServiceType.getOrganizationId();

        //Связывание города
        String city = facilityServiceType.getCity();

        Long cityId;
        Correction cityCorrection;

        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else if (cityCorrections.size() > 1) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);
            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.findInternalCityIds(city);
            if (cityIds.size() == 1) {
                cityId = cityIds.get(0);
                cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(cityCorrection);
            } else if (cityIds.size() > 1) {
                facilityServiceType.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);
                return;
            } else {
                facilityServiceType.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (cityId != null) {
            facilityServiceType.setInternalCityId(cityId);
        }

        //связывание улицы
        String streetCode = facilityServiceType.getStringField(FacilityServiceTypeDBF.CDUL);
        StreetCorrection streetCorrection;

        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByCode(
                cityCorrection.getId(), streetCode, osznId, userOrganizationId);

        if (streetCorrections.isEmpty()){
            try {
                facilityReferenceBookBean.updateStreetCorrections(streetCode, userOrganizationId, osznId);

                streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByCode(
                        cityCorrection.getId(), streetCode, osznId, userOrganizationId);
            } catch (ExecuteException e) {
                log.error("Ошибка создания коррекции", e);
            }
        }

        if (streetCorrections.size() >= 1) {
            //сформируем множество названий улиц
            Set<String> streetNames = Sets.newHashSet();
            for (StreetCorrection streetCorr : streetCorrections) {
                streetNames.add(streetCorr.getCorrection());
            }

            if (streetNames.size() == 1) { //все улицы имеют одно название - его и берём
                streetCorrection = streetCorrections.get(0);
            } else {
                //улицы имеют разные названия. Этого не должно быть, т.к. пара (название улицы, код улицы) уникальна на 
                //множестве соответствий для одной организации(осзн).
                facilityServiceType.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
                return;
            }
        } else {
            facilityServiceType.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }

        if (streetCorrection != null) {
            facilityServiceType.setStreetCorrectionId(streetCorrection.getId());
            facilityServiceType.setInternalStreetId(streetCorrection.getObjectId());
            facilityServiceType.setStreet(streetCorrection.getCorrection());
            DomainObject streetObject = streetStrategy.findById(streetCorrection.getObjectId(), true);
            facilityServiceType.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            facilityServiceType.setStreetType(streetCorrection.getStreetTypeCorrection().getCorrection());
            facilityServiceType.setInternalCityId(streetObject.getParentId());
        }
    }

    private void resolveLocalAddress(FacilityServiceType facilityServiceType, long userOrganizationId) {
        //осзн id
        long osznId = facilityServiceType.getOrganizationId();

        //Связывание дома
        String buildingNumber = facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE);
        String buildingCorp = facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                facilityServiceType.getStreetCorrectionId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
        if (buildingCorrections.size() == 1) {
            buildingCorrection = buildingCorrections.get(0);
            buildingId = buildingCorrection.getObjectId();
        } else if (buildingCorrections.size() > 1) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
            return;
        } else {
            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp,
                    facilityServiceType.getInternalStreetId(), facilityServiceType.getInternalCityId());
            if (buildingIds.size() == 1) {
                buildingId = buildingIds.get(0);
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber.toUpperCase(),
                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                        facilityServiceType.getStreetCorrectionId(), buildingId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insertBuilding(buildingCorrection);
            } else if (buildingIds.size() > 1) {
                facilityServiceType.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
                return;
            } else {
                facilityServiceType.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (buildingId != null) {
            facilityServiceType.setInternalBuildingId(buildingId);
            IStrategy buildingStrategy = strategyFactory.getStrategy("building");
            Building building = (Building) buildingStrategy.findById(buildingId, true);
            Long internalStreetId = building.getPrimaryStreetId();
            if (facilityServiceType.getInternalStreetId() != null
                    && !facilityServiceType.getInternalStreetId().equals(internalStreetId)) {
                facilityServiceType.setInternalStreetId(internalStreetId);
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                facilityServiceType.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                facilityServiceType.setInternalCityId(streetObject.getParentId());
            }
            facilityServiceType.setStatus(RequestStatus.CITY_UNRESOLVED);
        }
    }

    /**
     * Разрешить переход "ОСЗН адрес -> локальная адресная база"
     * Алгоритм:
     * Сначала пытаемся поискать город в таблице коррекций по названию города, пришедшего из ОСЗН и id ОСЗН.
     * Если не успешно, то пытаемся поискать по локальной адресной базе.
     * Если успешно, то записать коррекцию в таблицу коррекций.
     * Если город в итоге нашли, то проставляем его в internalCityId, иначе проставляем статус RequestStatus.CITY_UNRESOLVED_LOCALLY
     * и выходим, т.к. без города искать далее не имеет смысла.
     * Улицы ищем только в локальной адресной базе. Причем сначала ищем только по названию улицы. Если нашли ровно одну, т.е. существует только
     * один тип улицы для улицы с таким названием, то поиск успешен, по id улицы узнаем тип улицы, по id типа улицы находим(или создаем
     * если ничего не нашли) коррекцию для типа улицы, находим (или создаем) коррекцию для улицы. Далее обрабатываем дом по схеме аналогичной
     * схеме обработки города.
     * Если по названию улицы ничего не нашли, то проставляем статус RequestStatus.STREET_UNRESOLVED_LOCALLY и выходим.
     * Если же нашли более одной, то пытаемся поискать по названию улицы, номеру дома и корпуса(если есть). Если нашли ровно одну улицу, то
     * проставляем в payment id улицы и дома и выходим не создаваю никаких коррекций. Если не нашли ничего или более одной, то проставляем
     * статус RequestStatus.STREET_UNRESOLVED_LOCALLY и выходим.
     * Замечание: статус RequestStatus.STREET_UNRESOLVED_LOCALLY не позволяет корректировать улицы для payment, см.
     *      RequestStatus.isAddressCorrectableForPayment().
     *
     * Это алгоритм применяется и к поиску домов и с незначительными поправками к поиску улиц.
     */
    private void resolveLocalAddress(Payment payment, long userOrganizationId) {
        //осзн id
        long osznId = payment.getOrganizationId();

        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");

        //Связывание города
        String city = payment.getStringField(PaymentDBF.N_NAME);
        Long cityId = null;
        Correction cityCorrection = null;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
        if (cityCorrections.size() == 1) {
            cityCorrection = cityCorrections.get(0);
            cityId = cityCorrection.getObjectId();
        } else if (cityCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);
            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.findInternalCityIds(city);
            if (cityIds.size() == 1) {
                cityId = cityIds.get(0);
                cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                addressCorrectionBean.insert(cityCorrection);
            } else if (cityIds.size() > 1) {
                payment.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);
                return;
            } else {
                payment.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (cityId != null) {
            payment.setInternalCityId(cityId);
        }

        //Связывание улицы
        String street = payment.getStringField(PaymentDBF.VUL_NAME);
        String buildingNumber = payment.getStringField(PaymentDBF.BLD_NUM);
        String buildingCorp = payment.getStringField(PaymentDBF.CORP_NUM);

        Long streetId = null;
        StreetCorrection streetCorrection = null;
        //сначала ищем по коррекции, чтобы найти внутреннее название улицы
        List<Long> streetObjectIds = addressCorrectionBean.findLocalCorrectionStreetObjectIds(cityCorrection.getId(),
                street, osznId, userOrganizationId);
        if (streetObjectIds.size() >= 1) { // в коррекциях нашли соответствия на один или более внутренних объектов улиц
            //сформируем множество названий
            Set<String> streetNames = Sets.newHashSet();
            for (Long streetObjectId : streetObjectIds) {
                String streetName = streetStrategy.getName(streetObjectId);
                if (!Strings.isEmpty(streetName)) {
                    streetNames.add(streetName);
                }
            }
            if (streetNames.size() == 1) { //нашли внутренее название улицы
                String streetName = Lists.newArrayList(streetNames).get(0);
                //находим ids улиц по внутреннему названию
                List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(null, streetName, cityId);
                if (streetIds.size() == 1) { //нашли ровно одну улицу
                    streetId = streetIds.get(0);

                    DomainObject streetObject = streetStrategy.findById(streetId, true);
                    payment.setInternalStreetId(streetId);
                    long streetTypeObjectId = StreetStrategy.getStreetType(streetObject);
                    payment.setInternalStreetTypeId(streetTypeObjectId);
                    payment.setInternalCityId(streetObject.getParentId());
                    List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByStreetId(
                            streetId, cityCorrection.getId(), street, osznId, userOrganizationId);
                    if (streetCorrections.size() >= 1) {
                        streetCorrection = streetCorrections.get(0);
                    } else {
                        throw new IllegalStateException("Street correction was not found.");
                    }
                    //перейти к обработке дома
                } else if (streetIds.size() > 1) { // нашли больше одной улицы
                    //пытаемся найти по району
                    streetIds = addressCorrectionBean.findInternalStreetIdsByDistrict(cityId, street, osznId);
                    if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                        streetId = streetIds.get(0);
                        DomainObject streetObject = streetStrategy.findById(streetId, true);
                        payment.setInternalStreetId(streetId);
                        long streetTypeObjectId = StreetStrategy.getStreetType(streetObject);
                        payment.setInternalStreetTypeId(streetTypeObjectId);
                        payment.setInternalCityId(streetObject.getParentId());
                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrectionsByStreetId(
                                streetId, cityCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() >= 1) {
                            streetCorrection = streetCorrections.get(0);
                        } else {
                            throw new IllegalStateException("Street correction was not found.");
                        }
                        //перейти к обработке дома
                    } else {
                        // пытаемся искать дополнительно по номеру и корпусу дома
                        streetIds = addressCorrectionBean.findInternalStreetIdsByNameAndBuilding(cityId, streetName,
                                buildingNumber, buildingCorp);
                        if (streetIds.size() == 1) { //нашли ровно одну улицу с заданным номером и корпусом дома
                            streetId = streetIds.get(0);

                            //проставить дом для payment и выйти
                            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber,
                                    buildingCorp, streetId, cityId);
                            if (buildingIds.size() == 1) {
                                Long buildingId = buildingIds.get(0);
                                payment.setInternalBuildingId(buildingId);
                                Building building = (Building) buildingStrategy.findById(buildingId, true);
                                streetId = building.getPrimaryStreetId();
                                payment.setInternalStreetId(streetId);
                                DomainObject streetObject = streetStrategy.findById(streetId, true);
                                payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                                Long internalCityId = streetObject.getParentId();
                                payment.setInternalCityId(internalCityId);
                            } else {
                                throw new IllegalStateException("Building id was not found.");
                            }
                            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
                            return;
                        } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                            payment.setStatus(RequestStatus.STREET_AND_BUILDING_UNRESOLVED_LOCALLY);
                            return;
                        }
                    }
                } else {
                    throw new IllegalStateException("Street name `" + streetName + "` was not found.");
                }
            } else {
                throw new IllegalStateException("Street `" + street + "` is mapped to more one internal street objects: " + streetNames);
            }
        } else { // в коррекциях не нашли ни одного соответствия на внутренние объекты улиц
            // ищем по внутреннему справочнику улиц
            List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(null, street, cityId);
            if (streetIds.size() == 1) { // нашли ровно одну улицу
                streetId = streetIds.get(0);

                DomainObject streetObject = streetStrategy.findById(streetId, true);
                payment.setInternalStreetId(streetId);
                long streetTypeObjectId = StreetStrategy.getStreetType(streetObject);
                DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeObjectId, true);
                payment.setInternalStreetTypeId(streetTypeObjectId);
                payment.setInternalCityId(streetObject.getParentId());
                String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());

                //нужно создать коррекцию для улицы
                //нужно создать коррекцию для дома

                //create street type correction at first
                Correction streetTypeCorrection = findOrCreateStreetTypeCorrection(payment, streetType, streetTypeObjectId,
                        osznId, userOrganizationId);
                if (streetTypeCorrection == null) {
                    return;
                }

                //create street correction
                streetCorrection = findOrCreateStreetCorrection(payment, cityCorrection.getId(), streetTypeCorrection.getId(),
                        street, streetId, osznId, userOrganizationId);
                if (streetCorrection == null) {
                    return;
                }
                // перейти к обработке дома
            } else if (streetIds.size() > 1) { // нашли более одной улицы
                //пытаемся найти по району
                streetIds = addressCorrectionBean.findInternalStreetIdsByDistrict(cityId, street, osznId);
                if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                    streetId = streetIds.get(0);

                    DomainObject streetObject = streetStrategy.findById(streetId, true);
                    payment.setInternalStreetId(streetId);
                    long streetTypeObjectId = StreetStrategy.getStreetType(streetObject);
                    DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeObjectId, true);
                    payment.setInternalStreetTypeId(streetTypeObjectId);
                    payment.setInternalCityId(streetObject.getParentId());
                    String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());

                    //нужно создать коррекцию для улицы
                    //нужно создать коррекцию для дома

                    //create street type correction at first
                    Correction streetTypeCorrection = findOrCreateStreetTypeCorrection(payment, streetType,
                            streetTypeObjectId, osznId, userOrganizationId);
                    if (streetTypeCorrection == null) {
                        return;
                    }

                    //create street correction
                    streetCorrection = findOrCreateStreetCorrection(payment, cityCorrection.getId(), streetTypeCorrection.getId(),
                            street, streetId, osznId, userOrganizationId);
                    if (streetCorrection == null) {
                        return;
                    }
                    // перейти к обработке дома
                } else {
                    // пытаемся искать дополнительно по номеру и корпусу дома
                    streetIds = addressCorrectionBean.findInternalStreetIdsByNameAndBuilding(cityId, street, buildingNumber, buildingCorp);
                    if (streetIds.size() == 1) {
                        streetId = streetIds.get(0);

                        //не нужно создавать коррекцию для улицы
                        //не нужно создавать коррекцию для дома

                        //проставить дом для payment и выйти
                        List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp, streetId, cityId);
                        if (buildingIds.size() == 1) {
                            Long buildingId = buildingIds.get(0);
                            payment.setInternalBuildingId(buildingId);
                            Building building = (Building) buildingStrategy.findById(buildingId, true);
                            streetId = building.getPrimaryStreetId();
                            payment.setInternalStreetId(streetId);
                            DomainObject streetObject = streetStrategy.findById(streetId, true);
                            payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                            Long internalCityId = streetObject.getParentId();
                            payment.setInternalCityId(internalCityId);
                        } else {
                            throw new IllegalStateException("Building id was not found.");
                        }
                        payment.setStatus(RequestStatus.CITY_UNRESOLVED);
                        return;
                    } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                        payment.setStatus(RequestStatus.STREET_AND_BUILDING_UNRESOLVED_LOCALLY);
                        return;
                    }
                }
            } else { // не нашли ни одной улицы
                payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        }

        //Связывание дома
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
        if (buildingCorrections.size() == 1) {
            buildingCorrection = buildingCorrections.get(0);
            buildingId = buildingCorrection.getObjectId();
        } else if (buildingCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
            return;
        } else {
            List<Long> buildingIds = addressCorrectionBean.findInternalBuildingIds(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingIds.size() == 1) {
                buildingId = buildingIds.get(0);
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber.toUpperCase(),
                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                        streetCorrection.getId(), buildingId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID,
                        userOrganizationId);
                addressCorrectionBean.insertBuilding(buildingCorrection);
            } else if (buildingIds.size() > 1) {
                payment.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
                return;
            } else {
                payment.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
                return;
            }
        }

        if (buildingId != null) {
            payment.setInternalBuildingId(buildingId);
            Building building = (Building) buildingStrategy.findById(buildingId, true);
            Long internalStreetId = building.getPrimaryStreetId();
            if (streetId != null && !streetId.equals(internalStreetId)) {
                payment.setInternalStreetId(internalStreetId);
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                Long internalCityId = streetObject.getParentId();
                payment.setInternalCityId(internalCityId);
            }
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
        }
    }

    @Transactional
    private Correction findOrCreateStreetTypeCorrection(AbstractRequest request, String streetType, long streetTypeObjectId,
            long osznId, long userOrganizationId) {
        Correction streetTypeCorrection = null;
        List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                osznId, userOrganizationId);
        if (streetTypeCorrections.size() == 1) {
            streetTypeCorrection = streetTypeCorrections.get(0);
            return streetTypeCorrection;
        } else if (streetTypeCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION);
            return null;
        } else {
            streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                    streetTypeObjectId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
            addressCorrectionBean.insert(streetTypeCorrection);
            return streetTypeCorrection;
        }
    }

    @Transactional
    private StreetCorrection findOrCreateStreetCorrection(AbstractRequest request, long cityCorrectionId, long streetTypeCorrectionId,
            String street, long streetObjectId, long osznId, long userOrganizationId) {
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrectionId,
                streetTypeCorrectionId, street, osznId, userOrganizationId);
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
            if (streetObjectId != streetCorrection.getObjectId()) {
                request.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
                return null;
            }
            return streetCorrection;
        } else if (streetCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
            return null;
        } else {
            streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), null,
                    streetTypeCorrectionId, cityCorrectionId, streetObjectId, osznId,
                    IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
            addressCorrectionBean.insertStreet(streetCorrection);
            return streetCorrection;
        }
    }

    /**
     * разрешить переход "локальная адресная база -> адрес центра начислений"
     * Алгоритм:
     * Пытаемся найти коррекцию(строковое полное название и код) по id города в локальной адресной базе и текущему ЦН.
     * Если не нашли, то проставляем статус RequestStatus.CITY_UNRESOLVED и выходим, т.к. без города продолжать не имеет смысла.
     * Если нашли, то проставляем в payment информацию из коррекции(полное название, код) посредством метода
     * adapter.prepareCity() в адаптере для взаимодействия с ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter - адаптер по умолчанию.
     * Квартиры не ищем, а проставляем напрямую, обрезая пробелы.
     * Алгоритм аналогичен для поиска остальных составляющих адреса.
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment, CalculationContext calculationContext) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationContext.getCalculationCenterId(),
                payment.getInternalCityId());
        if (cityCorrections.size() == 1) {
            Correction cityCorrection = cityCorrections.get(0);
            adapter.prepareCity(payment, cityCorrection.getCorrection(), cityCorrection.getCode());
        } else if (cityCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);
            return;
        } else {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }

        //поиск района
        resolveOutgoingDistrict(payment, calculationContext);
        if (payment.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || payment.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationContext.getCalculationCenterId(),
                payment.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
        } else if (streetCorrections.size() > 1) {
            streetCorrections = addressCorrectionBean.findStreetRemoteCorrectionsByBuilding(calculationContext.getCalculationCenterId(),
                    payment.getInternalStreetId(), payment.getInternalBuildingId());
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
            } else {
                payment.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
                return;
            }
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(payment, streetCorrection.getCorrection(), streetCorrection.getCode());
        //получаем тип улицы
        Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
        if (streetTypeCorrection == null) {
            payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        } else {
            adapter.prepareStreetType(payment, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationContext.getCalculationCenterId(),
                payment.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(payment, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(),
                    buildingCorrection.getCode());
        } else if (buildingCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);
            return;
        } else {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }

        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(payment, null, null);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    @Transactional
    public void resolveOutgoingDistrict(Payment payment, CalculationContext calculationContext) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationContext.getCalculationCenterId(),
                payment.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(payment, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        }
    }

    @Transactional
    public void resolveOutgoingAddress(ActualPayment actualPayment, CalculationContext calculationContext) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationContext.getCalculationCenterId(),
                actualPayment.getInternalCityId());
        if (cityCorrections.size() == 1) {
            Correction cityCorrection = cityCorrections.get(0);
            adapter.prepareCity(actualPayment, cityCorrection.getCorrection(), cityCorrection.getCode());
        } else if (cityCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);
            return;
        } else {
            actualPayment.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }

        //поиск района
        resolveOutgoingDistrict(actualPayment, calculationContext);
        if (actualPayment.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || actualPayment.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationContext.getCalculationCenterId(),
                actualPayment.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
        } else if (streetCorrections.size() > 1) {
            streetCorrections = addressCorrectionBean.findStreetRemoteCorrectionsByBuilding(calculationContext.getCalculationCenterId(),
                    actualPayment.getInternalStreetId(), actualPayment.getInternalBuildingId());
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
            } else {
                actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
                return;
            }
        } else {
            actualPayment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(actualPayment, streetCorrection.getCorrection(), streetCorrection.getCode());
        //получаем тип улицы
        Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
        if (streetTypeCorrection == null) {
            actualPayment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        } else {
            adapter.prepareStreetType(actualPayment, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationContext.getCalculationCenterId(),
                actualPayment.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(actualPayment, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(),
                    buildingCorrection.getCode());
        } else if (buildingCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);
            return;
        } else {
            actualPayment.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }

        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(actualPayment, null, null);
        actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    @Transactional
    public void resolveOutgoingAddress(Subsidy subsidy, CalculationContext calculationContext) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationContext.getCalculationCenterId(),
                subsidy.getInternalCityId());
        if (cityCorrections.size() == 1) {
            Correction cityCorrection = cityCorrections.get(0);
            adapter.prepareCity(subsidy, cityCorrection.getCorrection(), cityCorrection.getCode());
        } else if (cityCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);
            return;
        } else {
            subsidy.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }

        //поиск района
        resolveOutgoingDistrict(subsidy, calculationContext);
        if (subsidy.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || subsidy.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationContext.getCalculationCenterId(),
                subsidy.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
        } else if (streetCorrections.size() > 1) {
            streetCorrections = addressCorrectionBean.findStreetRemoteCorrectionsByBuilding(calculationContext.getCalculationCenterId(),
                    subsidy.getInternalStreetId(), subsidy.getInternalBuildingId());
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
            } else {
                subsidy.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
                return;
            }
        } else {
            subsidy.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(subsidy, streetCorrection.getCorrection(), streetCorrection.getCode());
        //получаем тип улицы
        Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
        if (streetTypeCorrection == null) {
            subsidy.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        } else {
            adapter.prepareStreetType(subsidy, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationContext.getCalculationCenterId(),
                subsidy.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(subsidy, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(),
                    buildingCorrection.getCode());
        } else if (buildingCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);
            return;
        } else {
            subsidy.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }

        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(subsidy, null, null);
        subsidy.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    @Transactional
    public void resolveOutgoingAddress(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationContext.getCalculationCenterId(),
                dwellingCharacteristics.getInternalCityId());
        if (cityCorrections.size() == 1) {
            Correction cityCorrection = cityCorrections.get(0);
            adapter.prepareCity(dwellingCharacteristics, cityCorrection.getCorrection(), cityCorrection.getCode());
        } else if (cityCorrections.size() > 1) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);
            return;
        } else {
            dwellingCharacteristics.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }

        //поиск района
        resolveOutgoingDistrict(dwellingCharacteristics, calculationContext);
        if (dwellingCharacteristics.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || dwellingCharacteristics.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationContext.getCalculationCenterId(),
                dwellingCharacteristics.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
        } else if (streetCorrections.size() > 1) {
            streetCorrections = addressCorrectionBean.findStreetRemoteCorrectionsByBuilding(calculationContext.getCalculationCenterId(),
                    dwellingCharacteristics.getInternalStreetId(), dwellingCharacteristics.getInternalBuildingId());
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
            } else {
                dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
                return;
            }
        } else {
            dwellingCharacteristics.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(dwellingCharacteristics, streetCorrection.getCorrection(), streetCorrection.getCode());
        //получаем тип улицы
        Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
        if (streetTypeCorrection == null) {
            dwellingCharacteristics.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        } else {
            adapter.prepareStreetType(dwellingCharacteristics, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationContext.getCalculationCenterId(),
                dwellingCharacteristics.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(dwellingCharacteristics, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(),
                    buildingCorrection.getCode());
        } else if (buildingCorrections.size() > 1) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);
            return;
        } else {
            dwellingCharacteristics.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }

        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(dwellingCharacteristics, null, null);
        dwellingCharacteristics.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    @Transactional
    public void resolveOutgoingAddress(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationContext.getCalculationCenterId(),
                facilityServiceType.getInternalCityId());
        if (cityCorrections.size() == 1) {
            Correction cityCorrection = cityCorrections.get(0);
            adapter.prepareCity(facilityServiceType, cityCorrection.getCorrection(), cityCorrection.getCode());
        } else if (cityCorrections.size() > 1) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);
            return;
        } else {
            facilityServiceType.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }

        //поиск района
        resolveOutgoingDistrict(facilityServiceType, calculationContext);
        if (facilityServiceType.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || facilityServiceType.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationContext.getCalculationCenterId(),
                facilityServiceType.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
        } else if (streetCorrections.size() > 1) {
            streetCorrections = addressCorrectionBean.findStreetRemoteCorrectionsByBuilding(calculationContext.getCalculationCenterId(),
                    facilityServiceType.getInternalStreetId(), facilityServiceType.getInternalBuildingId());
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
            } else {
                facilityServiceType.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
                return;
            }
        } else {
            facilityServiceType.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(facilityServiceType, streetCorrection.getCorrection(), streetCorrection.getCode());
        //получаем тип улицы
        Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
        if (streetTypeCorrection == null) {
            facilityServiceType.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        } else {
            adapter.prepareStreetType(facilityServiceType, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationContext.getCalculationCenterId(),
                facilityServiceType.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(facilityServiceType, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(),
                    buildingCorrection.getCode());
        } else if (buildingCorrections.size() > 1) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);
            return;
        } else {
            facilityServiceType.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }

        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(facilityServiceType, null, null);
        facilityServiceType.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    @Transactional
    public void resolveOutgoingDistrict(ActualPayment actualPayment, CalculationContext calculationContext) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationContext.getCalculationCenterId(),
                actualPayment.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(actualPayment, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            actualPayment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        }
    }

    @Transactional
    public void resolveOutgoingDistrict(Subsidy subsidy, CalculationContext calculationContext) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationContext.getCalculationCenterId(),
                subsidy.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(subsidy, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            subsidy.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            subsidy.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        }
    }

    @Transactional
    public void resolveOutgoingDistrict(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationContext.getCalculationCenterId(),
                dwellingCharacteristics.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(dwellingCharacteristics, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            dwellingCharacteristics.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        }
    }

    @Transactional
    public void resolveOutgoingDistrict(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationContext.getCalculationCenterId(),
                facilityServiceType.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(facilityServiceType, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            facilityServiceType.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
        } else {
            facilityServiceType.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        }
    }

    /**
     * Разрешен ли адрес.
     * Адрес считаем разрешенным, если статус payment записи не входит в список статусов, указывающих на то что адрес не разрешен локально,
     * не входит в список статусов, указывающих на то что адрес не разрешен в ЦН, и не равен RequestStatus.ADDRESS_CORRECTED,
     * который указывает на то, что адрес откорректировали в UI.
     * См. RequestStatus
     */
    public boolean isAddressResolved(AbstractRequest request) {
        return request.getStatus().isAddressResolved();
    }

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     */
    @Transactional
    public void resolveAddress(Payment payment, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(payment, calculationContext.getUserOrganizationId());
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (payment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(payment, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(ActualPayment actualPayment, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(actualPayment, calculationContext.getUserOrganizationId());
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (actualPayment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(actualPayment, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(Subsidy subsidy, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(subsidy, calculationContext.getUserOrganizationId());
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (subsidy.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(subsidy, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(dwellingCharacteristics, calculationContext.getUserOrganizationId());
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (dwellingCharacteristics.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(dwellingCharacteristics, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(facilityServiceType, calculationContext.getUserOrganizationId());
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (facilityServiceType.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(facilityServiceType, calculationContext);
        }
    }

    /**
     * Корректирование адреса из UI.
     * Алгоритм:
     * Если у payment записи id города NULL и откорректированный город не NULL, то
     * вставить коррекцию для города в таблицу коррекций городов, коррекировать payment(PaymentBean.correctCity())
     * и benefit записи соответствующие данному payment(BenefitBean.addressCorrected()).
     *
     * Алгоритм аналогичен для других составляющих адреса.
     *
     * @param payment
     * @param cityId Откорректированный город
     * @param streetId Откорректированная улица
     * @param streetTypeId Откорректированный тип улицы
     * @param buildingId Откорректированный дом
     */
    @Transactional
    public void correctLocalAddress(Payment payment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        final long osznId = payment.getOrganizationId();
        final long requestFileId = payment.getRequestFileId();

        String city = payment.getStringField(PaymentDBF.N_NAME);
        String street = payment.getStringField(PaymentDBF.VUL_NAME);
        String buildingNumber = payment.getStringField(PaymentDBF.BLD_NUM);
        String buildingCorp = payment.getStringField(PaymentDBF.CORP_NUM);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
//                    if (addressCorrectionBean.checkAddressExistence(cityCorrection)) {
//                        throw new DublicateCorrectionException();
//                    }
                    addressCorrectionBean.insert(cityCorrection);
                    paymentBean.markCorrected(requestFileId, city);
                    benefitBean.markCorrected(requestFileId);
                }
            }
            break;
            case STREET_TYPE: {
                throw new IllegalArgumentException("Street type couldn't corrected for payment.");
            }
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    //find or create street type correction at first
                    IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
                    DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeId, true);
                    String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());
                    Correction streetTypeCorrection = null;
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        streetTypeCorrection = streetTypeCorrections.get(0);
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                                streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                        addressCorrectionBean.insert(streetTypeCorrection);
                    }

                    List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                            streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                    if (streetCorrections.size() > 0) {
                        throw new DublicateCorrectionException();
                    } else {
                        StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), null,
                                streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                                IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
//                            if (addressCorrectionBean.checkStreetExistence(streetCorrection)) {
//                                throw new DublicateCorrectionException();
//                            }
                        addressCorrectionBean.insertStreet(streetCorrection);
                        paymentBean.markCorrected(requestFileId, city, street);
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    long streetTypeObjectId = payment.getInternalStreetTypeId();
                    IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
                    DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeObjectId, true);
                    String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(
                                cityCorrection.getId(), streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                                    streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(
                                        buildingNumber.toUpperCase(),
                                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                                        streetCorrection.getId(), buildingId, osznId,
                                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
//                                if (addressCorrectionBean.checkBuildingExistence(buildingCorrection)) {
//                                    throw new DublicateCorrectionException();
//                                }
                                addressCorrectionBean.insertBuilding(buildingCorrection);
                                paymentBean.markCorrected(requestFileId, city, street, buildingNumber, buildingCorp);
                                benefitBean.markCorrected(requestFileId);
                            }
                        } else if (streetCorrections.size() > 1) {
                            throw new MoreOneCorrectionException("street");
                        } else {
                            throw new NotFoundCorrectionException("street");
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
        }
    }

    @Transactional
    public void correctLocalAddress(ActualPayment actualPayment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        final long osznId = actualPayment.getOrganizationId();
        final long requestFileId = actualPayment.getRequestFileId();

        String city = actualPayment.getStringField(ActualPaymentDBF.N_NAME);
        String streetType = actualPayment.getStringField(ActualPaymentDBF.VUL_CAT);
        String streetCode = actualPayment.getStringField(ActualPaymentDBF.VUL_CODE);
        String street = actualPayment.getStringField(ActualPaymentDBF.VUL_NAME);
        String buildingNumber = actualPayment.getStringField(ActualPaymentDBF.BLD_NUM);
        String buildingCorp = actualPayment.getStringField(ActualPaymentDBF.CORP_NUM);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(cityCorrection);
                    actualPaymentBean.markCorrected(requestFileId, city);
                }
            }
            break;
            case STREET_TYPE: {
                List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                        osznId, userOrganizationId);
                if (streetTypeCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                            streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(streetTypeCorrection);
                    actualPaymentBean.markCorrected(requestFileId, city, streetType);
                }
            }
            break;
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(
                                cityCorrection.getId(), streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() > 0) {
                            throw new DublicateCorrectionException();
                        } else {
                            StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(),
                                    streetCode.toUpperCase(),
                                    streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                                    IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                            addressCorrectionBean.insertStreet(streetCorrection);
                            actualPaymentBean.markCorrected(requestFileId, city, streetType, streetCode);
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                                    streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(
                                        buildingNumber.toUpperCase(),
                                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                                        streetCorrection.getId(), buildingId, osznId,
                                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                                addressCorrectionBean.insertBuilding(buildingCorrection);
                                actualPaymentBean.markCorrected(requestFileId, city, streetType, streetCode, buildingNumber, buildingCorp);
                            }
                        } else if (streetCorrections.size() > 1) {
                            throw new MoreOneCorrectionException("street");
                        } else {
                            throw new NotFoundCorrectionException("street");
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
        }
    }

    @Transactional
    public void correctLocalAddress(Subsidy subsidy, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId,
            Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        final long osznId = subsidy.getOrganizationId();
        final long requestFileId = subsidy.getRequestFileId();

        String city = subsidy.getStringField(SubsidyDBF.NP_NAME);
        String streetType = subsidy.getStringField(SubsidyDBF.CAT_V);
        String streetCode = subsidy.getStringField(SubsidyDBF.VULCOD);
        String street = subsidy.getStringField(SubsidyDBF.NAME_V);
        String buildingNumber = subsidy.getStringField(SubsidyDBF.BLD);
        String buildingCorp = subsidy.getStringField(SubsidyDBF.CORP);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(cityCorrection);
                    subsidyBean.markCorrected(requestFileId, city);
                }
            }
            break;
            case STREET_TYPE: {
                List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                        osznId, userOrganizationId);
                if (streetTypeCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                            streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(streetTypeCorrection);
                    subsidyBean.markCorrected(requestFileId, city, streetType);
                }
            }
            break;
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(
                                cityCorrection.getId(), streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() > 0) {
                            throw new DublicateCorrectionException();
                        } else {
                            StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(),
                                    streetCode.toUpperCase(),
                                    streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                                    IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                            addressCorrectionBean.insertStreet(streetCorrection);
                            subsidyBean.markCorrected(requestFileId, city, streetType, streetCode);
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                                    streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(
                                        buildingNumber.toUpperCase(),
                                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                                        streetCorrection.getId(), buildingId, osznId,
                                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                                addressCorrectionBean.insertBuilding(buildingCorrection);
                                subsidyBean.markCorrected(requestFileId, city, streetType, streetCode, buildingNumber, buildingCorp);
                            }
                        } else if (streetCorrections.size() > 1) {
                            throw new MoreOneCorrectionException("street");
                        } else {
                            throw new NotFoundCorrectionException("street");
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
        }
    }

    @Transactional
    public void correctLocalAddress(DwellingCharacteristics dwellingCharacteristics, CORRECTED_ENTITY entity, Long cityId,
            Long streetTypeId, Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        final long osznId = dwellingCharacteristics.getOrganizationId();
        final long requestFileId = dwellingCharacteristics.getRequestFileId();

        String city = dwellingCharacteristics.getCity();
        String streetCode = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.CDUL);
        String buildingNumber = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE);
        String buildingCorp = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(cityCorrection);
                    dwellingCharacteristicsBean.markCorrected(requestFileId);
                }
            }
            break;
            case STREET_TYPE: {
                throw new IllegalArgumentException("Street type couldn't corrected for dwelling characteristics.");
            }
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    //find or create street type correction at first
                    IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
                    DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeId, true);
                    String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());
                    Correction streetTypeCorrection = null;
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        streetTypeCorrection = streetTypeCorrections.get(0);
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                                streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                        addressCorrectionBean.insert(streetTypeCorrection);
                    }

                    String street = streetStrategy.getName(streetId);
                    List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                            streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                    if (streetCorrections.size() > 0) {
                        throw new DublicateCorrectionException();
                    } else {
                        StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), streetCode,
                                streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                                IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                        addressCorrectionBean.insertStreet(streetCorrection);
                        dwellingCharacteristicsBean.markCorrected(requestFileId, streetCode);
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    final String streetType = dwellingCharacteristics.getStreetType();
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        final String street = dwellingCharacteristics.getStreet();
                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                                    streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(
                                        buildingNumber.toUpperCase(),
                                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                                        streetCorrection.getId(), buildingId, osznId,
                                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                                addressCorrectionBean.insertBuilding(buildingCorrection);
                                dwellingCharacteristicsBean.markCorrected(requestFileId, streetCode, buildingNumber, buildingCorp);
                            }
                        } else if (streetCorrections.size() > 1) {
                            throw new MoreOneCorrectionException("street");
                        } else {
                            throw new NotFoundCorrectionException("street");
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
        }
    }

    @Transactional
    public void correctLocalAddress(FacilityServiceType facilityServiceType, CORRECTED_ENTITY entity, Long cityId,
            Long streetTypeId, Long streetId, Long buildingId, long userOrganizationId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        final long osznId = facilityServiceType.getOrganizationId();
        final long requestFileId = facilityServiceType.getRequestFileId();

        String city = facilityServiceType.getCity();
        String streetCode = facilityServiceType.getStringField(FacilityServiceTypeDBF.CDUL);
        String buildingNumber = facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE);
        String buildingCorp = facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city.toUpperCase(), cityId, osznId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                    addressCorrectionBean.insert(cityCorrection);
                    dwellingCharacteristicsBean.markCorrected(requestFileId);
                }
            }
            break;
            case STREET_TYPE: {
                throw new IllegalArgumentException("Street type couldn't corrected for facility service type.");
            }
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    //find or create street type correction at first
                    IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
                    DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeId, true);
                    String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());
                    Correction streetTypeCorrection = null;
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        streetTypeCorrection = streetTypeCorrections.get(0);
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType.toUpperCase(),
                                streetTypeId, osznId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                        addressCorrectionBean.insert(streetTypeCorrection);
                    }

                    String street = streetStrategy.getName(streetId);
                    List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                            streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                    if (streetCorrections.size() > 0) {
                        throw new DublicateCorrectionException();
                    } else {
                        StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street.toUpperCase(), streetCode,
                                streetTypeCorrection.getId(), cityCorrection.getId(), streetId, osznId,
                                IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                        addressCorrectionBean.insertStreet(streetCorrection);
                        dwellingCharacteristicsBean.markCorrected(requestFileId, streetCode);
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, osznId, userOrganizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    final String streetType = facilityServiceType.getStreetType();
                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetType,
                            osznId, userOrganizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        final String street = facilityServiceType.getStreet();
                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), street, osznId, userOrganizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(
                                    streetCorrection.getId(), buildingNumber, buildingCorp, osznId, userOrganizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(
                                        buildingNumber.toUpperCase(),
                                        buildingCorp != null ? buildingCorp.toUpperCase() : null,
                                        streetCorrection.getId(), buildingId, osznId,
                                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID, userOrganizationId);
                                addressCorrectionBean.insertBuilding(buildingCorrection);
                                dwellingCharacteristicsBean.markCorrected(requestFileId, streetCode, buildingNumber, buildingCorp);
                            }
                        } else if (streetCorrections.size() > 1) {
                            throw new MoreOneCorrectionException("street");
                        } else {
                            throw new NotFoundCorrectionException("street");
                        }
                    } else if (streetTypeCorrections.size() > 1) {
                        throw new MoreOneCorrectionException("street_type");
                    } else {
                        throw new NotFoundCorrectionException("street_type");
                    }
                } else if (cityCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("city");
                } else {
                    throw new NotFoundCorrectionException("city");
                }
            }
            break;
        }
    }
}
