package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.entity.AddressEntity;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.*;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.correction.service.exception.DuplicateCorrectionException;
import org.complitex.correction.service.exception.MoreOneCorrectionException;
import org.complitex.correction.service.exception.NotFoundCorrectionException;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.complitex.dictionary.util.StringUtil.removeWhiteSpaces;
import static org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy.MODULE_ID;

@Stateless(name = "OsznAddressService")
public class AddressService extends AbstractBean {
    private final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private BuildingStrategy buildingStrategy;

    @EJB
    private ServiceProviderAdapter adapter;

    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    @EJB
    private OsznOrganizationStrategy organizationStrategy;

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
    private void resolveLocalAddress(AbstractAddressRequest request){
        Long osznId = request.getOrganizationId();
        Long userOrganizationId = request.getUserOrganizationId();

        //Связывание города
        List<CityCorrection> cityCorrections = addressCorrectionBean.getCityCorrections(null, request.getCity(),
                osznId, userOrganizationId);

        if (cityCorrections.size() == 1) {
            CityCorrection cityCorrection = cityCorrections.get(0);
            request.setInternalCityId(cityCorrection.getObjectId());
        } else if (cityCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY_CORRECTION);

            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.getCityObjectIds(request.getCity());

            if (cityIds.size() == 1) {
                request.setInternalCityId(cityIds.get(0));
            } else if (cityIds.size() > 1) {
                request.setStatus(RequestStatus.MORE_ONE_LOCAL_CITY);

                return;
            } else {
                request.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);

                return;
            }
        }

        //Связывание типа улицы
        if(request.getStreetType() != null){
            List<StreetTypeCorrection> streetTypeCorrections = addressCorrectionBean.getStreetTypeCorrections(null,
                    request.getStreetType(), osznId, userOrganizationId);

            if (streetTypeCorrections.size() == 1) {
                request.setInternalStreetTypeId(streetTypeCorrections.get(0).getObjectId());
            } else if (streetTypeCorrections.size() > 1) {
                request.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE_CORRECTION);

                return;
            } else {
                List<Long> streetTypeIds = addressCorrectionBean.getStreetTypeObjectIds(request.getStreetType());

                if (streetTypeIds.size() == 1) {
                    request.setInternalStreetTypeId(streetTypeIds.get(0));
                } else if (streetTypeIds.size() > 1) {
                    request.setStatus(RequestStatus.MORE_ONE_LOCAL_STREET_TYPE);

                    return;
                } else {
                    request.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED_LOCALLY);

                    return;
                }
            }
        }

        //Связывание улицы
        List<StreetCorrection> streetCorrections = addressCorrectionBean.getStreetCorrections(request.getInternalCityId(),
                request.getInternalStreetTypeId(), request.getStreetCode(), null,  request.getStreet(),
                osznId, userOrganizationId);

        if (streetCorrections.size() == 1){
            StreetCorrection streetCorrection = streetCorrections.get(0);

            request.setInternalCityId(streetCorrection.getCityObjectId());
            request.setInternalStreetTypeId(streetCorrection.getStreetTypeObjectId());
            request.setInternalStreetId(streetCorrection.getObjectId());
        }else if (streetCorrections.size() > 1) {
            //сформируем множество названий
            Set<String> streetNames = Sets.newHashSet();

            for (StreetCorrection sc : streetCorrections) {
                String streetName = streetStrategy.getName(sc.getObjectId());

                if (!Strings.isEmpty(streetName)) {
                    streetNames.add(streetName);
                }
            }

            if (streetNames.size() == 1) { //нашли внутренее название улицы
                String streetName = Lists.newArrayList(streetNames).get(0);

                //находим ids улиц по внутреннему названию
                List<Long> streetIds = streetStrategy.getStreetObjectIds(request.getInternalCityId(),
                        request.getInternalStreetTypeId(), streetName);

                if (streetIds.size() == 1) { //нашли ровно одну улицу
                    Long streetObjectId = streetIds.get(0);
                    request.setInternalStreetId(streetObjectId);

                    DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                    request.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));

                    //перейти к обработке дома
                } else if (streetIds.size() > 1) { // нашли больше одной улицы
                    //пытаемся найти по району
                    streetIds = streetStrategy.getStreetObjectIdsByDistrict(request.getInternalCityId(),
                            request.getStreet(), osznId);

                    if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                        Long streetObjectId = streetIds.get(0);
                        request.setInternalStreetId(streetObjectId);


                        DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                        request.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));

                        //перейти к обработке дома
                    } else {
                        // пытаемся искать дополнительно по номеру и корпусу дома
                        streetIds = streetStrategy.getStreetObjectIdsByBuilding(request.getInternalCityId(), streetName,
                                request.getBuildingNumber(), request.getBuildingCorp());

                        if (streetIds.size() == 1) { //нашли ровно одну улицу с заданным номером и корпусом дома
                            Long streetObjectId = streetIds.get(0);
                            request.setInternalStreetId(streetObjectId);

                            DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                            request.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));

                            //проставить дом для payment и выйти
                            List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getInternalCityId(),
                                    streetObjectId,request.getBuildingNumber(),request.getBuildingCorp());

                            if (buildingIds.size() == 1) {
                                request.setInternalBuildingId(buildingIds.get(0));
                            } else {
                                throw new IllegalStateException("Building id was not found.");
                            }
                            request.setStatus(RequestStatus.CITY_UNRESOLVED);

                            return;
                        } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                            request.setStatus(RequestStatus.STREET_AND_BUILDING_UNRESOLVED_LOCALLY);
                            return;
                        }
                    }
                } else {
                    throw new IllegalStateException("Street name `" + streetName + "` was not found.");
                }
            } else {
                throw new IllegalStateException("Street `" + request.getStreet() +
                        "` is mapped to more one internal street objects: " + streetNames);
            }
        } else { // в коррекциях не нашли ни одного соответствия на внутренние объекты улиц
            // ищем по внутреннему справочнику улиц
            List<Long> streetIds = streetStrategy.getStreetObjectIds(request.getInternalCityId(),
                    request.getInternalStreetTypeId(), request.getStreet());

            if (streetIds.size() == 1) { // нашли ровно одну улицу
                Long streetId = streetIds.get(0);
                request.setInternalStreetId(streetId);

                DomainObject streetObject = streetStrategy.findById(streetId, true);
                request.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));

                // перейти к обработке дома
            } else if (streetIds.size() > 1) { // нашли более одной улицы
                //пытаемся найти по району
                streetIds = streetStrategy.getStreetObjectIdsByDistrict(request.getInternalCityId(), request.getStreet(), osznId);

                if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                    Long streetId = streetIds.get(0);
                    request.setInternalStreetId(streetId);

                    DomainObject streetObject = streetStrategy.findById(streetId, true);
                    request.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
                    // перейти к обработке дома
                } else {
                    // пытаемся искать дополнительно по номеру и корпусу дома
                    streetIds = streetStrategy.getStreetObjectIdsByBuilding(request.getInternalCityId(), request.getStreet(),
                            request.getBuildingNumber(), request.getBuildingCorp());

                    if (streetIds.size() == 1) {
                        Long streetId = streetIds.get(0);

                        //проставить дом для payment и выйти
                        List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getInternalCityId(), streetId,
                                request.getBuildingNumber(), request.getBuildingCorp());

                        if (buildingIds.size() == 1) {
                            request.setInternalBuildingId(buildingIds.get(0));

                            request.setInternalStreetId(streetId);
                        } else {
                            throw new IllegalStateException("Building id was not found.");
                        }
                        request.setStatus(RequestStatus.CITY_UNRESOLVED);
                        return;
                    } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                        request.setStatus(RequestStatus.STREET_AND_BUILDING_UNRESOLVED_LOCALLY);
                        return;
                    }
                }
            } else { // не нашли ни одной улицы
                request.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
                return;
            }
        }

        //Связывание дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.getBuildingCorrections(
                request.getInternalStreetId(), null, request.getBuildingNumber(), request.getBuildingCorp(),
                osznId, userOrganizationId);

        if (buildingCorrections.size() == 1) {
            request.setInternalBuildingId(buildingCorrections.get(0).getObjectId());
        } else if (buildingCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING_CORRECTION);
        } else {
            List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getInternalCityId(),
                    request.getInternalStreetId(), request.getBuildingNumber(), request.getBuildingCorp());

            if (buildingIds.size() == 1){
                request.setInternalBuildingId(buildingIds.get(0));
            }else if (buildingIds.size() > 1) {
                request.setStatus(RequestStatus.MORE_ONE_LOCAL_BUILDING);
            } else if (buildingIds.isEmpty()){
                request.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
            }
        }

        //Связанно с внутренней адресной базой
        if (request.getInternalBuildingId() != null){
            request.setStatus(RequestStatus.CITY_UNRESOLVED);
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
    public void resolveOutgoingAddress(AbstractAddressRequest request, CalculationContext calculationContext) {
        Long calcId = calculationContext.getCalculationCenterId();
        Long userOrganizationId = calculationContext.getUserOrganizationId();

        Locale locale = localeBean.getSystemLocale();

        //город
        List<CityCorrection> cityCorrections = addressCorrectionBean.getCityCorrections(request.getInternalCityId(),
                null, calcId, userOrganizationId);

        if (cityCorrections.isEmpty()){
            DomainObject city = cityStrategy.findById(request.getInternalCityId(), true);

            if (city != null){
                request.setOutgoingCity(cityStrategy.getName(city, locale));
            }else {
                request.setStatus(RequestStatus.CITY_UNRESOLVED);

                return;
            }
        } else if (cityCorrections.size() == 1) {
            request.setOutgoingCity(cityCorrections.get(0).getCorrection());
        } else {
            request.setStatus(RequestStatus.MORE_ONE_REMOTE_CITY_CORRECTION);

            return;
        }

        // район
        List<DistrictCorrection> districtCorrections = addressCorrectionBean.getDistrictCorrections(request.getInternalCityId(),
                null,null, null, calcId, userOrganizationId);

        if (districtCorrections.isEmpty()){
            DomainObject organization = organizationStrategy.findById(request.getOrganizationId(), true);

            Long districtId = organization.getAttribute(IOrganizationStrategy.DISTRICT).getValueId();
            DomainObject district = districtStrategy.findById(districtId, true);

            if (district != null){
                request.setOutgoingDistrict(districtStrategy.displayDomainObject(district, locale));
            }else {
                request.setStatus(RequestStatus.DISTRICT_UNRESOLVED);

                return;
            }

        } else if (districtCorrections.size() == 1) {
            request.setOutgoingDistrict(districtCorrections.get(0).getCorrection());
        } else if (districtCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_REMOTE_DISTRICT_CORRECTION);

            return;
        }

        //тип улицы
        if (request.getInternalStreetTypeId() != null) {
            List<StreetTypeCorrection> streetTypeCorrections = addressCorrectionBean.getStreetTypeCorrections(
                    request.getInternalStreetTypeId(), null, calcId, userOrganizationId);

            if (streetTypeCorrections.isEmpty()){
                DomainObject streetType = streetTypeStrategy.findById(request.getInternalStreetTypeId(), true);

                if (streetType != null){
                    request.setOutgoingStreetType(streetTypeStrategy.getShortName(streetType, locale));
                }else{
                    request.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);

                    return;
                }
            }else if (streetTypeCorrections.size() == 1){
                request.setOutgoingStreetType(streetTypeCorrections.get(0).getCorrection());
            }else {
                request.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION); //todo add status

                return;
            }
        }

        //улица
        List<StreetCorrection> streetCorrections = addressCorrectionBean.getStreetCorrections(null,
                request.getInternalStreetId(), null, null, null, calcId, userOrganizationId);

        if (streetCorrections.isEmpty()){
            DomainObject street = streetStrategy.findById(request.getInternalStreetId(), true);

            if (street != null){
                request.setOutgoingStreet(streetStrategy.getName(street, locale));
            }else {
                request.setStatus(RequestStatus.STREET_UNRESOLVED);

                return;
            }
        } else if (streetCorrections.size() == 1) {
            request.setOutgoingStreet(streetCorrections.get(0).getCorrection());
        } else {
            streetCorrections = addressCorrectionBean.getStreetCorrectionsByBuilding(request.getInternalStreetId(),
                    request.getInternalBuildingId(), calcId);

            if (streetCorrections.size() == 1) {
                request.setOutgoingStreet(streetCorrections.get(0).getCorrection());
            } else {
                request.setStatus(RequestStatus.MORE_ONE_REMOTE_STREET_CORRECTION);

                return;
            }
        }

        //дом
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.getBuildingCorrections(null,
                request.getInternalBuildingId(), null, null, calculationContext.getCalculationCenterId(), null);

        if (buildingCorrections.isEmpty()){
            Building building = buildingStrategy.findById(request.getInternalBuildingId(), true);

            if (building != null){
                request.setOutgoingBuildingNumber(building.getAccompaniedNumber(locale));
                request.setOutgoingBuildingCorp(building.getAccompaniedCorp(locale));
            }else {
                request.setStatus(RequestStatus.BUILDING_UNRESOLVED);

                return;
            }
        }else  if(buildingCorrections.size() == 1) {
            BuildingCorrection buildingCorrection = buildingCorrections.get(0);
            request.setOutgoingBuildingNumber(buildingCorrection.getCorrection());
            request.setOutgoingBuildingCorp(buildingCorrection.getCorrectionCorp());
        } else if (buildingCorrections.size() > 1) {
            request.setStatus(RequestStatus.MORE_ONE_REMOTE_BUILDING_CORRECTION);

            return;
        }

        //квартира
        request.setOutgoingApartment(removeWhiteSpaces(request.getApartment()));

        request.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     */
    @Transactional
    public void resolveAddress(Payment payment, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(payment);
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (payment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(payment, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(ActualPayment actualPayment, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(actualPayment);

        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (actualPayment.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(actualPayment, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(Subsidy subsidy, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(subsidy);

        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (subsidy.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(subsidy, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(dwellingCharacteristics);

        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (dwellingCharacteristics.getStatus().isAddressResolvedLocally()) {
            resolveOutgoingAddress(dwellingCharacteristics, calculationContext);
        }
    }

    @Transactional
    public void resolveAddress(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        //разрешить адрес локально
        resolveLocalAddress(facilityServiceType);

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
     * @param request AbstractAccountRequest
     * @param cityObjectId Откорректированный город
     * @param streetObjectId Откорректированная улица
     * @param streetTypeObjectId Откорректированный тип улицы
     * @param buildingObjectId Откорректированный дом
     */
    @Transactional
    public void correctLocalAddress(AbstractAccountRequest request, AddressEntity entity, Long cityObjectId,
                                    Long streetTypeObjectId, Long streetObjectId, Long buildingObjectId,
                                    Long userOrganizationId)
            throws DuplicateCorrectionException, MoreOneCorrectionException, NotFoundCorrectionException {
        Long osznId = request.getOrganizationId();

        switch (entity) {
            case CITY: {
                List<CityCorrection> cityCorrections = addressCorrectionBean.getCityCorrections(null, request.getCity(),
                        osznId, userOrganizationId);

                if (cityCorrections.isEmpty()) {
                    CityCorrection cityCorrection = new CityCorrection(null, cityObjectId, request.getCity().toUpperCase(),
                            osznId, userOrganizationId, MODULE_ID);
                    addressCorrectionBean.save(cityCorrection);
                } else {
                    throw new DuplicateCorrectionException();
                }
            }
            break;

            case STREET_TYPE: {
                List<StreetTypeCorrection> streetTypeCorrections = addressCorrectionBean.getStreetTypeCorrections(
                        null, request.getStreetType(), osznId, userOrganizationId);

                if (streetTypeCorrections.isEmpty()) {
                    StreetTypeCorrection streetTypeCorrection = new StreetTypeCorrection(request.getStreetTypeCode(),
                            streetTypeObjectId,
                            request.getStreetType().toUpperCase(),
                            osznId, userOrganizationId, MODULE_ID);
                    addressCorrectionBean.save(streetTypeCorrection);
                } else {
                    throw new DuplicateCorrectionException();
                }
            }
            break;

            case STREET:
                Long streetTypeId = request.getInternalStreetTypeId() != null
                        ? request.getInternalStreetTypeId() : streetTypeObjectId;

                List<StreetCorrection> streetCorrections = addressCorrectionBean.getStreetCorrections(
                        request.getInternalCityId(), streetTypeId, null, null, request.getStreet(), osznId, userOrganizationId);

                if (streetCorrections.isEmpty()) {
                    StreetCorrection streetCorrection = new StreetCorrection(request.getInternalCityId(), streetTypeId,
                            request.getStreetCode(), streetObjectId, request.getStreet().toUpperCase(),
                            osznId, userOrganizationId, MODULE_ID);

                    addressCorrectionBean.save(streetCorrection);
                } else {
                    throw new DuplicateCorrectionException();
                }

                break;

            case BUILDING:
                List<BuildingCorrection> buildingCorrections = addressCorrectionBean.getBuildingCorrections(
                        request.getInternalStreetId(), null, request.getBuildingNumber(), request.getBuildingCorp(),
                        osznId, userOrganizationId);

                if (buildingCorrections.isEmpty()) {
                    BuildingCorrection buildingCorrection = new BuildingCorrection(request.getInternalStreetId(), null,
                            buildingObjectId,
                            request.getBuildingNumber().toUpperCase(),
                            request.getBuildingCorp() != null ? request.getBuildingCorp().toUpperCase() : null,
                            osznId, userOrganizationId, MODULE_ID);

                    addressCorrectionBean.save(buildingCorrection);
                } else {
                    throw new DuplicateCorrectionException();
                }

                break;
        }
    }
}
