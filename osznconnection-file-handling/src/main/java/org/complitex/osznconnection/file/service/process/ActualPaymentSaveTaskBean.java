package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 20.01.11 23:40
 */
@Stateless(name = "ActualPaymentSaveTaskBean")
public class ActualPaymentSaveTaskBean extends AbstractSaveTaskBean implements ITaskBean {

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @Override
    public Class<?> getControllerClass() {
        return ActualPaymentSaveTaskBean.class;
    }

    @Override
    protected DBFField[] getDbfField(RequestFile.TYPE type) {
        ActualPaymentDBF[] actualPaymentDBFs = ActualPaymentDBF.values();
        DBFField[] dbfFields = new DBFField[actualPaymentDBFs.length];

        for (int i = 0; i < actualPaymentDBFs.length; ++i) {
            ActualPaymentDBF dbf = actualPaymentDBFs[i];
            dbfFields[i] = newDBFField(dbf.name(), dbf.getType(), dbf.getLength(), dbf.getScale());
        }
        return dbfFields;
    }

    @Override
    protected List<AbstractRequest> getAbstractRequests(RequestFile requestFile) {
        return actualPaymentBean.getActualPayments(requestFile.getId());
    }

    @Override
    protected String getPuAccountFieldName() {
        return ActualPaymentDBF.OWN_NUM.name();
    }

    @Override
    protected IConfig getConfigDirectory() {
        return FileHandlingConfig.SAVE_OUTPUT_ACTUAL_PAYMENT_FILE_STORAGE_DIR;
    }
}
