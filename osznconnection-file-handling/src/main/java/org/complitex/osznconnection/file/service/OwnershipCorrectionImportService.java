package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.service.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.complitex.osznconnection.file.entity.OwnershipCorrection;
import org.complitex.osznconnection.file.strategy.OwnershipStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

import static org.complitex.osznconnection.file.entity.CorrectionImportFile.OWNERSHIP_CORRECTION;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 15:22
 */
@Stateless
public class OwnershipCorrectionImportService extends AbstractImportService {

    private final Logger log = LoggerFactory.getLogger(OwnershipCorrectionImportService.class);
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
                Long objectId = ownershipStrategy.getObjectId(line[1].trim());
                if (objectId == null) {
                    throw new ImportObjectLinkException(OWNERSHIP_CORRECTION.getFileName(), recordIndex, line[1]);
                }

                ownershipCorrectionBean.save(new OwnershipCorrection(line[2].trim(), objectId, line[3].trim(), orgId,
                        null, intOrgId));

                listener.recordProcessed(OWNERSHIP_CORRECTION, recordIndex);
            }

            listener.completeImport(OWNERSHIP_CORRECTION, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
