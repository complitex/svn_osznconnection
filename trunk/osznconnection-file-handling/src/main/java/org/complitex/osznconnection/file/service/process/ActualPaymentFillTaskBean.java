package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
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
import org.complitex.dictionary.util.DateUtil;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentFillTaskBean implements ITaskBean<RequestFile> {
    private static final Logger log = LoggerFactory.getLogger(ActualPaymentFillTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB(beanName = "ConfigBean")
    protected ConfigBean configBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @Override
    public boolean execute(RequestFile actualPaymentFile) throws ExecuteException {
        actualPaymentFile.setStatus(RequestFileStatus.FILLING);
        requestFileBean.save(actualPaymentFile);

        actualPaymentBean.clearBeforeProcessing(actualPaymentFile.getId());

        //обработка файла actualPayment
        try {
            processActualPayment(actualPaymentFile);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }

        //проверить все ли записи в actualPayment файле обработались
        if (!actualPaymentBean.isActualPaymentFileProcessed(actualPaymentFile.getId())) {
            throw new FillException(true, actualPaymentFile);
        }

        actualPaymentFile.setStatus(RequestFileStatus.FILLED);
        requestFileBean.save(actualPaymentFile);

        return true;
    }

    @Override
    public void onError(RequestFile requestFile) {
        requestFile.setStatus(RequestFileStatus.FILL_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return ActualPaymentFillTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }

    private void process(ActualPayment actualPayment, Date date, ICalculationCenterAdapter adapter, long calculationCenterId) throws DBException {
        if (RequestStatus.unboundStatuses().contains(actualPayment.getStatus())) {
            return;
        }
        adapter.processActualPayment(actualPayment, date);
        actualPaymentBean.update(actualPayment);
    }

    private void processActualPayment(RequestFile actualPaymentFile) throws FillException, DBException {
        //получаем информацию о текущем центре начисления
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();

        //извлечь из базы все id подлежащие обработке для файла actualPayment и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedPaymentIds = actualPaymentBean.findIdsForProcessing(actualPaymentFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(Config.FILL_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(batchSize, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            //достать из базы очередную порцию записей
            List<ActualPayment> actualPayments = actualPaymentBean.findForOperation(actualPaymentFile.getId(), batch);
            for (ActualPayment actualPayment : actualPayments) {
                //обработать actualPayment запись
                try {
                    userTransaction.begin();
                    process(actualPayment, DateUtil.getFirstDayOf(actualPaymentFile.getLoaded()), adapter, calculationCenterId);
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
