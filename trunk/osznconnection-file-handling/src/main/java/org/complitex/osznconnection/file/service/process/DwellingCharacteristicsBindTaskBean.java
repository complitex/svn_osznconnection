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
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
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
import org.complitex.osznconnection.file.service.DwellingCharacteristicsBean;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DwellingCharacteristicsBindTaskBean implements ITaskBean {

    private static final Logger log = LoggerFactory.getLogger(DwellingCharacteristicsBindTaskBean.class);
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
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;
    @EJB
    private RequestFileBean requestFileBean;

    private void resolveStreet(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveLocalStreet(dwellingCharacteristics, calculationContext.getUserOrganizationId());
        if (log.isDebugEnabled()) {
            log.debug("Resolving of dwelling characteristics street (id = {}) took {} sec.", dwellingCharacteristics.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveAddress(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveAddress(dwellingCharacteristics, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of dwelling characteristics address (id = {}) took {} sec.", dwellingCharacteristics.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return addressService.isAddressResolved(dwellingCharacteristics);
    }

    private void resolveLocalAccount(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveLocalAccount(dwellingCharacteristics, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of dwelling characteristics (id = {}) for local account took {} sec.",
                    dwellingCharacteristics.getId(), (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveRemoteAccountNumber(DwellingCharacteristics dwellingCharacteristics,
            CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveRemoteAccount(dwellingCharacteristics, calculationContext, updatePuAccount);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of dwelling characteristics (id = {}) for remote account number took {} sec.",
                    dwellingCharacteristics.getId(), (System.nanoTime() - startTime) / 1000000000F);
        }
        return dwellingCharacteristics.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        //связать до улицы.
        resolveStreet(dwellingCharacteristics, calculationContext);

        if (dwellingCharacteristics.getInternalStreetId() != null) { // улица найдена
            //resolve local account.
            resolveLocalAccount(dwellingCharacteristics, calculationContext);
            if (dwellingCharacteristics.getStatus() != RequestStatus.ACCOUNT_NUMBER_RESOLVED
                    && dwellingCharacteristics.getStatus() != RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY) {
                if (resolveAddress(dwellingCharacteristics, calculationContext)) {
                    resolveRemoteAccountNumber(dwellingCharacteristics, calculationContext, updatePuAccount);
                }
            }
        }

        // обновляем dwelling characteristics запись
        {
            long startTime = 0;
            if (log.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            dwellingCharacteristicsBean.update(dwellingCharacteristics);
            if (log.isDebugEnabled()) {
                log.debug("Updating of dwelling characteristics (id = {}) took {} sec.", dwellingCharacteristics.getId(),
                        (System.nanoTime() - startTime) / 1000000000F);
            }
        }
    }

    private void bindDwellingCharacteristicsFile(RequestFile dwellingCharacteristicsFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла dwelling characteristics и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedDwellingCharacteristicsIds = dwellingCharacteristicsBean.findIdsForBinding(dwellingCharacteristicsFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedDwellingCharacteristicsIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedDwellingCharacteristicsIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedDwellingCharacteristicsIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<DwellingCharacteristics> dwellingCharacteristics =
                    dwellingCharacteristicsBean.findForOperation(dwellingCharacteristicsFile.getId(), batch);
            for (DwellingCharacteristics dwellingCharacteristic : dwellingCharacteristics) {
                if (dwellingCharacteristicsFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать dwelling characteristics запись
                try {
                    userTransaction.begin();
                    bind(dwellingCharacteristic, calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The dwelling characteristics item ( id = " + dwellingCharacteristic.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding dwelling characteristics item.", e1);
                    }
                }
            }
        }
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах комманды опцию "Переписывать номер л/с ПУ номером л/с МН"
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

        dwellingCharacteristicsBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла dwelling characteristics
        try {
            bindDwellingCharacteristicsFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в dwelling characteristics файле связались
        if (!dwellingCharacteristicsBean.isDwellingCharacteristicsFileBound(requestFile.getId())) {
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
        return DwellingCharacteristicsBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
