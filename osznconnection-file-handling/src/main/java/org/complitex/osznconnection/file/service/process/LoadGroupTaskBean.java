package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:57
 */
@Stateless(name = "LoadGroupTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadGroupTaskBean implements ITaskBean{
    private static final Logger log = LoggerFactory.getLogger(LoadGroupTaskBean.class);

    @EJB(beanName = "PaymentBean")
    protected PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    protected BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    protected RequestFileBean requestFileBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @EJB(beanName = "LoadRequestFileBean")
    private LoadRequestFileBean loadRequestFileBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFileGroup group = (RequestFileGroup) executorObject;

        group.setStatus(RequestFileStatus.LOADING);

        requestFileGroupBean.save(group);

        group.getBenefitFile().setGroupId(group.getId());
        group.getPaymentFile().setGroupId(group.getId());

        //load payment
        boolean noSkip = loadRequestFileBean.load(group.getPaymentFile(), new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return PaymentDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new Payment();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                paymentBean.insert(batch);
            }
        });

        // load benefit
        if (noSkip){
            boolean notLoaded = loadRequestFileBean.load(group.getBenefitFile(), new LoadRequestFileBean.AbstractLoadRequestFile() {

                @Override
                public Enum[] getFieldNames() {
                    return BenefitDBF.values();
                }

                @Override
                public AbstractRequest newObject() {
                    return new Benefit();
                }

                @Override
                public void save(List<AbstractRequest> batch) {
                    benefitBean.insert(batch);
                }
            });

            if (!notLoaded){
                throw new ExecuteException("Файл начислений {0} уже загружен.", group.getBenefitFile().getFullName());
            }
        } else {
            requestFileGroupBean.clear(group); //no cascading remove group
            group.setStatus(RequestFileStatus.SKIPPED);

            return false; //skip - file already loaded
        }

        group.setStatus(RequestFileStatus.LOADED);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFileGroup group = (RequestFileGroup) executorObject;
        group.setStatus(RequestFileStatus.LOAD_ERROR);

        requestFileGroupBean.delete(group);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return LoadGroupTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
