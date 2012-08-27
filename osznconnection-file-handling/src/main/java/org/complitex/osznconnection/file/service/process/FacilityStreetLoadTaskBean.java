/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.FacilityStreet;
import org.complitex.osznconnection.file.entity.FacilityStreetDBF;
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
public class FacilityStreetLoadTaskBean implements ITaskBean {

    public static final String LOCALE_TASK_PARAMETER_KEY = "locale";
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LoadRequestFileBean loadRequestFileBean;
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;
    @EJB
    private LocaleBean localeBean;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;
        final Locale locale = commandParameters.containsKey(LOCALE_TASK_PARAMETER_KEY)
                ? (Locale) commandParameters.get(LOCALE_TASK_PARAMETER_KEY) : localeBean.getSystemLocale();

        final long userOrganizationId = requestFile.getUserOrganizationId();
        //delete previous facility street
        requestFileBean.deleteFacilityReferenceFiles(requestFile.getOrganizationId(), userOrganizationId,
                requestFile.getType());

        requestFile.setStatus(RequestFileStatus.LOADING);
        loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return FacilityStreetDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new FacilityStreet();
            }

            @Override
            public void save(List<AbstractRequest> requests) throws ExecuteException {
                facilityReferenceBookBean.insert(requests);

                /*
                 * Обновить соответствия улиц кодами.
                 */
                facilityReferenceBookBean.updateStreetCorrections(ImmutableList.copyOf(
                        Iterables.transform(requests,
                        new Function<AbstractRequest, FacilityStreet>() {

                            @Override
                            public FacilityStreet apply(AbstractRequest street) {
                                return (FacilityStreet) street;
                            }
                        })), userOrganizationId, locale);
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
        return FacilityStreetLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
