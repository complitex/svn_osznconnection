package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.service.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.complitex.osznconnection.file.entity.PrivilegeCorrection;
import org.complitex.osznconnection.file.strategy.PrivilegeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

import static org.complitex.osznconnection.file.entity.CorrectionImportFile.PRIVILEGE_CORRECTION;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 15:47
 */
@Stateless
public class PrivilegeCorrectionImportService extends AbstractImportService {

    private final Logger log = LoggerFactory.getLogger(PrivilegeCorrectionImportService.class);
    @EJB
    private PrivilegeStrategy privilegeStrategy;
    @EJB
    private PrivilegeCorrectionBean privilegeCorrectionBean;

    /**
     * C_PRIVILEGE_ID	PRIVILEGE_ID	Код	Название привилегии
     * @param listener
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     * @throws org.complitex.dictionary.service.exception.ImportObjectLinkException
     */
    public void process(long orgId, long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(PRIVILEGE_CORRECTION, getRecordCount(PRIVILEGE_CORRECTION));

        CSVReader reader = getCsvReader(PRIVILEGE_CORRECTION);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //PRIVILEGE_ID
                Long objectId = privilegeStrategy.getObjectId(line[1].trim());
                if (objectId == null) {
                    throw new ImportObjectLinkException(PRIVILEGE_CORRECTION.getFileName(), recordIndex, line[1].trim());
                }

                privilegeCorrectionBean.save(new PrivilegeCorrection(line[2].trim(), objectId, line[3].trim(), orgId,
                        intOrgId, null));

                listener.recordProcessed(PRIVILEGE_CORRECTION, recordIndex);
            }

            listener.completeImport(PRIVILEGE_CORRECTION, recordIndex);
        } catch (IOException | NumberFormatException e) {
            throw new ImportFileReadException(e, PRIVILEGE_CORRECTION.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}
