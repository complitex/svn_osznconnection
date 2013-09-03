package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.AbstractAccountRequest;
import org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.DwellingCharacteristicsBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DwellingCharacteristicsSaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;
    @EJB
    private ConfigBean configBean;

    @Override
    protected List<AbstractAccountRequest> getAbstractRequests(RequestFile requestFile) {
        return dwellingCharacteristicsBean.getDwellingCharacteristics(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return DwellingCharacteristicsDBF.IDCODE.name();
    }

    @Override
    public Class<?> getControllerClass() {
        return DwellingCharacteristicsSaveTaskBean.class;
    }

    @Override
    protected RequestFileDirectoryType getSaveDirectoryType() {
        return RequestFileDirectoryType.SAVE_DWELLING_CHARACTERISTICS_DIR;
    }

    @Override
    protected String getOutputFileName(String inputFileName) {
        int lastDotIndex = inputFileName.lastIndexOf(".");
        String extension = inputFileName.substring(lastDotIndex + 1, inputFileName.length());
        String baseFileName = inputFileName.substring(0, lastDotIndex);
        String number = extension.substring(1, 3);
        String dwellingCharacteristicsOutputFileExtensionPrefix =
                configBean.getString(FileHandlingConfig.DWELLING_CHARACTERISTICS_OUTPUT_FILE_EXTENSION_PREFIX, true);
        return baseFileName + "." + dwellingCharacteristicsOutputFileExtensionPrefix + number;
    }
}
