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
import java.util.Map;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class GroupBindTaskBean implements ITaskBean {

    private static final Logger log = LoggerFactory.getLogger(GroupBindTaskBean.class);
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
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах комманды опцию "Переписывать номер л/с ПУ номером л/с МН"
        final Boolean updatePuAccount = commandParameters.containsKey(GlobalOptions.UPDATE_PU_ACCOUNT)
                ? (Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT) : false;

        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(requestFileGroupBean.getRequestFileStatus(group)); //обновляем статус из базы данных

        if (group.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new BindException(new AlreadyProcessingException(group), true, group);
        }

        group.setStatus(RequestFileStatus.BINDING);
        requestFileGroupBean.save(group);

        //получаем информацию о текущем контексте вычислений
        CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(group.getUserOrganizationId());

        //очищаем колонки которые заполняются во время связывания и обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeBinding(group.getPaymentFile().getId(), calculationContext.getServiceProviderTypeIds());
        benefitBean.clearBeforeBinding(group.getBenefitFile().getId());

        //связывание файла payment
        RequestFile paymentFile = group.getPaymentFile();
        try {
            bindPaymentFile(paymentFile, calculationContext, updatePuAccount);
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
        return GroupBindTaskBean.class;
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
    private boolean resolveAddress(Payment payment, CalculationContext calculationContext) {
        addressService.resolveAddress(payment, calculationContext);
        return payment.getStatus().isAddressResolved();
    }

    /**
     * Разрешить номер личного счета из локальной таблицы person_account
     * @param payment Запись запроса начислений
     * @return Разрешен ли номер л/с
     */
    private void resolveLocalAccount(Payment payment, CalculationContext calculationContext) {
        personAccountService.resolveLocalAccount(payment, calculationContext);
    }

    /**
     * Разрешить номер л/с в центре начислений
     * @param payment Запись запроса начислений
     * @param adapter Адаптер центра начислений
     * @return Разрешен ли номер л/с
     */
    private boolean resolveRemoteAccountNumber(Payment payment, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        personAccountService.resolveRemoteAccount(payment, calculationContext, updatePuAccount);
        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /*
     * Связать payment запись
     * Алгоритм связывания для payment записи:
     * Попытаться разрешить л/c локально.
     * Если не успешно, то попытаться разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений".
     * Если адрес разрешен, то пытаемся разрешить номер л/c в ЦН.
     */
    private void bind(Payment payment, CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        //resolve local account.
        resolveLocalAccount(payment, calculationContext);

        if (payment.getStatus() != RequestStatus.ACCOUNT_NUMBER_RESOLVED
                && payment.getStatus() != RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY) {
            if (resolveAddress(payment, calculationContext)) {
                resolveRemoteAccountNumber(payment, calculationContext, updatePuAccount);
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
    private void bindPaymentFile(RequestFile paymentFile, CalculationContext calculationContext, Boolean updatePuAccount)
            throws BindException, DBException, CanceledByUserException {
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
                    bind(payment, calculationContext, updatePuAccount);
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
