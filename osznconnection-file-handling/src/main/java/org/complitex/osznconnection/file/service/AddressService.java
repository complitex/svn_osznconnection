/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;

/**
 * Класс разрешает адрес.
 * @author Artem
 */
@Stateless(name = "AddressService")
public class AddressService extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;
    
    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    /**
     * Разрешить переход "ОСЗН адрес -> локальная адресная база"
     * Алгоритм:
     * Сначала пытаемся поискать город в таблице коррекций по названию города, пришедшего из ОСЗН и id ОСЗН.
     * Если не успешно, то пытаемся поискать по локальной адресной базе.
     * Если успешно, то записать коррекцию в таблицу коррекций.
     * Если город в итоге нашли, то проставляем его в internalCityId, иначе проставляем статус RequestStatus.CITY_UNRESOLVED_LOCALLY
     * и выходим, т.к. без города искать далее не имеет смысла.
     *
     * Это алгоритм применяется и к поиску домов и с незначительными поправками к поиску улиц.
     *
     * @param payment
     */
    private void resolveLocalAddress(Payment payment) {
        //осзн id
        long organizationId = payment.getOrganizationId();

        //Связывание города
        String city = (String) payment.getField(PaymentDBF.N_NAME);
        Long cityId = null;
        Correction cityCorrection = null;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
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
                cityCorrection = addressCorrectionBean.createCityCorrection(city, cityId, organizationId,
                        OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        Long streetId = null;
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(), null, street,
                organizationId);
        if (streetCorrections.size() == 1) {
            streetCorrection = streetCorrections.get(0);
            streetId = streetCorrection.getObjectId();
        } else if (streetCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
            return;
        } else {
            List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(null, street, cityId);
            if (streetIds.size() == 1) {
                streetId = streetIds.get(0);
                streetCorrection = addressCorrectionBean.createStreetCorrection(street, cityCorrection.getId(), streetId, organizationId,
                        OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                addressCorrectionBean.insert(streetCorrection);
            } else if (streetIds.size() > 1) {
                payment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET);
                return;
            } else {
                payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        }

        //Если нашли улицу, то достаем из базы весь объект, берем тип улицы и проставляем в internalStreetTypeId
        if (streetId != null) {
            DomainObject streetObject = strategyFactory.getStrategy("street").findById(streetId);
            payment.setInternalStreetId(streetId);
            payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            payment.setInternalCityId(streetObject.getParentId());
        }

        //Связывание дома
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(streetCorrection.getId(), buildingNumber,
                buildingCorp, organizationId);
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
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber, buildingCorp, streetCorrection.getId(), buildingId,
                        organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
            Strategy buildingStrategy = strategyFactory.getStrategy("building");
            Building building = (Building) buildingStrategy.findById(buildingId);
            Long internalStreetId = building.getPrimaryStreetId();
            if (streetId != null && !streetId.equals(internalStreetId)) {
                payment.setInternalStreetId(internalStreetId);
                Strategy streetStrategy = strategyFactory.getStrategy("street");
                DomainObject streetObject = streetStrategy.findById(internalStreetId);
                payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                Long internalCityId = streetObject.getParentId();
                payment.setInternalCityId(internalCityId);
            }
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
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
     * @param payment
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationCenterId, payment.getInternalCityId());
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
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationCenterId, payment.getOrganizationId());
        if (districtCorrections.size() == 1) {
            Correction districtCorrection = districtCorrections.get(0);
            adapter.prepareDistrict(payment, districtCorrection.getCorrection(), districtCorrection.getCode());
        } else if (districtCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);
            return;
        } else {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
            return;
        }

        //поиск типа улицы
        List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeRemoteCorrections(calculationCenterId,
                payment.getInternalStreetTypeId());
        if (streetTypeCorrections.size() == 1) {
            Correction streetTypeCorrection = streetTypeCorrections.get(0);
            adapter.prepareStreetType(payment, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
        } else if (streetTypeCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_TYPE_CORRECTION);
            return;
        } else {
            payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
            return;
        }

        //поиск улицы
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationCenterId, payment.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            StreetCorrection streetCorrection = streetCorrections.get(0);
            adapter.prepareStreet(payment, streetCorrection.getCorrection(), streetCorrection.getCode());
        } else if (streetCorrections.size() > 1) {
            payment.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
            return;
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationCenterId,
                payment.getInternalBuildingId());
        if (buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            adapter.prepareBuilding(payment, buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(), buildingCorrection.getCode());
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

    /**
     * Разрешен ли адрес
     * @param payment
     * @return
     */
    public boolean isAddressResolved(Payment payment) {
        /*
         * Адрес считаем разрешенным, если статус payment записи не входит в список статусов, указывающих на то что адрес не разрешен локально,
         * не входит в список статусов, указывающих на то что адрес не разрешен в ЦН, и не равен RequestStatus.ADDRESS_CORRECTED,
         * который указывает на то, что адрес откорректировали в UI.
         * См. RequestStatus
         */
        return payment.getStatus().isAddressResolved();
    }

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param payment
     * @param calculationCenterId
     * @param adapter
     */
    @Transactional
    public void resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        //разрешить адрес локально
        resolveLocalAddress(payment);
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (payment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(payment, calculationCenterId, adapter);
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
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        if (streetId == null) { //откорректировали город
            Correction cityCorrection = addressCorrectionBean.createCityCorrection(city, cityId, organizationId,
                    OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            if (addressCorrectionBean.checkExistence(cityCorrection)) {
                throw new DublicateCorrectionException();
            }
            addressCorrectionBean.insert(cityCorrection);
            paymentBean.markCorrected(requestFileId, city);
            benefitBean.markCorrected(requestFileId);
        } else if (buildingId == null) { //откорректировали улицу
            List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
            if (cityCorrections.size() == 1) {
                Correction cityCorrection = cityCorrections.get(0);
                StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street, cityCorrection.getId(), streetId,
                        organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                if (addressCorrectionBean.checkStreetExistence(streetCorrection)) {
                    throw new DublicateCorrectionException();
                }
                addressCorrectionBean.insert(streetCorrection);
                paymentBean.markCorrected(requestFileId, city, street);
                benefitBean.markCorrected(requestFileId);
            } else if (cityCorrections.size() > 1) {
                throw new MoreOneCorrectionException("city");
            } else {
                throw new NotFoundCorrectionException("city");
            }

        } else {  //откорректировали здание
            List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
            if (cityCorrections.size() == 1) {
                Correction cityCorrection = cityCorrections.get(0);
                List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(), null, street,
                        organizationId);
                if (streetCorrections.size() == 1) {
                    StreetCorrection streetCorrection = streetCorrections.get(0);
                    BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber, buildingCorp,
                            streetCorrection.getId(), buildingId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                    if (addressCorrectionBean.checkBuildingExistence(buildingCorrection)) {
                        throw new DublicateCorrectionException();
                    }
                    addressCorrectionBean.insertBuilding(buildingCorrection);
                    paymentBean.markCorrected(requestFileId, city, street, buildingNumber, buildingCorp);
                    benefitBean.markCorrected(requestFileId);
                } else if (streetCorrections.size() > 1) {
                    throw new MoreOneCorrectionException("street");
                } else {
                    throw new NotFoundCorrectionException("street");
                }
            } else if (cityCorrections.size() > 1) {
                throw new MoreOneCorrectionException("city");
            } else {
                throw new NotFoundCorrectionException("city");
            }
        }
    }
}