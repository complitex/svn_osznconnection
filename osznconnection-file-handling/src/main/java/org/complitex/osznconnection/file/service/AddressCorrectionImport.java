package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.service.AddressImportStorage;
import org.complitex.address.service.IAddressImportListener;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

import static org.complitex.address.entity.AddressImportFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.02.11 16:08
 */

@Stateless
public class AddressCorrectionImport extends AbstractBean{
    private final static Logger log = LoggerFactory.getLogger(AddressCorrectionImport.class);

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

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
    private BuildingAddressStrategy buildingAddressStrategy;

    private AddressImportStorage storage = AddressImportStorage.getInstance();

    public void process(long organizationId, long internalOrganizationId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportObjectLinkException, ImportFileReadException {
        importCity(organizationId, internalOrganizationId, listener);
        importDistrict(organizationId, internalOrganizationId, listener);
        importStreetType(organizationId, internalOrganizationId, listener);
        importStreet(organizationId, internalOrganizationId, listener);
        importBuilding(organizationId, internalOrganizationId, listener);
    }

    /**
     * CITY_ID	REGION_ID	CITY_TYPE_ID	Название населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importCity(Long orgId, Long intOrgId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, storage.getRecordCount(CITY));

        CSVReader reader = storage.getCsvReader(CITY);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //CITY_ID
                Long objectId = cityStrategy.getObjectId(Long.parseLong(line[0]));
                if (objectId == null){
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insert(addressCorrectionBean.createCityCorrection(
                        line[3], objectId, orgId, intOrgId));

                listener.recordProcessed(CITY, recordIndex);
            }

            listener.completeImport(CITY);
        } catch (IOException e) {
            throw new ImportFileReadException(e, CITY.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, CITY.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * DISTRICT_ID	CITY_ID	Код района	Название района
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importDistrict(Long orgId, Long intOrgId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, storage.getRecordCount(DISTRICT));

        CSVReader reader = storage.getCsvReader(DISTRICT);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //DISTRICT_ID
                Long districtId = districtStrategy.getObjectId(Long.parseLong(line[0]));
                if (districtId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[0]);
                }

                //CITY_ID
                Long cityId =  cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null){
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }

                addressCorrectionBean.insert(addressCorrectionBean.createDistrictCorrection(
                        line[3], cityCorrectionId, districtId, orgId, intOrgId));

                listener.recordProcessed(DISTRICT, recordIndex);
            }

            listener.completeImport(DISTRICT);
        } catch (IOException e) {
            throw new ImportFileReadException(e, DISTRICT.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, DISTRICT.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_TYPE_ID	Короткое наименование	Название типа улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importStreetType(Long orgId, Long intOrgId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(STREET_TYPE, storage.getRecordCount(STREET_TYPE));

        CSVReader reader = storage.getCsvReader(STREET_TYPE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                recordIndex++;

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[0]));
                if (streetTypeId == null){
                    throw new ImportObjectLinkException(STREET_TYPE.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insert(addressCorrectionBean.createStreetTypeCorrection(
                        line[2], streetTypeId, orgId, intOrgId));

                listener.recordProcessed(STREET_TYPE, recordIndex);
            }

            listener.completeImport(STREET_TYPE);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_ID	CITY_ID	STREET_TYPE_ID	Название улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importStreet(Long orgId, Long intOrgId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(STREET, storage.getRecordCount(STREET));

        CSVReader reader = storage.getCsvReader(STREET);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[0]));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null){
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[2]);
                }

                //Street Type Correction
                Long streetTypeCorrectionId = addressCorrectionBean.getStreetTypeCorrectionId(streetTypeId, orgId, intOrgId);

                addressCorrectionBean.insertStreet(addressCorrectionBean.createStreetCorrection(
                        line[3], null, streetTypeCorrectionId, cityCorrectionId, streetId, orgId, intOrgId));

                listener.recordProcessed(STREET, recordIndex);
            }

            listener.completeImport(STREET);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, STREET.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * BUILDING_ID	DISTRICT_ID	STREET_ID	Номер дома	Корпус	Строение
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importBuilding(Long orgId, Long intOrgId, IAddressImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(BUILDING, storage.getRecordCount(BUILDING));

        CSVReader reader = storage.getCsvReader(BUILDING);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0]));

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }

                //Street Correction
                Long streetCorrectionId = addressCorrectionBean.getStreetCorrectionId(streetId, orgId, intOrgId);
                if (streetCorrectionId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }

                addressCorrectionBean.insertBuilding(addressCorrectionBean.createBuildingCorrection(
                        line[3], line[4], streetCorrectionId, buildingId, orgId, intOrgId));

                listener.recordProcessed(BUILDING, recordIndex);
            }

            listener.completeImport(BUILDING);
        } catch (IOException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}