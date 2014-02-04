package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.02.14 2:25
 */
public class SubsidyExportTaskBean implements ITaskBean<RequestFile<Subsidy>> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    @Override
    public boolean execute(RequestFile<Subsidy> object, Map commandParameters) throws ExecuteException {
        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(RequestFile<Subsidy> requestFile) {
        requestFile.setStatus(RequestFileStatus.EXPORT_ERROR);

        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return SubsidyExportTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.VIEW;
    }
}
