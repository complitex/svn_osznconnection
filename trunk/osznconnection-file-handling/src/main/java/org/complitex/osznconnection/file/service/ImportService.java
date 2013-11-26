package org.complitex.osznconnection.file.service;

import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImportService;
import org.complitex.correction.service.AddressCorrectionImportService;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.exception.*;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.organization.entity.OrganizationImportFile;
import org.complitex.organization.service.OrganizationImportService;
import org.complitex.organization.service.exception.RootOrganizationNotFound;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.CorrectionImportFile;
import org.complitex.osznconnection.file.entity.OwnershipImportFile;
import org.complitex.osznconnection.file.entity.PrivilegeImportFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 18:05
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class ImportService {

    private final static Logger log = LoggerFactory.getLogger(ImportService.class);
    public static final long INT_ORG_ID = 0L;

    @Resource
    private UserTransaction userTransaction;

    @EJB
    private AddressImportService addressImportService;

    @EJB
    private AddressCorrectionImportService addressCorrectionImportService;

    @EJB
    private OwnershipImportService ownershipImportService;

    @EJB
    private PrivilegeImportService privilegeImportService;

    @EJB
    private OwnershipCorrectionImportService ownershipCorrectionImportService;

    @EJB
    private PrivilegeCorrectionImportService privilegeCorrectionImportService;

    @EJB
    private OrganizationImportService organizationImportService;

    @EJB
    private ConfigBean configBean;

    @EJB
    private LogBean logBean;

    private boolean processing;
    private boolean error;
    private boolean success;
    private String errorMessage;
    private Map<IImportFile, ImportMessage> dictionaryMap = new LinkedHashMap<>();
    private Map<IImportFile, ImportMessage> correctionMap = new LinkedHashMap<>();
    private IImportListener dictionaryListener = new IImportListener() {

        @Override
        public void beginImport(IImportFile importFile, int recordCount) {
            dictionaryMap.put(importFile, new ImportMessage(importFile, recordCount, 0));
        }

        @Override
        public void recordProcessed(IImportFile importFile, int recordIndex) {
            dictionaryMap.get(importFile).setIndex(recordIndex);
        }

        @Override
        public void completeImport(IImportFile importFile, int recordCount) {
            logBean.info(Module.NAME, ImportService.class, importFile.getClass(), null, Log.EVENT.CREATE,
                    "Имя файла: {0}, количество записей: {1}", importFile.getFileName(), recordCount);
        }

        @Override
        public void warn(IImportFile importFile, String message) {
        }
    };
    private IImportListener correctionListener = new IImportListener() {

        @Override
        public void beginImport(IImportFile importFile, int recordCount) {
            correctionMap.put(importFile, new ImportMessage(importFile, recordCount, 0));
        }

        @Override
        public void recordProcessed(IImportFile importFile, int recordIndex) {
            correctionMap.get(importFile).setIndex(recordIndex);
        }

        @Override
        public void completeImport(IImportFile importFile, int recordCount) {
            logBean.info(Module.NAME, ImportService.class, importFile.getClass(), null, Log.EVENT.CREATE,
                    "Имя файла: {0}, количество записей: {1}", importFile.getFileName(), recordCount);
        }

        @Override
        public void warn(IImportFile importFile, String message) {
        }
    };

    public boolean isProcessing() {
        return processing;
    }

    public boolean isError() {
        return error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ImportMessage getDictionaryMessage(IImportFile importFile) {
        return dictionaryMap.get(importFile);
    }

    public ImportMessage getCorrectionMessage(IImportFile importFile) {
        return correctionMap.get(importFile);
    }

    private void init() {
        dictionaryMap.clear();
        correctionMap.clear();
        processing = true;
        error = false;
        success = false;
        errorMessage = null;
    }

    private <T extends IImportFile> void processDictionary(T importFile, long localeId) throws ImportFileNotFoundException,
            ImportObjectLinkException, ImportFileReadException, ImportDuplicateException, RootOrganizationNotFound {
        if (importFile instanceof AddressImportFile) { //Address
            addressImportService.process(importFile, dictionaryListener, localeId, DateUtil.getCurrentDate());
        } else if (importFile instanceof OwnershipImportFile) { // Ownership
            ownershipImportService.process(dictionaryListener);
        } else if (importFile instanceof PrivilegeImportFile) { //Privilege
            privilegeImportService.process(dictionaryListener);
        } else if (importFile instanceof OrganizationImportFile){ //Organization
            organizationImportService.process(dictionaryListener, localeId, DateUtil.getCurrentDate());
        }
    }

    private <T extends IImportFile> void processCorrection(T importFile, long orgId) throws ImportFileNotFoundException,
            ImportObjectLinkException, ImportFileReadException {
        if (importFile instanceof AddressImportFile) { //Address
            switch ((AddressImportFile) importFile) {
                case CITY:
                    addressCorrectionImportService.importCityToCorrection(orgId, INT_ORG_ID, correctionListener);
                    break;
                case DISTRICT:
                    addressCorrectionImportService.importDistrictToCorrection(orgId, INT_ORG_ID, correctionListener);
                    break;
                case STREET_TYPE:
                    addressCorrectionImportService.importStreetTypeToCorrection(orgId, INT_ORG_ID, correctionListener);
                    break;
                case STREET:
                    addressCorrectionImportService.importStreetToCorrection(orgId, INT_ORG_ID, correctionListener);
                    break;
                case BUILDING:
                    addressCorrectionImportService.importBuildingToCorrection(orgId, INT_ORG_ID, correctionListener);
                    break;
            }
        } else if (importFile instanceof CorrectionImportFile) { //Correction
            switch ((CorrectionImportFile) importFile) {
                case OWNERSHIP_CORRECTION:
                    ownershipCorrectionImportService.process(orgId, INT_ORG_ID, correctionListener);
                    break;
                case PRIVILEGE_CORRECTION:
                    privilegeCorrectionImportService.process(orgId, INT_ORG_ID, correctionListener);
                    break;
            }
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> dictionaryFiles, List<T> correctionFiles, Long orgId, long localeId) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            //Dictionary
            for (T t : dictionaryFiles) {
                userTransaction.begin();

                processDictionary(t, localeId);

                userTransaction.commit();
            }

            //Correction
            for (T t : correctionFiles) {
                userTransaction.begin();

                processCorrection(t, orgId);

                userTransaction.commit();
            }

            success = true;
        } catch (Exception e) {
            log.error("Ошибка импорта", e);

            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Ошибка отката транзакции", e1);
            }

            error = true;
            errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();

            logBean.error(Module.NAME, ImportService.class, null, null, Log.EVENT.CREATE, errorMessage);
        } finally {
            processing = false;
        }
    }
}
