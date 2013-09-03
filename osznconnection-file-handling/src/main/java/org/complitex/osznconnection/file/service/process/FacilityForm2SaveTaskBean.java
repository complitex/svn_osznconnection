package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.AbstractAccountRequest;
import org.complitex.osznconnection.file.entity.FacilityForm2DBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.FacilityForm2Bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityForm2SaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private FacilityForm2Bean facilityForm2Bean2;

    @Override
    protected List<AbstractAccountRequest> getAbstractRequests(RequestFile requestFile) {
        return facilityForm2Bean2.getFacilityForm2(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return FacilityForm2DBF.IDCODE.name();
    }

    @Override
    public Class<?> getControllerClass() {
        return FacilityForm2SaveTaskBean.class;
    }

    @Override
    protected RequestFileDirectoryType getSaveDirectoryType() {
        return RequestFileDirectoryType.SAVE_FACILITY_FORM2_DIR;
    }
}
