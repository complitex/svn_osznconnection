package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.RequestFile.TYPE;
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
    protected DBFField[] getDbfField(TYPE type) {
        SubsidyDBF[] subsidyDBF = SubsidyDBF.values();
        DBFField[] dbfFields = new DBFField[subsidyDBF.length];

        for (int i = 0; i < subsidyDBF.length; ++i) {
            SubsidyDBF dbf = subsidyDBF[i];
            dbfFields[i] = newDBFField(dbf.name(), dbf.getType(), dbf.getLength(), dbf.getScale());
        }
        return dbfFields;
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
