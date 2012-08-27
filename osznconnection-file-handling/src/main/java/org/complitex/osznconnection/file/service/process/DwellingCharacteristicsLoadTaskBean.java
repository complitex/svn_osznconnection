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
import org.complitex.osznconnection.file.service.DwellingCharacteristicsBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.util.FacilityNameParser;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DwellingCharacteristicsLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;
    @EJB
    private ConfigBean configBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.LOADING);

        final String defaultCity = configBean.getString(FileHandlingConfig.DEFAULT_REQUEST_FILE_CITY, true);
        final Date dwellingCharacteristicsDate = DateUtil.getFirstDayOfMonth(requestFile.getYear(), requestFile.getMonth());

        boolean noSkip = loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return DwellingCharacteristicsDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new DwellingCharacteristics(defaultCity, dwellingCharacteristicsDate);
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                dwellingCharacteristicsBean.insert(batch);
            }

            @Override
            public void postProcess(int rowNumber, AbstractRequest request) {
                final DwellingCharacteristics dwellingCharacteristics = (DwellingCharacteristics) request;
                parseFio(dwellingCharacteristics);
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

    private void parseFio(DwellingCharacteristics dwellingCharacteristics) {
        String fio = dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.FIO);
        PersonName personName = FacilityNameParser.parse(fio);
        dwellingCharacteristics.setFirstName(personName.getFirstName());
        dwellingCharacteristics.setMiddleName(personName.getMiddleName());
        dwellingCharacteristics.setLastName(personName.getLastName());
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
        return DwellingCharacteristicsLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
