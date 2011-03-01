package org.complitex.osznconnection.file.service;

import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImport;
import org.complitex.address.service.IAddressImportListener;
import org.complitex.dictionary.service.exception.AbstractException;
import org.complitex.dictionary.service.exception.ImportCriticalException;
import org.complitex.osznconnection.file.entity.AddressImportMessage;
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
    private AddressImport addressImport;

    @EJB
    private AddressCorrectionImport addressCorrectionImport;

    private boolean processing = false;
    private boolean error = false;

    private String errorMessage;

    private Map<AddressImportFile, AddressImportMessage> addressMap = new LinkedHashMap<AddressImportFile, AddressImportMessage>();

    private Map<AddressImportFile, AddressImportMessage> correctionMap = new LinkedHashMap<AddressImportFile, AddressImportMessage>();

    public boolean isProcessing() {
        return processing;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<AddressImportMessage> getMessages(){
        return Collections.unmodifiableList(new ArrayList<AddressImportMessage>(addressMap.values()));
    }

    public List<AddressImportMessage> getCorrectionMessages(){
        return Collections.unmodifiableList(new ArrayList<AddressImportMessage>(correctionMap.values()));
    }

    @Asynchronous
    public void process(Long organizationId){
        if (processing){
            return;
        }

        addressMap.clear();
        correctionMap.clear();
        processing = true;
        error = false;
        errorMessage = null;

        try {
            userTransaction.begin();

            addressImport.process(new IAddressImportListener() {

                @Override
                public void beginImport(AddressImportFile addressImportFile, int recordCount) {
                    addressMap.put(addressImportFile, new AddressImportMessage(addressImportFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(AddressImportFile addressImportFile, int recordIndex) {
                    addressMap.get(addressImportFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(AddressImportFile addressImportFile) {}
            });

            addressCorrectionImport.process(organizationId, INTERNAL_ORGANIZATION_ID, new IAddressImportListener() {

                @Override
                public void beginImport(AddressImportFile addressImportFile, int recordCount) {
                    correctionMap.put(addressImportFile, new AddressImportMessage(addressImportFile, recordCount, 0));
                }

                @Override
                public void recordProcessed(AddressImportFile addressImportFile, int recordIndex) {
                    correctionMap.get(addressImportFile).setIndex(recordIndex);
                }

                @Override
                public void completeImport(AddressImportFile addressImportFile) {}
            });

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
