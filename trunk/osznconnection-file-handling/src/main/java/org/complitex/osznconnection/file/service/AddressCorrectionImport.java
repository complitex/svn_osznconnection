package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImportStorage;
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

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.02.11 16:08
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AddressCorrectionImport extends AbstractBean{
    private final static Logger log = LoggerFactory.getLogger(AddressCorrectionImport.class);

    @Resource
    private UserTransaction userTransaction;

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

    public void process(){
        try {
            //todo add files validation

            userTransaction.begin();

            importCity(1L, 1L);
            importDistrict(1L, 1L);
            importStreetType(1L, 1L);
            importStreet(1L, 1L);
            importBuilding(1L, 1L);

            userTransaction.commit();
        } catch (Exception e) {
            log.error("Ошибка импорта", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Ошибка отката транзакции", e);
            }
        }
    }

    /**
     * CITY_ID	REGION_ID	CITY_TYPE_ID	Название населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    private void importCity(Long orgId, Long intOrgId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.CITY);

        try {
            String[] line;

            reader.readNext(); //Skip column names line

            while ((line = reader.readNext()) != null) {
                //CITY_ID
                Long objectId = cityStrategy.getObjectId(Long.parseLong(line[0]));
                if (objectId == null){
                    throw new ImportObjectLinkException();
                }

                addressCorrectionBean.insert(addressCorrectionBean.createCityCorrection(
                        line[3], objectId, orgId, intOrgId));
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
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
    private void importDistrict(Long orgId, Long intOrgId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.DISTRICT);

        try {
            String[] line;

            reader.readNext(); //Skip column names line

            while ((line = reader.readNext()) != null) {
                //DISTRICT_ID
                Long districtId = districtStrategy.getObjectId(Long.parseLong(line[0]));
                if (districtId == null) {
                    throw new ImportObjectLinkException();
                }

                //CITY_ID
                Long cityId =  cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException();
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null){
                    throw new ImportObjectLinkException();
                }

                addressCorrectionBean.insert(addressCorrectionBean.createDistrictCorrection(
                        line[3], cityCorrectionId, districtId, orgId, intOrgId));
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
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
    private void importStreetType(Long orgId, Long intOrgId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.STREET_TYPE);

        try {
            String[] line;

            reader.readNext(); //Skip column names line

            while ((line = reader.readNext()) != null){
                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[0]));
                if (streetTypeId == null){
                    throw new ImportObjectLinkException();
                }

                addressCorrectionBean.insert(addressCorrectionBean.createStreetTypeCorrection(
                        line[2], streetTypeId, orgId, intOrgId));
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e);
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
    private void importStreet(Long orgId, Long intOrgId) throws ImportFileNotFoundException,
            ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.STREET);

        try {
            String[] line;

            reader.readNext(); //Skip column names line

            while ((line = reader.readNext()) != null) {
                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[0]));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException();
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null){
                    throw new ImportObjectLinkException();
                }

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException();
                }

                //Street Type Correction
                Long streetTypeCorrectionId = addressCorrectionBean.getStreetTypeCorrectionId(streetTypeId, orgId, intOrgId);

                addressCorrectionBean.insertStreet(addressCorrectionBean.createStreetCorrection(
                        line[3], null, streetTypeCorrectionId, cityCorrectionId, streetId, orgId, orgId));
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
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
    private void importBuilding(Long orgId, Long intOrgId) throws ImportFileNotFoundException,
            ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.BUILDING);

        try {
            String[] line;

            reader.readNext(); //Skip column names line

            while ((line = reader.readNext()) != null) {
                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0]));

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetId == null) {
                    throw new ImportObjectLinkException();
                }

                //Street Correction
                Long streetCorrectionId = addressCorrectionBean.getStreetCorrectionId(streetId, orgId, intOrgId);
                if (streetCorrectionId == null) {
                    throw new ImportObjectLinkException();
                }

                addressCorrectionBean.insertBuilding(addressCorrectionBean.createBuildingCorrection(
                        line[3], line[4], streetCorrectionId, buildingId, orgId, intOrgId));
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}