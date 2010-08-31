/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitExample;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.NumField;
import org.xBaseJ.xBaseJException;

import javax.ejb.Stateless;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
@Stateless(name = "BenefitBean")
public class BenefitBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = BenefitBean.class.getName();

    public enum OrderBy {

        FIRST_NAME("fNam"), MIDDLE_NAME("mNam"), LAST_NAME("surNam"),
        CITY("internalCity"), STREET("internalStreet"), BUILDING("internalBuilding"), APARTMENT("internalApartment"),
        STATUS("status");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public int count(BenefitExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public int countByFile(long fileId) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", fileId);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<Benefit> find(BenefitExample example) {
        return (List<Benefit>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void load(RequestFile requestFile, DBF dbf) throws xBaseJException, IOException, WrongFieldTypeException {
        Map<BenefitDBF, Field> fields = new HashMap<BenefitDBF, Field>();

        for (BenefitDBF benefitDBF : BenefitDBF.values()){
            Field field = dbf.getField(benefitDBF.name());

            Class fieldClass = field.getClass();
            if ((benefitDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (benefitDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (benefitDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (benefitDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))){
                throw new WrongFieldTypeException();
            }

            fields.put(benefitDBF, field);
        }

        for (int i=0; i< dbf.getRecordCount(); ++i){
            dbf.read();

            Benefit benefit = new Benefit();
            benefit.setRequestFileId(requestFile.getId());
            benefit.setStatus(Status.ADDRESS_UNRESOLVED);
            
            for (BenefitDBF benefitDBF : BenefitDBF.values()){
                Field field = fields.get(benefitDBF);

                String value = field.get().trim();

                if (value.isEmpty()) continue;

                if (benefitDBF.getType().equals(String.class)){
                    benefit.setField(benefitDBF, value);
                }else if (benefitDBF.getType().equals(Integer.class)){
                    benefit.setField(benefitDBF, Integer.parseInt(value));
                }else if (benefitDBF.getType().equals(Double.class)){
                    benefit.setField(benefitDBF, Double.parseDouble(value));
                }else if (benefitDBF.getType().equals(Date.class)){
                    benefit.setField(benefitDBF, ((DateField)field).getCalendar().getTime());
                }
            }

            sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefit", benefit);
        }
    }
}
