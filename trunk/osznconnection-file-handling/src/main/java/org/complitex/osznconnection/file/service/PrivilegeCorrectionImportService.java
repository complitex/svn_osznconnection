package org.complitex.osznconnection.file.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.entity.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.complitex.osznconnection.file.entity.CorrectionImportFile;
import org.complitex.osznconnection.privilege.strategy.PrivilegeStrategy;
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
public class PrivilegeCorrectionImportService extends AbstractImportService{
    private final static Logger log = LoggerFactory.getLogger(PrivilegeCorrectionImportService.class);

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
                Long objectId = privilegeStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (objectId == null){
                    throw new ImportObjectLinkException(PRIVILEGE_CORRECTION.getFileName(), recordIndex, line[1].trim());
                }

                privilegeCorrectionBean.insertOwnershipCorrection(line[2].trim(), line[3].trim(), objectId, orgId, intOrgId);

                listener.recordProcessed(PRIVILEGE_CORRECTION, recordIndex);
            }

            listener.completeImport(PRIVILEGE_CORRECTION);
        } catch (IOException e) {
            throw new ImportFileReadException(e, PRIVILEGE_CORRECTION.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
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

