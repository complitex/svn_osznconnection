/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.complitex.dictionary.entity.IConfig;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.complitex.osznconnection.file.service.file_description.convert.ConversionException;
import org.complitex.osznconnection.file.service.file_description.convert.DBFFieldTypeConverter;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;

/**
 *
 * @author Artem
 */
public abstract class AbstractSaveTaskBean {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

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

    private DBFField newDBFField(String name, Class<?> javaType, int length, Integer scale) {
        DBFField field = new DBFField();
        field.setName(name);
        field.setDataType(DBFFieldTypeConverter.toDBFType(name, javaType));
        if (javaType != Date.class) {
            field.setFieldLength(length);
            if (scale == null) {
                scale = 0;
            }
            field.setDecimalCount(scale);
        }
        return field;
    }

    protected abstract List<AbstractRequest> getAbstractRequests(RequestFile requestFile);

    protected abstract String getPuAccountFieldName();

    protected abstract IConfig getConfigDirectory();

    private DBFField[] newDBFFields(RequestFileDescription description) {
        List<DBFField> dbfFields = Lists.newArrayList();

        for (RequestFileFieldDescription field : description.getFields()) {
            dbfFields.add(newDBFField(field.getName(), field.getFieldType(), field.getLength(), field.getScale()));
        }
        return dbfFields.toArray(new DBFField[dbfFields.size()]);
    }

    protected final void save(RequestFile requestFile, boolean updatePuAccount) throws SaveException {
        final RequestFileDescription description = requestFileDescriptionBean.getFileDescription(requestFile.getType());

        DBFWriter writer = null;

        try {
            //устанавливаем абсолютный путь для сохранения файла запроса
            File file = RequestFileStorage.getInstance().createOutputRequestFileDirectory(getConfigDirectory(),
                    requestFile.getName(), requestFile.getDirectory());
            requestFile.setAbsolutePath(file.getAbsolutePath());

            //Удаляем файл если такой есть
            RequestFileStorage.getInstance().delete(requestFile.getAbsolutePath());

            writer = new DBFWriter(RequestFileStorage.getInstance().createFile(requestFile.getAbsolutePath(), true));
            writer.setCharactersetName("cp866");

            //Создание полей
            DBFField[] fields = newDBFFields(description);
            writer.setFields(fields);

            //Сохранение строк
            List<AbstractRequest> rows;
            try {
                rows = getAbstractRequests(requestFile);
                requestFile.setRequests(rows);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            for (AbstractRequest request : rows) {
                if (requestFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    final String fieldName = fields[i].getName();
                    String stringValue = request.getDbfFields().get(fieldName);

                    try {
                        final RequestFileFieldDescription fieldDescription = description.getField(fieldName);
                        if (fieldDescription == null) {
                            throw new SaveException(new FieldNotFoundException(fieldName), requestFile);
                        }
                        rowData[i] = description.getTypeConverter().toObject(stringValue, fieldDescription.getFieldType());
                    } catch (ConversionException e) {
                        throw new SaveException(e, requestFile);
                    }

                    // перезаписываем номер л/с ПУ номером л/с МН при наличии установленной опции
                    if (updatePuAccount && fields[i].getName().equals(getPuAccountFieldName())) {
                        rowData[i] = request.getAccountNumber();
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
