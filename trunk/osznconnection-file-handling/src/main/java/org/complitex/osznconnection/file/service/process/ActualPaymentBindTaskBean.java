/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentBindTaskBean implements ITaskBean<RequestFile> {
    private static final Logger log = LoggerFactory.getLogger(ActualPaymentBindTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB(beanName = "ConfigBean")
    protected ConfigBean configBean;

    @EJB(beanName = "AddressService")
    private AddressService addressService;

    @EJB(beanName = "PersonAccountService")
    private PersonAccountService personAccountService;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;


    private boolean resolveAddress(ActualPayment actualPayment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        addressService.resolveAddress(actualPayment, calculationCenterId, adapter);
        return addressService.isAddressResolved(actualPayment);
    }

    private boolean resolveLocalAccount(ActualPayment actualPayment, long calculationCenterId) {
        personAccountService.resolveLocalAccount(actualPayment, calculationCenterId);
        return actualPayment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private boolean resolveRemoteAccountNumber(ActualPayment actualPayment, Date date, long calculationCenterId, ICalculationCenterAdapter adapter)
            throws DBException {
        personAccountService.resolveRemoteAccount(actualPayment, date, calculationCenterId, adapter);
        return actualPayment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(ActualPayment actualPayment, Date date, long calculationCenterId, ICalculationCenterAdapter adapter) throws DBException {
        if (!resolveLocalAccount(actualPayment, calculationCenterId)) {
            if (resolveAddress(actualPayment, calculationCenterId, adapter)) {
                resolveRemoteAccountNumber(actualPayment, date, calculationCenterId, adapter);
            }
        }

        // обновляем actualPayment запись
        actualPaymentBean.update(actualPayment);
    }

    private void bindActualPaymentFile(RequestFile actualPaymentFile) throws BindException, DBException {
        //получаем информацию о текущем центре начисления
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();

        //извлечь из базы все id подлежащие связыванию для файла actualPayment и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedPaymentIds = actualPaymentBean.findIdsForBinding(actualPaymentFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(Config.BIND_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedPaymentIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedPaymentIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<ActualPayment> actualPayments = actualPaymentBean.findForOperation(actualPaymentFile.getId(), batch);
            for (ActualPayment actualPayment : actualPayments) {
                //связать actualPayment запись
                try {
                    userTransaction.begin();
                    bind(actualPayment, DateUtil.getFirstDayOf(actualPaymentFile.getLoaded()), calculationCenterId, adapter);
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
    public boolean execute(RequestFile requestFile) throws ExecuteException {
        requestFile.setStatus(RequestFileStatus.BINDING);
        requestFileBean.save(requestFile);

        actualPaymentBean.clearBeforeBinding(requestFile.getId());

        //связывание файла actualPayment
        try {
            bindActualPaymentFile(requestFile);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("", e);
        } catch (DBException e) {
            throw new RuntimeException(e);
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
    public void onError(RequestFile requestFile) {
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
