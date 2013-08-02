package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.service.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.complitex.osznconnection.file.strategy.OwnershipStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

import static org.complitex.correction.entity.CorrectionImportFile.OWNERSHIP_CORRECTION;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 15:22
 */
@Stateless
public class OwnershipCorrectionImportService extends AbstractImportService {

    private final static Logger log = LoggerFactory.getLogger(OwnershipCorrectionImportService.class);
    @EJB
    private OwnershipStrategy ownershipStrategy;
    @EJB
    private OwnershipCorrectionBean ownershipCorrectionBean;

    /**
     * C_OWNERSHIP_ID	OWNERSHIP_ID	Код	Название формы собственности
     * @param listener
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     * @throws ImportObjectLinkException
     */
    public void process(long orgId, long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(OWNERSHIP_CORRECTION, getRecordCount(OWNERSHIP_CORRECTION));

        CSVReader reader = getCsvReader(OWNERSHIP_CORRECTION);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //OWNERSHIP_ID
                Long objectId = ownershipStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (objectId == null) {
                    throw new ImportObjectLinkException(OWNERSHIP_CORRECTION.getFileName(), recordIndex, line[1]);
                }

                ownershipCorrectionBean.insertOwnershipCorrection(line[2].trim(), line[3].trim(), objectId, orgId,
                        intOrgId, null);

                listener.recordProcessed(OWNERSHIP_CORRECTION, recordIndex);
            }

            listener.completeImport(OWNERSHIP_CORRECTION, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, OWNERSHIP_CORRECTION.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, OWNERSHIP_CORRECTION.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}
