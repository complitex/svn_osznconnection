package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.FillException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class GroupFillTaskBean implements ITaskBean {
    private final Logger log = LoggerFactory.getLogger(GroupFillTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB
    protected ConfigBean configBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private RequestFileGroupBean requestFileGroupBean;

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
        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(requestFileGroupBean.getRequestFileStatus(group)); //обновляем статус из базы данных

        if (group.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new FillException(new AlreadyProcessingException(group), true, group);
        }

        group.setStatus(RequestFileStatus.FILLING);
        requestFileGroupBean.save(group);

        //получаем информацию о текущем контексте вычислений
        final Collection<CalculationContext> calculationContexts = calculationCenterBean.getContexts(group.getUserOrganizationId());

        //очищаем колонки которые заполняются во время обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeProcessing(group.getPaymentFile().getId(), getServiceProviderTypeIds(calculationContexts));
        benefitBean.clearBeforeProcessing(group.getBenefitFile().getId());

        //обработка файла payment
        try {
            processPayment(group.getPaymentFile(), calculationContexts);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }

        //обработка файла benefit
        try {
            processBenefit(group.getBenefitFile());
        } catch (DBException e) {
            throw new RuntimeException(e);
        }

        group.setStatus(RequestFileStatus.FILLED);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(RequestFileStatus.FILL_ERROR);
        requestFileGroupBean.save(group);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return GroupFillTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }

    /**
     * Обрабатывает payment.
     * Алгоритм:
     * Если статус payment записи указывает на то что запись даже не связана, то пропускаем запись.(Такая ситуация
     * возможна т.к. существует требование обрабатывать файла, которые связаны с ошибками)
     * Иначе вызываем DefaultCalculationCenterAdapter.processPaymentAndBenefit() для заполнения
     * некоторых полей в payment и benefit записях. Наконец, обновляем payment и соответствующие ему benefit
     * записи(BenefitBean.populateBenefit()).
     *
     * @param payment Запись запроса начислений
     */
    private void process(Payment payment, Collection<CalculationContext> calculationContexts) throws DBException {
        if (RequestStatus.unboundStatuses().contains(payment.getStatus())) {
            return;
        }

        final List<Benefit> benefits = benefitBean.findByOZN(payment);

        for (CalculationContext calculationContext : calculationContexts) {
            adapter.processPaymentAndBenefit(calculationContext, payment, benefits);

            /* если payment обработан некорректно текущим модулем начислений, то прерываем обработку данной записи
             * оставшимися модулями.
             */
            if (payment.getStatus() != RequestStatus.PROCESSED) {
                break;
            }
        }

        paymentBean.update(payment, getServiceProviderTypeIds(calculationContexts));
        for (Benefit benefit : benefits) {
            benefitBean.populateBenefit(benefit);
        }
    }

    /**
     * Обработать payment файл.
     * @param paymentFile Файл запроса начислений
     * @throws FillException Ошибка обработки
     */
    private void processPayment(RequestFile paymentFile, Collection<CalculationContext> calculationContexts) throws FillException, DBException {
        //извлечь из базы все id подлежащие обработке для файла payment и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedPaymentIds = paymentBean.findIdsForProcessing(paymentFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.FILL_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(batchSize, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            //достать из базы очередную порцию записей
            List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
            for (Payment payment : payments) {
                if (paymentFile.isCanceled()) {
                    throw new FillException(new CanceledByUserException(), true, paymentFile);
                }

                //обработать payment запись
                try {
                    userTransaction.begin();
                    process(payment, calculationContexts);
                    userTransaction.commit();
                } catch (DBException e) {
                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for processing payment item.", e1);
                    }

                    throw e;
                } catch (Exception e) {
                    log.error("The payment item (id = " + payment.getId() + ") was processed with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for processing payment item.", e1);
                    }
                }
            }
        }

        //проверить все ли записи в payment файле обработались
        if (!paymentBean.isPaymentFileProcessed(paymentFile.getId())) {
            throw new FillException(true, paymentFile);
        }
    }

    /**
     * Обработать benefit файл.
     * Алгоритм:
     * Извлечь все не null account numbers в benefit файле(BenefitBean.getAllAccountNumbers()).
     * Для каждого account number достаем из базы benefit записи с таким account number(BenefitBean.findByAccountNumber().
     * Методом BenefitBean.findDat1() достает дату из поля DAT1 в записи payment, у которой account number такой же
     * (т.е. payment соответствующий данной группе benefit записей) и
     * кроме того поле FROG больше 0(только benefit записи соответствующие таким payment записям нужно обрабатывать).
     * Дата нужна как параметр для вызова
     * org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefit().
     * И если дата не null, то вызываем processBenefit() для заполнения полей в benefit записях. У тех групп benefit
     * записей, у которых дата не нашлась, т.е. соответствующий payment имеет в поле FROG значение 0, проставляем
     * статус RequestStatus.PROCESSED. Наконец, обновляем все benefit записи.
     *
     * @param benefitFile Файл запроса льгот
     * @throws FillException Ошибка обработки
     */
    private void processBenefit(RequestFile benefitFile) throws FillException, DBException {
        //получаем информацию о текущем контексте вычислений
        CalculationContext calculationContext =
                calculationCenterBean.getContextWithAnyCalculationCenter(benefitFile.getUserOrganizationId());

        List<String> allAccountNumbers = benefitBean.getAllAccountNumbers(benefitFile.getId());
        for (String accountNumber : allAccountNumbers) {
            List<Benefit> benefits = benefitBean.findByAccountNumber(accountNumber, benefitFile.getId());
            if (benefits != null && !benefits.isEmpty()) {
                Date dat1 = paymentBean.findDat1(accountNumber, benefitFile.getId());
                if (dat1 != null) {
                    adapter.processBenefit(calculationContext, dat1, benefits);
                } else {
                    for (Benefit benefit : benefits) {
                        benefit.setStatus(RequestStatus.PROCESSED);
                    }
                }

                for (Benefit benefit : benefits) {
                    try {
                        benefitBean.update(benefit);
                    } catch (Exception e) {
                        log.error("The benefit item (id = " + benefit.getId() + ") was processed with error: ", e);
                        throw new FillException(e, false, benefitFile);
                    }
                }
            }
        }

        if (!benefitBean.isBenefitFileProcessed(benefitFile.getId())) {
            throw new FillException(true, benefitFile);
        }
    }
}
