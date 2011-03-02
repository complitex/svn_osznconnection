package org.complitex.osznconnection.privilege.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.entity.AbstractImportService;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.osznconnection.privilege.entity.PrivilegeImportFile;
import org.complitex.osznconnection.privilege.strategy.PrivilegeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

import static org.complitex.osznconnection.privilege.entity.PrivilegeImportFile.PRIVILEGE;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 15:15
 */
@Stateless
public class PrivilegeImportService extends AbstractImportService{
    private final static Logger log = LoggerFactory.getLogger(PrivilegeImportService.class);

    @EJB
    private PrivilegeStrategy privilegeStrategy;

    @EJB
    private StringCultureBean stringCultureBean;

    /**
     * PRIVILEGE_ID	Код	Короткое наименование	Название привилегии
     * @param listener
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void process(IImportListener<PrivilegeImportFile> listener)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(PRIVILEGE, getRecordCount(PRIVILEGE));

        CSVReader reader = getCsvReader(PRIVILEGE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                recordIndex++;

                DomainObject domainObject = privilegeStrategy.newInstance();

                //PRIVILEGE_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //Код
                Attribute code = domainObject.getAttribute(PrivilegeStrategy.CODE);
                stringCultureBean.getSystemStringCulture(code.getLocalizedValues()).setValue(line[1].trim());

                //Короткое наименование
                //todo implement in future release

                //Название привилегии
                Attribute name = domainObject.getAttribute(PrivilegeStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                privilegeStrategy.insert(domainObject);

                listener.recordProcessed(PRIVILEGE, recordIndex);
            }

            listener.completeImport(PRIVILEGE);
        } catch (IOException e) {
            throw new ImportFileReadException(e, PRIVILEGE.getFileName(), recordIndex);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e, PRIVILEGE.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}


