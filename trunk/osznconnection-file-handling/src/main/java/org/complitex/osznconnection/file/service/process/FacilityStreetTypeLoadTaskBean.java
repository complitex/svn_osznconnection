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
import org.complitex.osznconnection.file.entity.FacilityStreetType;
import org.complitex.osznconnection.file.entity.FacilityStreetTypeDBF;
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
public class FacilityStreetTypeLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        //delete previous facility street type
        requestFileBean.deleteFacilityReferenceFiles(requestFile.getOrganizationId(), requestFile.getUserOrganizationId(),
                requestFile.getType());

        requestFile.setStatus(RequestFileStatus.LOADING);
        loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return FacilityStreetTypeDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new FacilityStreetType();
            }

            @Override
            public void save(List<AbstractRequest> requests) {
                facilityReferenceBookBean.insert(requests);
            }
        });
        requestFile.setStatus(RequestFileStatus.LOADED);
        requestFileBean.save(requestFile);
        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        requestFileBean.delete((RequestFile) executorObject);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return FacilityStreetTypeLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
