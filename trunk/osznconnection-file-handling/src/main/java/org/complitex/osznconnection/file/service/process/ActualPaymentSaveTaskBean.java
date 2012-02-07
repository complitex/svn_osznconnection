package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.SaveException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.wicket.Session;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.complitex.template.web.template.TemplateSession;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 20.01.11 23:40
 */
@Stateless(name = "ActualPaymentSaveTaskBean")
public class ActualPaymentSaveTaskBean implements ITaskBean {
    private static final Logger log = LoggerFactory.getLogger(ActualPaymentSaveTaskBean.class);

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "ActualPaymentBean")
    private ActualPaymentBean actualPaymentBean;
    
    // опция перезаписи номера л/с поставщика услуг номером л/с модуля начислений при выгрузке файла запроса
    private boolean updatePuAccount;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;
        
        // получаем значение опции и параметров комманды
        updatePuAccount = ((Boolean)commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT)).booleanValue();

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()){ //проверяем что не обрабатывается в данный момент
            throw new SaveException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.SAVING);
        requestFileBean.save(requestFile);

        //сохранение фактических начислений
        save(requestFile);

        requestFile.setStatus(RequestFileStatus.SAVED);
        requestFileBean.save(requestFile);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.SAVE_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return ActualPaymentSaveTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.VIEW;
    }

    private byte getDataType(Class type){
        if (type.equals(String.class)){
            return DBFField.FIELD_TYPE_C;
        }else if (type.equals(Integer.class) || type.equals(BigDecimal.class)){
            return DBFField.FIELD_TYPE_N;
        }else if (type.equals(Date.class)){
            return DBFField.FIELD_TYPE_D;
        }

        throw  new IllegalArgumentException(type.toString());
    }

    private DBFField[] getDbfField(RequestFile.TYPE type){
        DBFField[] dbfFields;

        switch (type){
            case ACTUAL_PAYMENT:
                ActualPaymentDBF[] actualPaymentDBFs = ActualPaymentDBF.values();
                dbfFields = new DBFField[actualPaymentDBFs.length];

                for (int i = 0; i < actualPaymentDBFs.length; ++i){
                    ActualPaymentDBF dbf = actualPaymentDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(dbf.name());
                    dbfFields[i].setDataType(getDataType(dbf.getType()));
                    if (!dbf.getType().equals(Date.class)){
                        dbfFields[i].setFieldLength(dbf.getLength());
                        dbfFields[i].setDecimalCount(dbf.getScale());
                    }
                }

                return dbfFields;

            default: throw new IllegalArgumentException(type.name());
        }
    }

    public List<AbstractRequest> getAbstractRequests(RequestFile requestFile){
        switch (requestFile.getType()){
            case ACTUAL_PAYMENT:
                return actualPaymentBean.getActualPayments(requestFile);
            default: throw new IllegalArgumentException(requestFile.getType().name());
        }
    }

    //todo extract super class
    @SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
    private void save(RequestFile requestFile) throws SaveException {
        DBFWriter writer = null;

        try {
            //устанавливаем абсолютный путь для сохранения файла запроса
            File file = RequestFileStorage.getInstance().createOutputActualPaymentFile(requestFile.getName(),
                    requestFile.getDirectory());
            requestFile.setAbsolutePath(file.getAbsolutePath());

            //Удаляем файл есть такой есть
            RequestFileStorage.getInstance().delete(requestFile.getAbsolutePath());

            writer = new DBFWriter(RequestFileStorage.getInstance().createFile(requestFile.getAbsolutePath(), true));
            writer.setCharactersetName("cp866");

            //Создание полей
            DBFField[] fields = getDbfField(requestFile.getType());
            writer.setFields(fields);

            //Сохранение строк
            List<AbstractRequest> rows;
            try {
                rows = getAbstractRequests(requestFile);
                requestFile.setRequests(rows);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            for (AbstractRequest abstractRequest : rows) {
                if (requestFile.isCanceled()){
                    throw new CanceledByUserException();
                }

                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = abstractRequest.getDbfFields().get(fields[i].getName());
                    // перезаписываем номер л/с ПУ номером л/с МН при наличии установленной опции
                    if (updatePuAccount && fields[i].getName().equals("OWN_NUM")){
                        rowData[i] = abstractRequest.getAccountNumber();
                    }
                }

                writer.addRecord(rowData);
            }

            //Выгрузка завершена
            writer.write();
        } catch (Exception e) {
            if (writer != null) {
                writer.rollback();
            }

            throw new SaveException(e, requestFile);
        }
    }
}

