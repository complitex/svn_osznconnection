package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.service.SubsidyBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidySaveTaskBean extends AbstractSaveTaskBean<Subsidy> implements ITaskBean {

    @EJB
    private SubsidyBean subsidyBean;

    @Override
    protected List<Subsidy> getAbstractRequests(RequestFile requestFile) {
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
    protected RequestFileDirectoryType getSaveDirectoryType() {
        return RequestFileDirectoryType.SAVE_SUBSIDY_DIR;
    }
}
