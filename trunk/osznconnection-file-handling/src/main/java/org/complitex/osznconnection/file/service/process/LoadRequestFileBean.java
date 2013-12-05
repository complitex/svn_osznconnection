package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.*;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.complitex.osznconnection.file.service.file_description.convert.DBFFieldTypeConverter;
import org.complitex.osznconnection.file.service.file_description.convert.RequestFileTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 16:03
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadRequestFileBean {

    private final Logger log = LoggerFactory.getLogger(LoadRequestFileBean.class);
    @EJB
    private ConfigBean configBean;
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    public static abstract class AbstractLoadRequestFile {

        public abstract Enum[] getFieldNames();

        public abstract AbstractRequest newObject();

        public abstract void save(List<AbstractRequest> batch) throws ExecuteException;

        public void postProcess(int rowNumber, AbstractRequest request) {
        }
    }

    public boolean load(RequestFile requestFile, AbstractLoadRequestFile loadRequestFile) throws ExecuteException {
        String currentFieldName = "0";
        int index = -1;
        final int batchSize = configBean.getInteger(FileHandlingConfig.LOAD_BATCH_SIZE, true);

        requestFile.setLoadedRecordCount(0);

        FileInputStream fileInputStream = null;

        try {
            //Инициализация парсера
            fileInputStream = new FileInputStream(requestFile.getAbsolutePath());
            DBFReader reader = new DBFReader(fileInputStream);
            reader.setCharactersetName("cp866");

            //Начало загрузки
            requestFile.setDbfRecordCount(reader.getRecordCount());
            requestFile.setLoaded(DateUtil.getCurrentDate());

            final RequestFileDescription description = requestFileDescriptionBean.getFileDescription(requestFile.getType());

            //обработка строк файла.
            Object[] rowObjects;
            List<AbstractRequest> batch = new ArrayList<>();

            while ((rowObjects = reader.nextRecord()) != null) {
                if (requestFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                index++;

                //если в файле есть хотя бы одна строка, то нужно проверить соответствие описаний полей файла.
                if (index == 0) {
                    //проверка наличия всех полей в файле.
                    {
                        final Set<String> realFieldNames = new HashSet<String>();
                        for (int i = 0; i < reader.getFieldCount(); i++) {
                            realFieldNames.add(reader.getField(i).getName());
                        }
                        for (Enum<?> expectedField : loadRequestFile.getFieldNames()) {
                            final String expectedFieldName = expectedField.name();
                            if (!realFieldNames.contains(expectedFieldName)) {
                                throw new FieldNotFoundException(expectedFieldName);
                            }
                        }
                    }

                    //проверка всех полей в файле, их типов, длины и масштаба.
                    {
                        for (int i = 0; i < reader.getFieldCount(); i++) {
                            checkField(description, reader.getField(i));
                        }
                    }
                }

                AbstractRequest request = loadRequestFile.newObject();
                request.setOrganizationId(requestFile.getOrganizationId());
                request.setUserOrganizationId(requestFile.getUserOrganizationId());

                //Заполнение колонок записи
                for (int i = 0; i < rowObjects.length; ++i) {
                    Object value = rowObjects[i];

                    //обрезать начальные и конечные пробелы, если это строка.
                    if (value != null && value instanceof String) {
                        value = ((String) value).trim();
                    }

                    DBFField field = reader.getField(i);
                    currentFieldName = field.getName();
                    setField(currentFieldName, description.getTypeConverter(), request, value);
                }

                //post processing after filling all fields of request
                loadRequestFile.postProcess(index, request);

                //обработка первой строки
                if (index == 0) {
                    //проверка загружен ли файл
                    Long loadedId = requestFileBean.getLoadedId(requestFile);
                    if (loadedId != null) {
                        request.setId(loadedId);

                        return false;
                    }

                    //сохранение
                    try {
                        requestFileBean.save(requestFile);
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                }

                request.setRequestFileId(requestFile.getId());
                request.setOrganizationId(requestFile.getOrganizationId());
                request.setStatus(RequestStatus.LOADED);

                batch.add(request);

                //Сохранение
                if (batch.size() > batchSize) {
                    try {
                        loadRequestFile.save(batch);
                    } catch (ExecuteException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                    batch.clear();
                }
            }

            //пропуск загрузки если файл пустой
            if (index < 0) {
                throw new EmptyFileException();
            }

            try {
                if (!batch.isEmpty()) {
                    loadRequestFile.save(batch);
                }
            } catch (ExecuteException e) {
                throw e;
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            //Загрузка завершена
            requestFile.setLoadedRecordCount(index + 1);
            requestFileBean.save(requestFile);
        } catch (Exception e) {
            throw new LoadException(e, requestFile, index + 1, currentFieldName);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("Couldn't close request file.", e);
                }
            }
        }

        return true;
    }

    private void checkField(RequestFileDescription description, DBFField dBFField)
            throws FieldNotFoundException, FieldWrongTypeException, FieldWrongSizeException {

        //проверить имя поля.
        final String fieldName = dBFField.getName();
        final RequestFileFieldDescription fieldDescription = description.getField(fieldName);
        if (fieldDescription == null) {
            throw new FieldNotFoundException(fieldName);
        }

        //проверить тип поля.
        final Class<?> realFieldType = DBFFieldTypeConverter.toJavaType(dBFField);
        final Class<?> expectedFieldType = fieldDescription.getFieldType();
        if (!expectedFieldType.equals(realFieldType)) {
            throw new FieldWrongTypeException(fieldName, realFieldType, expectedFieldType);
        }

        //проверить длину поля.
        if (!expectedFieldType.equals(Date.class)) {
            final int realFieldLength = dBFField.getFieldLength();
            final int expectedFieldLength = fieldDescription.getLength();
            if (realFieldLength > expectedFieldLength) {
                throw new FieldWrongSizeException(fieldName);
            }
        }

        //для чисел нужно проверить масштаб.
        if (expectedFieldType == BigDecimal.class || expectedFieldType == Long.class) {
            final int realFieldScale = dBFField.getDecimalCount();
            Integer expectedFieldScale = fieldDescription.getScale();
            if (expectedFieldScale == null) {
                expectedFieldScale = 0;
            }

            if (realFieldScale > expectedFieldScale) {
                throw new FieldWrongSizeException(fieldName);
            }
        }
    }


    private void setField(String fieldName, RequestFileTypeConverter typeConverter, AbstractRequest request, Object value) {
        String stringValue = typeConverter.toString(value);
        request.getDbfFields().put(fieldName, stringValue);
    }
}