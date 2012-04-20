package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class SubsidySaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private SubsidyBean subsidyBean;

    @Override
    protected List<AbstractRequest> getAbstractRequests(RequestFile requestFile) {
        return subsidyBean.getSubsidies(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return SubsidyDBF.RASH.name();
    }

    @Override
    public Class<?> getControllerClass() {
        return SubsidySaveTaskBean.class;
    }

    @Override
    protected IConfig getConfigDirectory() {
        return FileHandlingConfig.SAVE_OUTPUT_SUBSIDY_FILE_STORAGE_DIR;
    }
}
