/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.FacilityTarif;
import org.complitex.osznconnection.file.entity.FacilityTarifDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.FacilityReferenceBookBean;
import org.complitex.osznconnection.file.service.RequestFileBean;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityTarifLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        //delete previous facility tarif
        requestFileBean.deleteFacilityReferenceFiles(requestFile.getOrganizationId(), requestFile.getUserOrganizationId(),
                requestFile.getType());

        requestFile.setStatus(RequestFileStatus.LOADING);
        loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return FacilityTarifDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new FacilityTarif();
            }

            @Override
            public void save(List<AbstractRequest> requests) throws ExecuteException {
                facilityReferenceBookBean.insert(requests);
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
        return FacilityTarifLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
