package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
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
public class AddressCorrectionImportService extends AbstractImportService {

    private final static Logger log = LoggerFactory.getLogger(AddressCorrectionImportService.class);
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

    public void process(long organizationId, long internalOrganizationId, IImportListener listener)
            throws ImportFileNotFoundException, ImportObjectLinkException, ImportFileReadException {
        importCityToCorrection(organizationId, internalOrganizationId, listener);
        importDistrictToCorrection(organizationId, internalOrganizationId, listener);
        importStreetTypeToCorrection(organizationId, internalOrganizationId, listener);
        importStreetToCorrection(organizationId, internalOrganizationId, listener);
        importBuildingToCorrection(organizationId, internalOrganizationId, listener);
    }

    /**
     * CITY_ID	REGION_ID	CITY_TYPE_ID	Название населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCityToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, getRecordCount(CITY));

        CSVReader reader = getCsvReader(CITY);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //CITY_ID
                Long objectId = cityStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (objectId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insertCityCorrection(line[3].trim(), objectId, orgId, intOrgId, null);

                listener.recordProcessed(CITY, recordIndex);
            }

            listener.completeImport(CITY, recordIndex);
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
    public void importDistrictToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, getRecordCount(DISTRICT));

        CSVReader reader = getCsvReader(DISTRICT);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //DISTRICT_ID
                Long districtId = districtStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (districtId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[0]);
                }

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }

                addressCorrectionBean.insertDistrictCorrection(line[3].trim(), cityCorrectionId, districtId, orgId,
                        intOrgId, null);

                listener.recordProcessed(DISTRICT, recordIndex);
            }

            listener.completeImport(DISTRICT, recordIndex);
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
    public void importStreetTypeToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(STREET_TYPE, getRecordCount(STREET_TYPE));

        CSVReader reader = getCsvReader(STREET_TYPE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET_TYPE.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insertStreetTypeCorrection(line[2].trim(), streetTypeId, orgId, intOrgId, null);

                listener.recordProcessed(STREET_TYPE, recordIndex);
            }

            listener.completeImport(STREET_TYPE, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
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
    public void importStreetToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[0].trim()));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[2]);
                }

                //Street Type Correction
                Long streetTypeCorrectionId = addressCorrectionBean.getStreetTypeCorrectionId(streetTypeId, orgId, intOrgId);

                addressCorrectionBean.insertStreetCorrection(line[3].trim(), null, streetTypeCorrectionId,
                        cityCorrectionId, streetId, orgId, intOrgId, null);

                listener.recordProcessed(STREET, recordIndex);
            }

            listener.completeImport(STREET, recordIndex);
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
    public void importBuildingToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(BUILDING, getRecordCount(BUILDING));

        CSVReader reader = getCsvReader(BUILDING);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0]));

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }

                //Street Correction
                Long streetCorrectionId = addressCorrectionBean.getStreetCorrectionId(streetId, orgId, intOrgId);
                if (streetCorrectionId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }

                addressCorrectionBean.insertBuildingCorrection(line[3].trim(), line[4].trim(), streetCorrectionId,
                        buildingId, orgId, intOrgId, null);

                listener.recordProcessed(BUILDING, recordIndex);
            }

            listener.completeImport(BUILDING, recordIndex);
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