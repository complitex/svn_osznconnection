package org.complitex.osznconnection.file.service;

import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImportService;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.AbstractException;
import org.complitex.dictionary.service.exception.ImportCriticalException;
import org.complitex.osznconnection.file.entity.CorrectionImportFile;
import org.complitex.osznconnection.ownership.entity.OwnershipImportFile;
import org.complitex.osznconnection.ownership.service.OwnershipImportService;
import org.complitex.osznconnection.privilege.entity.PrivilegeImportFile;
import org.complitex.osznconnection.privilege.service.PrivilegeImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
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

    public static final long INTERNAL_ORGANIZATION_ID = 0L;

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

    private boolean processing;
    private boolean error;
    private boolean success;

    private String errorMessage;

    private Map<IImportFile, ImportMessage> dictionaryMap = new LinkedHashMap<IImportFile, ImportMessage>();

    private Map<IImportFile, ImportMessage> correctionMap = new LinkedHashMap<IImportFile, ImportMessage>();

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

    public List<ImportMessage> getMessages(){
        return Collections.unmodifiableList(new ArrayList<ImportMessage>(dictionaryMap.values()));
    }

    public List<ImportMessage> getCorrectionMessages(){
        return Collections.unmodifiableList(new ArrayList<ImportMessage>(correctionMap.values()));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Asynchronous
    public void process(Long organizationId){
        if (processing){
            return;
        }

        dictionaryMap.clear();
        correctionMap.clear();
        processing = true;
        error = false;
        success = false;
        errorMessage = null;

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            userTransaction.begin();

            //Address
            addressImportService.process(new IImportListener<AddressImportFile>() {

                @Override
                public void beginImport(AddressImportFile importFile, int recordCount) {
                    dictionaryMap.put(importFile, new ImportMessage<AddressImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(AddressImportFile importFile, int recordIndex) {
                    dictionaryMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(AddressImportFile importFile) {}
            });

            //Ownership
            ownershipImportService.process(new IImportListener<OwnershipImportFile>() {
                @Override
                public void beginImport(OwnershipImportFile importFile, int recordCount) {
                    dictionaryMap.put(importFile, new ImportMessage<OwnershipImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(OwnershipImportFile importFile, int recordIndex) {
                    dictionaryMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(OwnershipImportFile importFile) {}
            });

            //Privilege
            privilegeImportService.process(new IImportListener<PrivilegeImportFile>() {
                @Override
                public void beginImport(PrivilegeImportFile importFile, int recordCount) {
                     dictionaryMap.put(importFile, new ImportMessage<PrivilegeImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(PrivilegeImportFile importFile, int recordIndex) {
                     dictionaryMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(PrivilegeImportFile importFile) {}
            });

            //Address correction
            addressCorrectionImportService.process(organizationId, INTERNAL_ORGANIZATION_ID,
                    new IImportListener<AddressImportFile>() {

                @Override
                public void beginImport(AddressImportFile importFile, int recordCount) {
                    correctionMap.put(importFile, new ImportMessage<AddressImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(AddressImportFile importFile, int recordIndex) {
                    correctionMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(AddressImportFile importFile) {}
            });

            //Ownership correction
            ownershipCorrectionImportService.process(organizationId, INTERNAL_ORGANIZATION_ID,
                    new IImportListener<CorrectionImportFile>() {
                @Override
                public void beginImport(CorrectionImportFile importFile, int recordCount) {
                    correctionMap.put(importFile, new ImportMessage<CorrectionImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(CorrectionImportFile importFile, int recordIndex) {
                     correctionMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(CorrectionImportFile importFile) {}
            });

            //Privilege correction
            privilegeCorrectionImportService.process(organizationId, INTERNAL_ORGANIZATION_ID,
                    new IImportListener<CorrectionImportFile>() {
                @Override
                public void beginImport(CorrectionImportFile importFile, int recordCount) {
                    correctionMap.put(importFile, new ImportMessage<CorrectionImportFile>(importFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(CorrectionImportFile importFile, int recordIndex) {
                     correctionMap.get(importFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(CorrectionImportFile importFile) {}
            });

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
}
