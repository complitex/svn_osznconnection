package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 20.01.11 23:40
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentSaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @Override
    public Class<?> getControllerClass() {
        return ActualPaymentSaveTaskBean.class;
    }

    @Override
    protected List<AbstractRequest> getAbstractRequests(RequestFile requestFile) {
        return actualPaymentBean.getActualPayments(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return ActualPaymentDBF.OWN_NUM.name();
    }

    @Override
    protected FileHandlingConfig getDefaultConfigDirectory() {
        return FileHandlingConfig.DEFAULT_SAVE_ACTUAL_PAYMENT_DIR;
    }
}
