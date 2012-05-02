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
import org.complitex.osznconnection.file.service.ActualPaymentBean;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentBindTaskBean implements ITaskBean {

    private static final Logger log = LoggerFactory.getLogger(ActualPaymentBindTaskBean.class);
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
    private ActualPaymentBean actualPaymentBean;
    @EJB
    private RequestFileBean requestFileBean;

    private boolean resolveAddress(ActualPayment actualPayment, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        addressService.resolveAddress(actualPayment, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of actualPayment address (id = {}) took {} sec.", actualPayment.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return addressService.isAddressResolved(actualPayment);
    }

    private void resolveLocalAccount(ActualPayment actualPayment, CalculationContext calculationContext) {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveLocalAccount(actualPayment, calculationContext);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of actualPayment (id = {}) for local account took {} sec.", actualPayment.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private boolean resolveRemoteAccountNumber(ActualPayment actualPayment, Date date,
            CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        personAccountService.resolveRemoteAccount(actualPayment, date, calculationContext, updatePuAccount);
        if (log.isDebugEnabled()) {
            log.debug("Resolving of actualPayment (id = {}) for remote account number took {} sec.", actualPayment.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
        return actualPayment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(ActualPayment actualPayment, Date date, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        //resolve local account.
        resolveLocalAccount(actualPayment, calculationContext);

        if (actualPayment.getStatus() != RequestStatus.ACCOUNT_NUMBER_RESOLVED
                && actualPayment.getStatus() != RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY) {
            if (resolveAddress(actualPayment, calculationContext)) {
                resolveRemoteAccountNumber(actualPayment, date, calculationContext, updatePuAccount);
            }
        }

        // обновляем actualPayment запись
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        actualPaymentBean.update(actualPayment);
        if (log.isDebugEnabled()) {
            log.debug("Updating of actualPayment (id = {}) took {} sec.", actualPayment.getId(),
                    (System.nanoTime() - startTime) / 1000000000F);
        }
    }

    private void bindActualPaymentFile(RequestFile actualPaymentFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла actualPayment и доставать записи порциями по BATCH_SIZE штук.
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        List<Long> notResolvedPaymentIds = actualPaymentBean.findIdsForBinding(actualPaymentFile.getId());
        if (log.isDebugEnabled()) {
            log.debug("Finding of actualPayment ids for binding took {} sec.", (System.nanoTime() - startTime) / 1000000000F);
        }
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedPaymentIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedPaymentIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<ActualPayment> actualPayments = actualPaymentBean.findForOperation(actualPaymentFile.getId(), batch);
            for (ActualPayment actualPayment : actualPayments) {
                if (actualPaymentFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать actualPayment запись
                try {
                    userTransaction.begin();
                    bind(actualPayment, actualPaymentBean.getFirstDay(actualPayment, actualPaymentFile),
                            calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The actual payment item ( id = " + actualPayment.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding actual payment item.", e1);
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

        actualPaymentBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла actualPayment
        try {
            bindActualPaymentFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в actualPayment файле связались
        if (!actualPaymentBean.isActualPaymentFileBound(requestFile.getId())) {
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
    public Class getControllerClass() {
        return ActualPaymentBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
