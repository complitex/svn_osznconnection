/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.FacilityServiceTypeBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityServiceTypeBindTaskBean implements ITaskBean {
    private static final Logger log = LoggerFactory.getLogger(FacilityServiceTypeBindTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB
    protected ConfigBean configBean;

    @EJB
    private AddressService addressService;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;

    @EJB
    private RequestFileBean requestFileBean;

    private void resolveStreet(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveLocalStreet(facilityServiceType, calculationContext.getUserOrganizationId());

        if (log.isDebugEnabled()) {
            log.debug("Resolving of facility service type street (id = {}) took {} sec.", facilityServiceType.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveAddress(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveAddress(facilityServiceType, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of facility service type address (id = {}) took {} sec.", facilityServiceType.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return facilityServiceType.getStatus().isAddressResolved();
    }

    private void resolveLocalAccount(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveLocalAccount(facilityServiceType, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of facility service type (id = {}) for local account took {} sec.",
                    facilityServiceType.getId(), (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveRemoteAccountNumber(FacilityServiceType facilityServiceType,
            CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveRemoteAccount(facilityServiceType, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of facility service type (id = {}) for remote account number took {} sec.",
                    facilityServiceType.getId(), (System.nanoTime() - startTime) / 1000000000F);
        }
        return facilityServiceType.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(FacilityServiceType facilityServiceType, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        //связать до улицы.
        resolveStreet(facilityServiceType, calculationContext);

        if (facilityServiceType.getInternalStreetId() != null) { // улица найдена
            //resolve local account.
            resolveLocalAccount(facilityServiceType, calculationContext);
            if (facilityServiceType.getStatus() != RequestStatus.ACCOUNT_NUMBER_RESOLVED
                    && facilityServiceType.getStatus() != RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY) {
                if (resolveAddress(facilityServiceType, calculationContext)) {
                    resolveRemoteAccountNumber(facilityServiceType, calculationContext, updatePuAccount);
                }
            }
        }

        // обновляем facility service type запись
        {
            long startTime = 0;
            if (log.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            facilityServiceTypeBean.update(facilityServiceType);
            if (log.isDebugEnabled()) {
                log.debug("Updating of facility service type (id = {}) took {} sec.", facilityServiceType.getId(),
                        (System.nanoTime() - startTime) / 1000000000F);
            }
        }
    }

    private void bindFacilityServiceTypeFile(RequestFile facilityServiceTypeFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла facility service type и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedFacilityServiceTypeIds = facilityServiceTypeBean.findIdsForBinding(facilityServiceTypeFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedFacilityServiceTypeIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedFacilityServiceTypeIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedFacilityServiceTypeIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<FacilityServiceType> facilityServiceTypes =
                    facilityServiceTypeBean.findForOperation(facilityServiceTypeFile.getId(), batch);
            for (FacilityServiceType facilityServiceType : facilityServiceTypes) {
                if (facilityServiceTypeFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать dwelling characteristics запись
                try {
                    userTransaction.begin();
                    bind(facilityServiceType, calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The facility service type item ( id = " + facilityServiceType.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding facility service type item.", e1);
                    }
                }
            }
        }
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах команды опцию "Переписывать номер л/с ПУ номером л/с МН"
        final Boolean updatePuAccount = commandParameters.containsKey(GlobalOptions.UPDATE_PU_ACCOUNT)
                ? (Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT) : false;

        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new BindException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.BINDING);
        requestFileBean.save(requestFile);

        //получаем информацию о текущем контексте вычислений 
        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(requestFile.getUserOrganizationId());

        facilityServiceTypeBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла facility service type
        try {
            bindFacilityServiceTypeFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в facility service type файле связались
        if (!facilityServiceTypeBean.isFacilityServiceTypeFileBound(requestFile.getId())) {
            throw new BindException(true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.BOUND);
        requestFileBean.save(requestFile);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFile.setStatus(RequestFileStatus.BIND_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return FacilityServiceTypeBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
