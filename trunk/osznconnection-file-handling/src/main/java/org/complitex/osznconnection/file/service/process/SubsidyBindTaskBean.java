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
import org.complitex.osznconnection.file.service.SubsidyBean;
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
public class SubsidyBindTaskBean implements ITaskBean {

    private final Logger log = LoggerFactory.getLogger(SubsidyBindTaskBean.class);
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
    private SubsidyBean subsidyBean;
    @EJB
    private RequestFileBean requestFileBean;

    private boolean resolveAddress(Subsidy subsidy, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveAddress(subsidy, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of subsidy address (id = {}) took {} sec.", subsidy.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return subsidy.getStatus().isAddressResolved();
    }

    private void resolveLocalAccount(Subsidy subsidy, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveLocalAccount(subsidy, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of subsidy (id = {}) for local account took {} sec.", subsidy.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveRemoteAccountNumber(Subsidy subsidy,
            CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveRemoteAccount(subsidy, calculationContext, updatePuAccount);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of subsidy (id = {}) for remote account number took {} sec.", subsidy.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return subsidy.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(Subsidy subsidy, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        //resolve local account.
        resolveLocalAccount(subsidy, calculationContext);

        if (subsidy.getStatus() != RequestStatus.ACCOUNT_NUMBER_RESOLVED
                && subsidy.getStatus() != RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY) {
            if (resolveAddress(subsidy, calculationContext)) {
                resolveRemoteAccountNumber(subsidy, calculationContext, updatePuAccount);
            }
        }

        // обновляем subsidy запись
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        subsidyBean.update(subsidy);
        if (log.isDebugEnabled()) {
            log.debug("Updating of subsidy (id = {}) took {} sec.", subsidy.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private void bindSubsidyFile(RequestFile subsidyFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла subsidy и доставать записи порциями по BATCH_SIZE штук.
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        List<Long> notResolvedSubsidyIds = subsidyBean.findIdsForBinding(subsidyFile.getId());
        if (log.isDebugEnabled()) {
            log.debug("Finding of subsidy ids for binding took {} sec.", (System.nanoTime() - startTime) / 1000000000F);
        }
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedSubsidyIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedSubsidyIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedSubsidyIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<Subsidy> subsidies = subsidyBean.findForOperation(subsidyFile.getId(), batch);
            for (Subsidy subsidy : subsidies) {
                if (subsidyFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать subsidy запись
                try {
                    userTransaction.begin();
                    bind(subsidy, calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The subsidy item ( id = " + subsidy.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding subsidy item.", e1);
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

        subsidyBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла subsidy
        try {
            bindSubsidyFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в subsidy файле связались
        if (!subsidyBean.isSubsidyFileBound(requestFile.getId())) {
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
        return SubsidyBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
