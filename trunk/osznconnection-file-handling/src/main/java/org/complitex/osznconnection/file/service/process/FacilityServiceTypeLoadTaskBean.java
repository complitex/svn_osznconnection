package org.complitex.osznconnection.file.service.process;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.FacilityServiceTypeBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.util.FacilityNameParser;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityServiceTypeLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;
    @EJB
    private ConfigBean configBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.LOADING);

        final String defaultCity = configBean.getString(FileHandlingConfig.DEFAULT_REQUEST_FILE_CITY, true);
        final Date facilityServiceTypeDate = DateUtil.getFirstDayOfMonth(requestFile.getYear(), requestFile.getMonth());

        boolean noSkip = loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return FacilityServiceTypeDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new FacilityServiceType(defaultCity, facilityServiceTypeDate);
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                facilityServiceTypeBean.insert(batch);
            }

            @Override
            public void postProcess(int rowNumber, AbstractRequest request) {
                final FacilityServiceType facilityServiceType = (FacilityServiceType) request;
                parseFio(facilityServiceType);
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

    private void parseFio(FacilityServiceType facilityServiceType) {
        String fio = facilityServiceType.getStringField(FacilityServiceTypeDBF.FIO);
        PersonName personName = FacilityNameParser.parse(fio);
        facilityServiceType.setFirstName(personName.getFirstName());
        facilityServiceType.setMiddleName(personName.getMiddleName());
        facilityServiceType.setLastName(personName.getLastName());
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
        return FacilityServiceTypeLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
