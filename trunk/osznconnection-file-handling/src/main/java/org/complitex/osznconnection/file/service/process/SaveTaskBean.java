package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
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

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:58
 */
@Stateless(name = "SaveTaskBean")
public class SaveTaskBean implements ITaskBean<RequestFileGroup>{
    private static final Logger log = LoggerFactory.getLogger(SaveTaskBean.class);

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @Override
    public boolean execute(RequestFileGroup group) throws ExecuteException {
        group.setStatus(requestFileGroupBean.getRequestFileStatus(group)); //обновляем статус из базы данных

        if (group.isProcessing()){ //проверяем что не обрабатывается в данный момент
            throw new SaveException(new AlreadyProcessingException(group), true, group);
        }

        group.setStatus(RequestFileStatus.SAVING);
        requestFileGroupBean.save(group);

        //сохранение начислений
        save(group.getPaymentFile());
        save(group.getBenefitFile());

        group.setStatus(RequestFileStatus.SAVED);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(RequestFileGroup group) {
        group.setStatus(RequestFileStatus.SAVE_ERROR);
        requestFileGroupBean.save(group);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return SaveTaskBean.class;
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
            case BENEFIT:
                BenefitDBF[] benefitDBFs = BenefitDBF.values();
                dbfFields = new DBFField[benefitDBFs.length];

                for (int i = 0; i < benefitDBFs.length; ++i){
                    BenefitDBF benefitDBF = benefitDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(benefitDBF.name());
                    dbfFields[i].setDataType(getDataType(benefitDBF.getType()));
                    if (!benefitDBF.getType().equals(Date.class)){
                        dbfFields[i].setFieldLength(benefitDBF.getLength());
                        dbfFields[i].setDecimalCount(benefitDBF.getScale());
                    }
                }

                return dbfFields;
            case PAYMENT:
                PaymentDBF[] paymentDBFs = PaymentDBF.values();
                dbfFields = new DBFField[paymentDBFs.length];

                for (int i = 0; i < paymentDBFs.length; ++i){
                    PaymentDBF paymentDBF = paymentDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(paymentDBF.name());
                    dbfFields[i].setDataType(getDataType(paymentDBF.getType()));
                    if (!paymentDBF.getType().equals(Date.class)) {
                        dbfFields[i].setFieldLength(paymentDBF.getLength());
                        dbfFields[i].setDecimalCount(paymentDBF.getScale());
                    }
                }

                return dbfFields;
            default: throw new IllegalArgumentException(type.name());
        }
    }

    public List<AbstractRequest> getAbstractRequests(RequestFile requestFile){
        switch (requestFile.getType()){
            case BENEFIT:
                return benefitBean.getBenefits(requestFile);
            case PAYMENT:
                return paymentBean.getPayments(requestFile);
            default: throw new IllegalArgumentException(requestFile.getType().name());
        }
    }

    @SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
    private void save(RequestFile requestFile) throws SaveException {
        DBFWriter writer = null;

        try {
            //устанавливаем абсолютный путь для сохранения файла запроса
            File file = RequestFileStorage.getInstance().createOutputRequestFile(requestFile.getName(), requestFile.getDirectory());
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
                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = abstractRequest.getDbfFields().get(fields[i].getName());
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
