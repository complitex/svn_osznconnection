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
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.osznconnection.file.service.util.SubsidyNameParser;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidyLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private SubsidyBean subsidyBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.LOADING);

        boolean noSkip = loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return SubsidyDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new Subsidy();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                subsidyBean.insert(batch);
            }

            @Override
            public void postProcess(AbstractRequest request) {
                final Subsidy subsidy = (Subsidy) request;
                parseFio(subsidy);
            }
        });

        if (!noSkip) {
            requestFile.setStatus(RequestFileStatus.SKIPPED);

            return false; //skip - file already loaded
        }

        requestFile.setStatus(RequestFileStatus.LOADED);
        requestFileBean.save(requestFile);

        return true;
    }

    private void parseFio(Subsidy subsidy) {
        final String rash = (String) subsidy.getField(SubsidyDBF.RASH);
        final String fio = (String) subsidy.getField(SubsidyDBF.FIO);
        SubsidyNameParser.SubsidyName name = SubsidyNameParser.parse(rash, fio);
        subsidy.setFirstName(name.getFirstName());
        subsidy.setMiddleName(name.getMiddleName());
        subsidy.setLastName(name.getLastName());
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFileBean.delete(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return SubsidyLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
