package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.SaveException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:58
 */
@Stateless(name = "GroupSaveTaskBean")
public class GroupSaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private PaymentBean paymentBean;
    @EJB
    private BenefitBean benefitBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFileGroup group = (RequestFileGroup) executorObject;

        // получаем значение опции и параметров комманды
        // опция перезаписи номера л/с поставщика услуг номером л/с модуля начислений при выгрузке файла запроса
        final boolean updatePuAccount = ((Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT)).booleanValue();

        group.setStatus(requestFileGroupBean.getRequestFileStatus(group)); //обновляем статус из базы данных

        if (group.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new SaveException(new AlreadyProcessingException(group), true, group);
        }

        group.setStatus(RequestFileStatus.SAVING);
        requestFileGroupBean.save(group);

        //сохранение начислений
        save(group.getPaymentFile(), updatePuAccount);
        save(group.getBenefitFile(), updatePuAccount);

        group.setStatus(RequestFileStatus.SAVED);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFileGroup group = (RequestFileGroup) executorObject;
        group.setStatus(RequestFileStatus.SAVE_ERROR);
        requestFileGroupBean.save(group);
    }

    @Override
    public Class<?> getControllerClass() {
        return GroupSaveTaskBean.class;
    }

    @Override
    protected List<AbstractRequest> getAbstractRequests(RequestFile requestFile) {
        switch (requestFile.getType()) {
            case BENEFIT:
                return benefitBean.getBenefits(requestFile.getId());
            case PAYMENT:
                return paymentBean.getPayments(requestFile.getId());
            default:
                throw new IllegalArgumentException(requestFile.getType().name());
        }
    }

    @Override
    protected String getPuAccountFieldName() {
        return PaymentDBF.OWN_NUM_SR.name();
    }

    @Override
    protected FileHandlingConfig getDefaultConfigDirectory() {
        return FileHandlingConfig.DEFAULT_SAVE_PAYMENT_BENEFIT_FILES_DIR;
    }
}
