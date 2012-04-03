/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.SaveException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import javax.ejb.EJB;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 *
 * @author Artem
 */
public abstract class AbstractSaveTaskBean {

    @EJB
    private RequestFileBean requestFileBean;

    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) executorObject;

        // получаем значение опции и параметров комманды
        // опция перезаписи номера л/с поставщика услуг номером л/с модуля начислений при выгрузке файла запроса
        final boolean updatePuAccount = ((Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT)).booleanValue();

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new SaveException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.SAVING);
        requestFileBean.save(requestFile);

        //сохранение
        save(requestFile, updatePuAccount);

        requestFile.setStatus(RequestFileStatus.SAVED);
        requestFileBean.save(requestFile);

        return true;
    }

    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFile.setStatus(RequestFileStatus.SAVE_ERROR);
        requestFileBean.save(requestFile);
    }

    public String getModuleName() {
        return Module.NAME;
    }

    public Log.EVENT getEvent() {
        return Log.EVENT.VIEW;
    }

    private byte getDataType(Class<?> type) {
        if (type.equals(String.class)) {
            return DBFField.FIELD_TYPE_C;
        } else if (type.equals(Integer.class) || type.equals(BigDecimal.class)) {
            return DBFField.FIELD_TYPE_N;
        } else if (type.equals(Date.class)) {
            return DBFField.FIELD_TYPE_D;
        }

        throw new IllegalArgumentException(type.toString());
    }

    protected DBFField newDBFField(String name, Class<?> type, int length, int scale) {
        DBFField field = new DBFField();
        field.setName(name);
        field.setDataType(getDataType(type));
        if (!type.equals(Date.class)) {
            field.setFieldLength(length);
            field.setDecimalCount(scale);
        }
        return field;
    }

    protected abstract List<AbstractRequest> getAbstractRequests(RequestFile requestFile);

    protected abstract String getPuAccountFieldName();

    protected abstract DBFField[] getDbfField(RequestFile.TYPE type);

    protected void save(RequestFile requestFile, boolean updatePuAccount) throws SaveException {
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
                if (requestFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = abstractRequest.getDbfFields().get(fields[i].getName());
                    // перезаписываем номер л/с ПУ номером л/с МН при наличии установленной опции
                    if (updatePuAccount && fields[i].getName().equals(getPuAccountFieldName())) {
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
