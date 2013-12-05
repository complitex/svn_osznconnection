package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.FillException;
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
import java.util.Set;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentFillTaskBean implements ITaskBean {

    private final Logger log = LoggerFactory.getLogger(ActualPaymentFillTaskBean.class);
    @Resource
    private UserTransaction userTransaction;
    @EJB
    protected ConfigBean configBean;
    @EJB
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private ActualPaymentBean actualPaymentBean;
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private ServiceProviderAdapter adapter;

    private Set<Long> getServiceProviderTypeIds(Collection<CalculationContext> calculationContexts) {
        final Set<Long> serviceProviderTypeIds = Sets.newHashSet();
        for (CalculationContext context : calculationContexts) {
            serviceProviderTypeIds.addAll(context.getServiceProviderTypeIds());
        }
        return ImmutableSet.copyOf(serviceProviderTypeIds);
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new FillException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.FILLING);
        requestFileBean.save(requestFile);

        //получаем информацию о текущем контексте вычислений
        final Collection<CalculationContext> calculationContexts = calculationCenterBean.getContexts(requestFile.getUserOrganizationId());

        actualPaymentBean.clearBeforeProcessing(requestFile.getId(), getServiceProviderTypeIds(calculationContexts));

        //обработка файла actualPayment
        try {
            processActualPayment(requestFile, calculationContexts);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new FillException(e, true, requestFile);
        }

        //проверить все ли записи в actualPayment файле обработались
        if (!actualPaymentBean.isActualPaymentFileProcessed(requestFile.getId())) {
            throw new FillException(true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.FILLED);
        requestFileBean.save(requestFile);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.FILL_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return ActualPaymentFillTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }

    private void process(ActualPayment actualPayment, Date date, Collection<CalculationContext> calculationContexts) throws DBException {
        if (RequestStatus.unboundStatuses().contains(actualPayment.getStatus())) {
            return;
        }

        for (CalculationContext calculationContext : calculationContexts) {
            long startTime = 0;
            if (log.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            adapter.processActualPayment(calculationContext, actualPayment, date);
            log.debug("Processing actualPayment (id = {}, calculation center id: {}) took {} sec.",
                    new Object[]{
                        actualPayment.getId(),
                        calculationContext.getCalculationCenterId(),
                        (System.nanoTime() - startTime) / 1000000000F
                    });

            /* 
             * если actualPayment обработан некорректно текущим модулем начислений, то прерываем обработку данной записи
             * оставшимися модулями.
             */
            if (actualPayment.getStatus() != RequestStatus.PROCESSED) {
                break;
            }
        }

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        actualPaymentBean.update(actualPayment, getServiceProviderTypeIds(calculationContexts));
        log.debug("Updating of actualPayment (id = {}) took {} sec.", actualPayment.getId(), (System.nanoTime() - startTime) / 1000000000F);
    }

    private void processActualPayment(RequestFile actualPaymentFile, Collection<CalculationContext> calculationContexts)
            throws FillException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие обработке для файла actualPayment и доставать записи порциями по BATCH_SIZE штук.
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.nanoTime();
        }
        List<Long> notResolvedPaymentIds = actualPaymentBean.findIdsForProcessing(actualPaymentFile.getId());
        log.debug("Finding of actualPayment ids for processing took {} sec.", (System.nanoTime() - startTime) / 1000000000F);
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.FILL_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(batchSize, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            //достать из базы очередную порцию записей
            List<ActualPayment> actualPayments = actualPaymentBean.findForOperation(actualPaymentFile.getId(), batch);
            for (ActualPayment actualPayment : actualPayments) {
                if (actualPaymentFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //обработать actualPayment запись
                try {
                    userTransaction.begin();
                    process(actualPayment, actualPaymentBean.getFirstDay(actualPayment, actualPaymentFile), calculationContexts);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The actual payment item (id = " + actualPayment.getId() + ") was processed with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for processing actual payment item.", e1);
                    }
                }
            }
        }
    }
}
