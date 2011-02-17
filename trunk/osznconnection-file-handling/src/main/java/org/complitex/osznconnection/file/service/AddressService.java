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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.osznconnection.file.service.exception.DublicateCorrectionException;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.osznconnection.file.service.exception.MoreOneCorrectionException;
import org.complitex.osznconnection.file.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.web.component.address.AddressCorrectionPanel.CORRECTED_ENTITY;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

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
    private BenefitBean benefitBean;
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private LocaleBean localeBean;

    private void resolveLocalAddress(ActualPayment actualPayment) {
        //осзн id
        long organizationId = actualPayment.getOrganizationId();

        //Связывание города
        String city = (String) actualPayment.getField(ActualPaymentDBF.N_NAME);
        Long cityId = null;
        Correction cityCorrection = null;
        List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
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
                cityCorrection = addressCorrectionBean.createCityCorrection(city, cityId, organizationId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
        String streetType = (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT);
        Long streetTypeId = null;
        Correction streetTypeCorrection = null;
        List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrection(streetType, organizationId);
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
                streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType, streetTypeId, organizationId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
        String street = (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME);
        String streetCode = (String) actualPayment.getField(ActualPaymentDBF.VUL_CODE);
        Long streetId = null;
        StreetCorrection streetCorrection = null;
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(), streetTypeCorrection.getId(),
                null, streetCode, organizationId);
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
                streetCorrection = addressCorrectionBean.createStreetCorrection(street, streetCode, streetTypeCorrection.getId(),
                        cityCorrection.getId(), streetId, organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
        String buildingNumber = (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM);
        String buildingCorp = (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM);
        Long buildingId = null;
        BuildingCorrection buildingCorrection = null;
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(streetCorrection.getId(), buildingNumber,
                buildingCorp, organizationId);
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
                buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber, buildingCorp, streetCorrection.getId(), buildingId,
                        organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
                IStrategy streetStrategy = strategyFactory.getStrategy("street");
                DomainObject streetObject = streetStrategy.findById(internalStreetId, true);
                actualPayment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                Long internalCityId = streetObject.getParentId();
                actualPayment.setInternalCityId(internalCityId);
            }
            actualPayment.setStatus(RequestStatus.CITY_UNRESOLVED);
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
     *
     * @param actualPayment
     */
    private void resolveLocalAddress(Payment payment) {
        //осзн id
        long organizationId = payment.getOrganizationId();

        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");

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
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        Long streetId = null;
        StreetCorrection streetCorrection = null;
        List<Long> streetIds = addressCorrectionBean.findInternalStreetIds(null, street, cityId);
        if (streetIds.size() == 1) {
            streetId = streetIds.get(0);

            DomainObject streetObject = streetStrategy.findById(streetId, true);
            payment.setInternalStreetId(streetId);
            long streetTypeObjectId = StreetStrategy.getStreetType(streetObject);
            payment.setInternalStreetTypeId(streetTypeObjectId);
            payment.setInternalCityId(streetObject.getParentId());

            //нужно создать коррекцию для улицы
            //нужно создать коррекцию для дома

            //create street type correction at first
            Correction streetTypeCorrection = null;
            List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(streetTypeObjectId, organizationId);
            if (streetTypeCorrections.size() == 1) {
                streetTypeCorrection = streetTypeCorrections.get(0);
            } else if (streetTypeCorrections.size() > 1) {
                payment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION);
                return;
            } else {
                DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeObjectId, true);
                String streetType = streetTypeStrategy.displayDomainObject(streetTypeObject, localeBean.getSystemLocale());
                streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType, streetTypeObjectId, organizationId,
                        IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                addressCorrectionBean.insert(streetTypeCorrection);
            }

            //create street correction
            List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                    streetTypeCorrection.getId(), street, null, organizationId);
            if (streetCorrections.size() == 1) {
                streetCorrection = streetCorrections.get(0);
                if (!streetId.equals(streetCorrection.getObjectId())) {
                    payment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
                    return;
                }
            } else if (streetCorrections.size() > 1) {
                payment.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_CORRECTION);
                return;
            } else {
                streetCorrection = addressCorrectionBean.createStreetCorrection(street, null, streetTypeCorrection.getId(), cityCorrection.getId(),
                        streetId, organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                addressCorrectionBean.insertStreet(streetCorrection);
            }
        } else if (streetIds.size() > 1) {
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
                    throw new RuntimeException("Inconsistent queries results.");
                }
                payment.setStatus(RequestStatus.CITY_UNRESOLVED);
                return;
            } else {
                payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }

        //Связывание дома
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
                        organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
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
     * @param actualPayment
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
        resolveOutgoingDistrict(payment, calculationCenterId, adapter);
        if (payment.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || payment.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationCenterId, payment.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            StreetCorrection streetCorrection = streetCorrections.get(0);
            adapter.prepareStreet(payment, streetCorrection.getCorrection(), streetCorrection.getCode());

            //получаем тип улицы
            Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
            if (streetTypeCorrection == null) {
                payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
                return;
            } else {
                adapter.prepareStreetType(payment, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
            }
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

    @Transactional
    public void resolveOutgoingDistrict(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationCenterId, payment.getOrganizationId());
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
    public void resolveOutgoingAddress(ActualPayment actualPayment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        List<Correction> cityCorrections = addressCorrectionBean.findCityRemoteCorrections(calculationCenterId, actualPayment.getInternalCityId());
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
        resolveOutgoingDistrict(actualPayment, calculationCenterId, adapter);
        if (actualPayment.getStatus().equals(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION)
                || actualPayment.getStatus().equals(RequestStatus.DISTRICT_UNRESOLVED)) {
            return;
        }

        //поиск улицы
        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetRemoteCorrections(calculationCenterId,
                actualPayment.getInternalStreetId());
        if (streetCorrections.size() == 1) {
            StreetCorrection streetCorrection = streetCorrections.get(0);
            adapter.prepareStreet(actualPayment, streetCorrection.getCorrection(), streetCorrection.getCode());

            //получаем тип улицы
            Correction streetTypeCorrection = streetCorrection.getStreetTypeCorrection();
            if (streetTypeCorrection == null) {
                actualPayment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
                return;
            } else {
                adapter.prepareStreetType(actualPayment, streetTypeCorrection.getCorrection(), streetTypeCorrection.getCode());
            }
        } else if (streetCorrections.size() > 1) {
            actualPayment.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);
            return;
        } else {
            actualPayment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }

        //поиск дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingRemoteCorrections(calculationCenterId,
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
    public void resolveOutgoingDistrict(ActualPayment actualPayment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        List<Correction> districtCorrections = addressCorrectionBean.findDistrictRemoteCorrections(calculationCenterId,
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

    /**
     * Разрешен ли адрес
     * @param actualPayment
     * @return
     */
    public boolean isAddressResolved(AbstractRequest request) {
        /*
         * Адрес считаем разрешенным, если статус payment записи не входит в список статусов, указывающих на то что адрес не разрешен локально,
         * не входит в список статусов, указывающих на то что адрес не разрешен в ЦН, и не равен RequestStatus.ADDRESS_CORRECTED,
         * который указывает на то, что адрес откорректировали в UI.
         * См. RequestStatus
         */
        return request.getStatus().isAddressResolved();
    }

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param actualPayment
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

    @Transactional
    public void resolveAddress(ActualPayment actualPayment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        //разрешить адрес локально
        resolveLocalAddress(actualPayment);
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (actualPayment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(actualPayment, calculationCenterId, adapter);
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
    public void correctLocalAddress(Payment payment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId, Long streetId, Long buildingId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city, cityId, organizationId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                    if (addressCorrectionBean.checkAddressExistence(cityCorrection)) {
                        throw new DublicateCorrectionException();
                    }
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
                throw new IllegalArgumentException("Street couldn't corrected for payment.");
            }
            case BUILDING: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrections(payment.getInternalStreetTypeId(),
                            organizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), street, null, organizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(streetId,
                                    buildingNumber, buildingCorp, organizationId);
                            if (buildingCorrections.size() > 1) {
                                throw new DublicateCorrectionException();
                            } else if (buildingCorrections.size() == 0) {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber, buildingCorp,
                                        streetCorrection.getId(), buildingId, organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                                if (addressCorrectionBean.checkBuildingExistence(buildingCorrection)) {
                                    throw new DublicateCorrectionException();
                                }
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
    public void correctLocalAddress(ActualPayment actualPayment, CORRECTED_ENTITY entity, Long cityId, Long streetTypeId, Long streetId, Long buildingId)
            throws DublicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        long organizationId = actualPayment.getOrganizationId();
        long requestFileId = actualPayment.getRequestFileId();

        String city = (String) actualPayment.getField(ActualPaymentDBF.N_NAME);
        String streetType = (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT);
        String streetCode = (String) actualPayment.getField(ActualPaymentDBF.VUL_CODE);
        String street = (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME);
        String buildingNumber = (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM);
        String buildingCorp = (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM);

        switch (entity) {
            case CITY: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
                if (cityCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction cityCorrection = addressCorrectionBean.createCityCorrection(city, cityId, organizationId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                    if (addressCorrectionBean.checkAddressExistence(cityCorrection)) {
                        throw new DublicateCorrectionException();
                    }
                    addressCorrectionBean.insert(cityCorrection);
                    actualPaymentBean.markCorrected(requestFileId, city);
                }
            }
            break;
            case STREET_TYPE: {
                List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrection(streetType, organizationId);
                if (streetTypeCorrections.size() > 0) {
                    throw new DublicateCorrectionException();
                } else {
                    Correction streetTypeCorrection = addressCorrectionBean.createStreetTypeCorrection(streetType, streetTypeId, organizationId,
                            IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                    if (addressCorrectionBean.checkAddressExistence(streetTypeCorrection)) {
                        throw new DublicateCorrectionException();
                    }
                    addressCorrectionBean.insert(streetTypeCorrection);
                    actualPaymentBean.markCorrected(requestFileId, city, streetType);
                }
            }
            break;
            case STREET: {
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrection(streetType, organizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), null, streetCode, organizationId);
                        if (streetCorrections.size() > 0) {
                            throw new DublicateCorrectionException();
                        } else {
                            StreetCorrection streetCorrection = addressCorrectionBean.createStreetCorrection(street, streetCode,
                                    streetTypeCorrection.getId(), cityCorrection.getId(), streetId, organizationId,
                                    IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                            if (addressCorrectionBean.checkStreetExistence(streetCorrection)) {
                                throw new DublicateCorrectionException();
                            }
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
                List<Correction> cityCorrections = addressCorrectionBean.findCityLocalCorrections(city, organizationId);
                if (cityCorrections.size() == 1) {
                    Correction cityCorrection = cityCorrections.get(0);

                    List<Correction> streetTypeCorrections = addressCorrectionBean.findStreetTypeLocalCorrection(streetType, organizationId);
                    if (streetTypeCorrections.size() == 1) {
                        Correction streetTypeCorrection = streetTypeCorrections.get(0);

                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetLocalCorrections(cityCorrection.getId(),
                                streetTypeCorrection.getId(), null, streetCode, organizationId);
                        if (streetCorrections.size() == 1) {
                            StreetCorrection streetCorrection = streetCorrections.get(0);

                            List<BuildingCorrection> buildingCorrections = addressCorrectionBean.findBuildingLocalCorrections(streetId,
                                    buildingNumber, buildingCorp, organizationId);
                            if (buildingCorrections.size() > 0) {
                                throw new DublicateCorrectionException();
                            } else {
                                BuildingCorrection buildingCorrection = addressCorrectionBean.createBuildingCorrection(buildingNumber, buildingCorp,
                                        streetCorrection.getId(), buildingId, organizationId, IOsznOrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                                if (addressCorrectionBean.checkBuildingExistence(buildingCorrection)) {
                                    throw new DublicateCorrectionException();
                                }
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
}
