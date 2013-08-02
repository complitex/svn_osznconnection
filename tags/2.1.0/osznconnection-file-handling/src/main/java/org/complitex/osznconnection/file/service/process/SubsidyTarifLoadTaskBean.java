package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.SubsidyTarif;
import org.complitex.osznconnection.file.entity.SubsidyTarifDBF;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.SubsidyTarifBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;
import java.util.Map;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.11.10 13:03
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidyTarifLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private SubsidyTarifBean subsidyTarifBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        //delete previous subsidy tarif files.
        requestFileBean.deleteSubsidyTarifFiles(requestFile.getOrganizationId());

        requestFile.setStatus(RequestFileStatus.LOADING);

        loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return SubsidyTarifDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new SubsidyTarif();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                subsidyTarifBean.insert(batch);
            }
        });

        requestFile.setStatus(RequestFileStatus.LOADED);
        requestFileBean.save(requestFile);
        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFile.setStatus(RequestFileStatus.LOAD_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return SubsidyTarifLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
