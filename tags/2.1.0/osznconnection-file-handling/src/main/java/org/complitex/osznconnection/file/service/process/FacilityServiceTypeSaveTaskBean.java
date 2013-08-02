package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.dictionary.service.ConfigBean;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityServiceTypeSaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;
    @EJB
    private ConfigBean configBean;

    @Override
    protected List<AbstractRequest> getAbstractRequests(RequestFile requestFile) {
        return facilityServiceTypeBean.getFacilityServiceType(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return FacilityServiceTypeDBF.IDCODE.name();
    }

    @Override
    public Class<?> getControllerClass() {
        return FacilityServiceTypeSaveTaskBean.class;
    }

    @Override
    protected RequestFileDirectoryType getSaveDirectoryType() {
        return RequestFileDirectoryType.SAVE_FACILITY_SERVICE_TYPE_DIR;
    }

    @Override
    protected String getOutputFileName(String inputFileName) {
        int lastDotIndex = inputFileName.lastIndexOf(".");
        String extension = inputFileName.substring(lastDotIndex + 1, inputFileName.length());
        String baseFileName = inputFileName.substring(0, lastDotIndex);
        String number = extension.substring(1, 3);
        String facilityServiceTypeOutputFileExtensionPrefix =
                configBean.getString(FileHandlingConfig.FACILITY_SERVICE_TYPE_OUTPUT_FILE_EXTENSION_PREFIX, true);
        return baseFileName + "." + facilityServiceTypeOutputFileExtensionPrefix + number;
    }
}
