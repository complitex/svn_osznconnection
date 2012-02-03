package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
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
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless(name = "BindTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class BindTaskBean implements ITaskBean {

    private static final Logger log = LoggerFactory.getLogger(BindTaskBean.class);
    @Resource
    private UserTransaction userTransaction;
    @EJB
    protected ConfigBean configBean;
    @EJB
    private AddressService addressService;
    @EJB
    private PersonAccountService personAccountService;
    @EJB
    private PaymentBean paymentBean;
    @EJB
    private BenefitBean benefitBean;
    @EJB
    private CalculationCenterBean calculationCenterBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @Override
    public boolean execute(IExecutorObject executorObject) throws ExecuteException {
        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(requestFileGroupBean.getRequestFileStatus(group)); //обновляем статус из базы данных

        if (group.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new BindException(new AlreadyProcessingException(group), true, group);
        }

        group.setStatus(RequestFileStatus.BINDING);
        requestFileGroupBean.save(group);

        //очищаем колонки которые заполняются во время связывания и обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeBinding(group.getPaymentFile().getId());
        benefitBean.clearBeforeBinding(group.getBenefitFile().getId());

        //связывание файла payment
        RequestFile paymentFile = group.getPaymentFile();
        try {
            bindPaymentFile(paymentFile);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, group);
        }

        //связывание файла benefit
        RequestFile benefitFile = group.getBenefitFile();
        bindBenefitFile(benefitFile);

        //проверить все ли записи в payment файле связались
        if (!paymentBean.isPaymentFileBound(paymentFile.getId())) {
            throw new BindException(true, paymentFile);
        }

        //проверить все ли записи в benefit файле связались
        if (!benefitBean.isBenefitFileBound(benefitFile.getId())) {
            throw new BindException(true, benefitFile);
        }

        group.setStatus(RequestFileStatus.BOUND);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(RequestFileStatus.BIND_ERROR);
        requestFileGroupBean.save(group);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return BindTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }

    /**
     * Разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param payment Запись запроса начислений
     * @param adapter Адаптер центра начислений
     * @return Разрешен ли адрес
     */
    private boolean resolveAddress(Payment payment, CalculationCenterInfo calculationCenterInfo) {
        addressService.resolveAddress(payment, calculationCenterInfo);
        return addressService.isAddressResolved(payment);
    }

    /**
     * Разрешить номер личного счета из локальной таблицы person_account
     * @param payment Запись запроса начислений
     * @return Разрешен ли номер л/с
     */
    private boolean resolveLocalAccount(Payment payment, long calculationCenterId) {
        personAccountService.resolveLocalAccount(payment, calculationCenterId);
        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /**
     * Разрешить номер л/с в центре начислений
     * @param payment Запись запроса начислений
     * @param adapter Адаптер центра начислений
     * @return Разрешен ли номер л/с
     */
    private boolean resolveRemoteAccountNumber(Payment payment, CalculationCenterInfo calculationCenterInfo) throws DBException {
        personAccountService.resolveRemoteAccount(payment, calculationCenterInfo);
        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /*
     * Связать payment запись
     * Алгоритм связывания для payment записи:
     * Попытаться разрешить л/c локально.
     * Если не успешно, то попытаться разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений".
     * Если адрес разрешен, то пытаемся разрешить номер л/c в ЦН.
     */
    private void bind(Payment payment, CalculationCenterInfo calculationCenterInfo) throws DBException {
        if (!resolveLocalAccount(payment, calculationCenterInfo.getOrganizationId())) {
            if (resolveAddress(payment, calculationCenterInfo)) {
                resolveRemoteAccountNumber(payment, calculationCenterInfo);
            }
        }

        // обновляем payment запись
        paymentBean.update(payment);
    }

    /**
     * Связать payment файл.
     * @param paymentFile Файл запроса начислений
     * @throws BindException Ошибка связывания
     */
    private void bindPaymentFile(RequestFile paymentFile) throws BindException, DBException, CanceledByUserException {
        //получаем информацию о текущем центре начисления
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getInfo();

        //извлечь из базы все id подлежащие связыванию для файла payment и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedPaymentIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedPaymentIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
            for (Payment payment : payments) {
                if (paymentFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать payment запись
                try {
                    userTransaction.begin();
                    bind(payment, calculationCenterInfo);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The payment item ( id = " + payment.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding payment item.", e1);
                    }
                }
            }
        }
    }

    /**
     * Связать benefit файл.
     * @param benefitFile Файл запросов льгот
     * @throws BindException Ошибка связывания
     */
    private void bindBenefitFile(RequestFile benefitFile) throws BindException {
        //каждой записи benefit проставить статус соответствующей записи payment
        benefitBean.updateBindingStatus(benefitFile.getId());
    }
}
