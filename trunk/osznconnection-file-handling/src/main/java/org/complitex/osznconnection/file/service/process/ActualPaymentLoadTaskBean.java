package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 12.01.11 19:25
 */
@Stateless(name = "ActualPaymentLoadTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class ActualPaymentLoadTaskBean implements ITaskBean<RequestFile> {
    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LoadRequestFileBean")
    private LoadRequestFileBean loadRequestFileBean;

    @EJB(beanName = "ActualPaymentBean")
    private ActualPaymentBean actualPaymentBean;

    @Override
    public boolean execute(RequestFile requestFile) throws ExecuteException {
        requestFile.setStatus(RequestFileStatus.LOADING);

        boolean noSkip = loadRequestFileBean.load(requestFile, new LoadRequestFileBean.ILoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return ActualPaymentDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new ActualPayment();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                actualPaymentBean.insert(batch);
            }
        });

        if (!noSkip){
            requestFile.setStatus(RequestFileStatus.SKIPPED);

            return false; //skip - file already loaded
        }

        requestFile.setStatus(RequestFileStatus.LOADED);
        requestFileBean.save(requestFile);

        return true;
    }

    @Override
    public void onError(RequestFile requestFile) {
        requestFileBean.delete(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return ActualPaymentLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
