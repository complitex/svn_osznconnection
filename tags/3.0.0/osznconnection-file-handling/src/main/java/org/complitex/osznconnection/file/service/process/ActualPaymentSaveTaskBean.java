package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.AbstractAccountRequest;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.ActualPaymentBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

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
    protected List<AbstractAccountRequest> getAbstractRequests(RequestFile requestFile) {
        return actualPaymentBean.getActualPayments(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return ActualPaymentDBF.OWN_NUM.name();
    }

    @Override
    protected RequestFileDirectoryType getSaveDirectoryType() {
        return RequestFileDirectoryType.SAVE_ACTUAL_PAYMENT_DIR;
    }
}
