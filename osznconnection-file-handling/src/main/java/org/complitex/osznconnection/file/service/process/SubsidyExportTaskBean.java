package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.apache.commons.lang.StringUtils;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.SubsidyService;
import org.complitex.osznconnection.file.service.exception.SaveException;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.complitex.osznconnection.file.service.file_description.convert.DBFFieldTypeConverter;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.File;
import java.text.SimpleDateFormat;
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

    @EJB
    private OsznOrganizationStrategy organizationStrategy;

    @EJB
    private SessionBean sessionBean;

    private final SimpleDateFormat MONTH = new SimpleDateFormat("MM");

    private Map<String, String> S_FILE_MAP = ImmutableMap.<String, String>builder()
            .put("P_ACCOUNT", "LS")
            .put("DT", "BEGIN0")
            .put("DDPP", "BEGIN0")
            .put("N_DEL", "DELO")
            .put("OPTOT", "TOT")
            .put("KWARTG", "OTOPL")
            .put("NAKWART", "KWART_O")
            .put("WODAG", "WODA")
            .put("NAWODA", "WODA_O")
            .put("OTOPLG", "OTOPL")
            .put("NAOTOPL", "OTOPL_O")
            .put("GORWODA", "PODOGR")
            .put("GORWODAG", "PODOGR")
            .put("NAGORWODA", "GORWODA_O")
            .put("STOKIG", "STOKI")
            .put("NASTOKI", "STOKI_O")
            .put("GAZG", "GAZ")
            .put("NAGAZ", "GAZ_O")
            .put("NAELEKTR", "ELEKTR_O")
            .put("ELEKTRG", "ELEKTR")
            .put("TOTRASKLAD", "TOT")
            .put("BEG_IH", "BEGIN0")
            .put("END_IH", "END0")
            .put("COUNTM", "NUMM")
            .build();

    @Override
    public boolean execute(SubsidyMasterDataFile masterDataFile, Map commandParameters) throws ExecuteException {
        try {
            if (!masterDataFile.getMasterDataList().isEmpty()) {
                masterDataFile.setStatus(RequestFileStatus.EXPORTING);

                export(masterDataFile);

                masterDataFile.setStatus(RequestFileStatus.EXPORTED);
            }
        } catch (Exception e) {
            throw new ExecuteException(e, "Ошибка экспорта");
        }

        return true;
    }

    private void export(SubsidyMasterDataFile masterDataFile) throws SaveException {
        DBFWriter writer = null;

        try {
            RequestFileDescription description = requestFileDescriptionBean.getFileDescription(masterDataFile.getType());

            SubsidyMasterData data = masterDataFile.getMasterDataList().get(0);

            String root = organizationStrategy.getRootExportStoragePath(data.getUserOrganizationId());

            String prefix = "[?]";

            switch (masterDataFile.getType()){
                case SUBSIDY_J_FILE: prefix = "J"; break;
                case SUBSIDY_S_FILE: prefix = "S"; break;
            }

            String fileName =
                    organizationStrategy.getCode(organizationStrategy.getBalanceHolder(data.getServicingOrganizationId())) +
                    File.separator +
                    prefix +
                    StringUtils.leftPad(organizationStrategy.getCode(data.getServicingOrganizationId()), 4, '0') +
                    MONTH.format(data.getDbfFields().get(SubsidyMasterDataDBF.BEGIN0.name())) +
                    ".DBF";
            masterDataFile.setObjectName(fileName);

            if (root == null){
                throw new RuntimeException("Корневой каталог для экспорта файлов не задан");
            }

            //Удаляем файл если такой есть и создаем новый.
            writer = new DBFWriter(RequestFileStorage.INSTANCE.deleteAndCreateFile(root + File.separator + masterDataFile.getObjectName()));
            writer.setCharactersetName("cp866");

            //Создание полей
            DBFField[] fields = newDBFFields(description);
            writer.setFields(fields);

            //Сохранение строк
            for (SubsidyMasterData masterData : masterDataFile.getMasterDataList()) {
                //s_file fields
                if (RequestFileType.SUBSIDY_S_FILE.equals(masterDataFile.getType())){
                    for (String s : S_FILE_MAP.keySet()){
                        masterData.getDbfFields().put(s, masterData.getDbfFields().get(S_FILE_MAP.get(s)));
                    }
                }

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

            throw new SaveException(e, masterDataFile);
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
        masterDataFile.setStatus(RequestFileStatus.EXPORT_ERROR);
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
