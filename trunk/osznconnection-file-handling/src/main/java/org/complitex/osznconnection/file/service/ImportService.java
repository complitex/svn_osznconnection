package org.complitex.osznconnection.file.service;

import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImportService;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.exception.*;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.CorrectionImportFile;
import org.complitex.osznconnection.ownership.entity.OwnershipImportFile;
import org.complitex.osznconnection.ownership.service.OwnershipImportService;
import org.complitex.osznconnection.privilege.entity.PrivilegeImportFile;
import org.complitex.osznconnection.privilege.service.PrivilegeImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.*;
import java.util.*;

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
    private ConfigBean configBean;

    @EJB
    private LogBean logBean;

    private boolean processing;
    private boolean error;
    private boolean success;

    private String errorMessage;

    private Map<IImportFile, ImportMessage> dictionaryMap = new LinkedHashMap<IImportFile, ImportMessage>();

    private Map<IImportFile, ImportMessage> correctionMap = new LinkedHashMap<IImportFile, ImportMessage>();

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

    public List<ImportMessage> getDictionaryMessages(){
        return Collections.unmodifiableList(new ArrayList<ImportMessage>(dictionaryMap.values()));
    }

    public ImportMessage getDictionaryMessage(IImportFile importFile){
        return dictionaryMap.get(importFile);
    }

    public List<ImportMessage> getCorrectionMessages(){
        return Collections.unmodifiableList(new ArrayList<ImportMessage>(correctionMap.values()));
    }

    public ImportMessage getCorrectionMessage(IImportFile importFile){
        return correctionMap.get(importFile);
    }

    private void init(){
        dictionaryMap.clear();
        correctionMap.clear();
        processing = true;
        error = false;
        success = false;
        errorMessage = null;
    }

    @Asynchronous
    public void processDictionary(){
        if (processing){
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            userTransaction.begin();

            //Address
            addressImportService.process(dictionaryListener);

            //Ownership
            ownershipImportService.process(dictionaryListener);

            //Privilege
            privilegeImportService.process(dictionaryListener);

            success = true;

            userTransaction.commit();
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
        }finally {
            processing = false;
        }
    }

    @Asynchronous
    public void processCorrections(Long organizationId){
        if (processing){
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            userTransaction.begin();

            //Address correction
            addressCorrectionImportService.process(organizationId, INT_ORG_ID, correctionListener);

            //Ownership correction
            ownershipCorrectionImportService.process(organizationId, INT_ORG_ID, correctionListener);

            //Privilege correction
            privilegeCorrectionImportService.process(organizationId, INT_ORG_ID, correctionListener);

            success = true;

            userTransaction.commit();
        } catch (Exception e) {
            log.error("Ошибка импорта", e);

            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Ошибка отката транзакции", e1);
            }

            error = true;
            errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();
        }finally {
            processing = false;
        }
    }

    private <T extends IImportFile> void processDictionary(T importFile) throws SystemException,
            NotSupportedException, ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException,
            RollbackException, HeuristicRollbackException, HeuristicMixedException {
        userTransaction.begin();

        if (importFile instanceof AddressImportFile){ //Address
            switch ((AddressImportFile)importFile){
                case COUNTRY:
                    addressImportService.importCountry(dictionaryListener);
                    break;
                case REGION:
                    addressImportService.importRegion(dictionaryListener);
                    break;
                case CITY_TYPE:
                    addressImportService.importCityType(dictionaryListener);
                    break;
                case CITY:
                    addressImportService.importCity(dictionaryListener);
                    break;
                case DISTRICT:
                    addressImportService.importDistrict(dictionaryListener);
                    break;
                case STREET_TYPE:
                    addressImportService.importStreetType(dictionaryListener);
                    break;
                case STREET:
                    addressImportService.importStreet(dictionaryListener);
                    break;
                case BUILDING:
                    addressImportService.importBuilding(dictionaryListener);
                    break;
            }
        }else if (importFile instanceof OwnershipImportFile){ // Ownership
            ownershipImportService.process(dictionaryListener);
        }else if (importFile instanceof PrivilegeImportFile){ //Privilege
            privilegeImportService.process(dictionaryListener);
        }

        success = true;

        userTransaction.commit();
    }

    private <T extends IImportFile> void processCorrection(T importFile, long orgId) throws SystemException,
            NotSupportedException, ImportFileNotFoundException, ImportObjectLinkException, ImportFileReadException,
            RollbackException, HeuristicRollbackException, HeuristicMixedException {

        userTransaction.begin();

        if (importFile instanceof AddressImportFile){ //Address
            switch ((AddressImportFile) importFile){
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
                    addressCorrectionImportService.importBuildingToCorrection(orgId, INT_ORG_ID,  correctionListener);
                    break;
            }
        }else if (importFile instanceof CorrectionImportFile){ //Correction
            switch ((CorrectionImportFile)importFile){
                case OWNERSHIP_CORRECTION:
                    ownershipCorrectionImportService.process(orgId, INT_ORG_ID, correctionListener);
                    break;
                case PRIVILEGE_CORRECTION:
                    privilegeCorrectionImportService.process(orgId, INT_ORG_ID, correctionListener);
                    break;
            }
        }

        success = true;

        userTransaction.commit();
    }

    public <T extends IImportFile> void process(List<T> dictionaryFiles, List<T> correctionFiles, Long orgId){
        if (processing){
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            //Dictionary
            for(T t : dictionaryFiles){
                processDictionary(t);
            }

            //Correction
            for (T t : correctionFiles){
                processCorrection(t, orgId);
            }
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
        }finally {
            processing = false;
        }
    }
}
