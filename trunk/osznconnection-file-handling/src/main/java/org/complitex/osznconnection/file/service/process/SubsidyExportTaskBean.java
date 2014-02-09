package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.entity.SubsidyMasterData;
import org.complitex.osznconnection.file.entity.SubsidyMasterDataFile;
import org.complitex.osznconnection.file.service.SubsidyService;
import org.complitex.osznconnection.file.service.exception.SaveException;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.complitex.osznconnection.file.service.file_description.convert.DBFFieldTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.02.14 2:25
 */
@Stateless
public class SubsidyExportTaskBean implements ITaskBean<SubsidyMasterDataFile> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private SubsidyService subsidyService;

    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    @Override
    public boolean execute(SubsidyMasterDataFile masterDataFile, Map commandParameters) throws ExecuteException {

        try {
            export(masterDataFile);
        } catch (Exception e) {
            throw new ExecuteException(e, "Ошибка экспорта");
        }

        return true;
    }

    private void export(SubsidyMasterDataFile masterDataFile) throws SaveException {
        DBFWriter writer = null;

        try {
            RequestFileDescription description = requestFileDescriptionBean.getFileDescription(RequestFileType.SUBSIDY_J_FILE);

            //Удаляем файл если такой есть и создаем новый.
            writer = new DBFWriter(RequestFileStorage.INSTANCE.deleteAndCreateFile("c:\\tmp\\" + masterDataFile.getId() + ".dbf"));
            writer.setCharactersetName("cp866");

            //Создание полей
            DBFField[] fields = newDBFFields(description);
            writer.setFields(fields);

            //Сохранение строк
            for (SubsidyMasterData masterData : masterDataFile.getMasterDataList()) {
                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = masterData.getDbfFields().get(fields[i].getName());
                }

                writer.addRecord(rowData);
            }

            //Выгрузка завершена
            writer.write();
        } catch (Exception e) {
            if (writer != null) {
                writer.rollback();
            }

            throw new SaveException(e, new RequestFile());
        }
    }

    private DBFField[] newDBFFields(RequestFileDescription description) {
        List<DBFField> dbfFields = Lists.newArrayList();

        for (RequestFileFieldDescription field : description.getFields()) {
            dbfFields.add(newDBFField(field.getName(), field.getFieldType(), field.getLength(), field.getScale()));
        }
        return dbfFields.toArray(new DBFField[dbfFields.size()]);
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

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(SubsidyMasterDataFile masterDataFile) {
        //todo on error
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return SubsidyExportTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.VIEW;
    }
}
