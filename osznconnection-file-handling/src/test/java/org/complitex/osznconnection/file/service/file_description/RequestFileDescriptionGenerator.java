/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.file_description.jaxb.DatePattern;
import org.complitex.osznconnection.file.service.file_description.jaxb.Field;
import org.complitex.osznconnection.file.service.file_description.jaxb.FieldType;
import org.complitex.osznconnection.file.service.file_description.jaxb.Fields;
import org.complitex.osznconnection.file.service.file_description.jaxb.FileDescription;
import org.complitex.osznconnection.file.service.file_description.jaxb.FileDescriptions;
import org.complitex.osznconnection.file.service.file_description.jaxb.Formatters;
import org.complitex.osznconnection.file.service.file_description.jaxb.ObjectFactory;

/**
 *
 * @author Artem
 */
public class RequestFileDescriptionGenerator {

    private interface DBF {

        Class<?> getType();

        int getLength();

        int getScale();
    }

    private enum PaymentDBF implements DBF {

        OWN_NUM(String.class, 15),
        REE_NUM(Integer.class, 2),
        OPP(String.class, 8),
        NUMB(Integer.class, 2),
        MARK(Integer.class, 2),
        CODE(Integer.class, 4),
        ENT_COD(Integer.class, 10),
        FROG(BigDecimal.class, 5, 1),
        FL_PAY(BigDecimal.class, 9, 2),
        NM_PAY(BigDecimal.class, 9, 2),
        DEBT(BigDecimal.class, 9, 2),
        CODE2_1(Integer.class, 6),
        CODE2_2(Integer.class, 6),
        CODE2_3(Integer.class, 6),
        CODE2_4(Integer.class, 6),
        CODE2_5(Integer.class, 6),
        CODE2_6(Integer.class, 6),
        CODE2_7(Integer.class, 6),
        CODE2_8(Integer.class, 6),
        NORM_F_1(BigDecimal.class, 10, 4),
        NORM_F_2(BigDecimal.class, 10, 4),
        NORM_F_3(BigDecimal.class, 10, 4),
        NORM_F_4(BigDecimal.class, 10, 4),
        NORM_F_5(BigDecimal.class, 10, 4),
        NORM_F_6(BigDecimal.class, 10, 4),
        NORM_F_7(BigDecimal.class, 10, 4),
        NORM_F_8(BigDecimal.class, 10, 4),
        OWN_NUM_SR(String.class, 15),
        DAT1(Date.class, 8),
        DAT2(Date.class, 8),
        OZN_PRZ(Integer.class, 1),
        DAT_F_1(Date.class, 8),
        DAT_F_2(Date.class, 8),
        DAT_FOP_1(Date.class, 8),
        DAT_FOP_2(Date.class, 8),
        ID_RAJ(String.class, 5),
        SUR_NAM(String.class, 30),
        F_NAM(String.class, 15),
        M_NAM(String.class, 20),
        IND_COD(String.class, 10),
        INDX(String.class, 6),
        N_NAME(String.class, 30),
        VUL_NAME(String.class, 30),
        BLD_NUM(String.class, 7),
        CORP_NUM(String.class, 2),
        FLAT(String.class, 9),
        CODE3_1(Integer.class, 6),
        CODE3_2(Integer.class, 6),
        CODE3_3(Integer.class, 6),
        CODE3_4(Integer.class, 6),
        CODE3_5(Integer.class, 6),
        CODE3_6(Integer.class, 6),
        CODE3_7(Integer.class, 6),
        CODE3_8(Integer.class, 6),
        OPP_SERV(String.class, 8),
        RESERV1(Integer.class, 10),
        RESERV2(String.class, 10);
        private Class<?> type;
        private int length;
        private int scale = 0;

        PaymentDBF(Class<?> type, int length, int scale) {
            this.type = type;
            this.length = length;
            this.scale = scale;
        }

        PaymentDBF(Class<?> type, int length) {
            this.type = type;
            this.length = length;
        }

        PaymentDBF(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getScale() {
            return scale;
        }
    }

    private enum BenefitDBF implements DBF {

        OWN_NUM(String.class, 15),
        REE_NUM(Integer.class, 2),
        OWN_NUM_SR(String.class, 15),
        FAM_NUM(Integer.class, 2),
        SUR_NAM(String.class, 30),
        F_NAM(String.class, 15),
        M_NAM(String.class, 20),
        IND_COD(String.class, 10),
        PSP_SER(String.class, 6),
        PSP_NUM(String.class, 6),
        OZN(Integer.class, 1),
        CM_AREA(BigDecimal.class, 10, 2),
        HEAT_AREA(BigDecimal.class, 10, 2),
        OWN_FRM(Integer.class, 6),
        HOSTEL(Integer.class, 2),
        PRIV_CAT(Integer.class, 3),
        ORD_FAM(Integer.class, 2),
        OZN_SQ_ADD(Integer.class, 1),
        OZN_ABS(Integer.class, 1),
        RESERV1(BigDecimal.class, 10, 2),
        RESERV2(String.class, 10);
        private Class<?> type;
        private int length;
        private int scale = 0;

        BenefitDBF(Class<?> type, int length, int scale) {
            this.type = type;
            this.length = length;
            this.scale = scale;
        }

        BenefitDBF(Class<?> type, int length) {
            this.type = type;
            this.length = length;
        }

        BenefitDBF(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getScale() {
            return scale;
        }
    }

    private enum ActualPaymentDBF implements DBF {

        SUR_NAM(String.class, 30),
        F_NAM(String.class, 15),
        M_NAM(String.class, 20),
        INDX(String.class, 6),
        N_NAME(String.class, 30),
        N_CODE(String.class, 5),
        VUL_CAT(String.class, 7),
        VUL_NAME(String.class, 30),
        VUL_CODE(String.class, 5),
        BLD_NUM(String.class, 7),
        CORP_NUM(String.class, 2),
        FLAT(String.class, 9),
        OWN_NUM(String.class, 15),
        APP_NUM(String.class, 8),
        DAT_BEG(Date.class, 8),
        DAT_END(Date.class, 8),
        CM_AREA(BigDecimal.class, 7, 2),
        NM_AREA(BigDecimal.class, 7, 2),
        BLC_AREA(BigDecimal.class, 5, 2),
        FROG(BigDecimal.class, 5, 1),
        DEBT(BigDecimal.class, 10, 2),
        NUMB(Integer.class, 2),
        P1(BigDecimal.class, 10, 4),
        N1(BigDecimal.class, 10, 4),
        P2(BigDecimal.class, 10, 4),
        N2(BigDecimal.class, 10, 4),
        P3(BigDecimal.class, 10, 4),
        N3(BigDecimal.class, 10, 4),
        P4(BigDecimal.class, 10, 4),
        N4(BigDecimal.class, 10, 4),
        P5(BigDecimal.class, 10, 4),
        N5(BigDecimal.class, 10, 4),
        P6(BigDecimal.class, 10, 4),
        N6(BigDecimal.class, 10, 4),
        P7(BigDecimal.class, 10, 4),
        N7(BigDecimal.class, 10, 4),
        P8(BigDecimal.class, 10, 4),
        N8(BigDecimal.class, 10, 4);
        private Class<?> type;
        private int length;
        private int scale = 0;

        ActualPaymentDBF(Class<?> type, int length, int scale) {
            this.type = type;
            this.length = length;
            this.scale = scale;
        }

        ActualPaymentDBF(Class<?> type, int length) {
            this.type = type;
            this.length = length;
        }

        ActualPaymentDBF(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getScale() {
            return scale;
        }
    }

    private enum SubsidyDBF implements DBF {

        FIO(String.class, 30),
        ID_RAJ(String.class, 5),
        NP_CODE(String.class, 5),
        NP_NAME(String.class, 30),
        CAT_V(String.class, 7),
        VULCOD(String.class, 8),
        NAME_V(String.class, 30),
        BLD(String.class, 7),
        CORP(String.class, 2),
        FLAT(String.class, 9),
        RASH(String.class, 14),
        NUMB(String.class, 8),
        DAT1(Date.class, 8),
        DAT2(Date.class, 8),
        NM_PAY(BigDecimal.class, 9, 2),
        P1(BigDecimal.class, 9, 4),
        P2(BigDecimal.class, 9, 4),
        P3(BigDecimal.class, 9, 4),
        P4(BigDecimal.class, 9, 4),
        P5(BigDecimal.class, 9, 4),
        P6(BigDecimal.class, 9, 4),
        P7(BigDecimal.class, 9, 4),
        P8(BigDecimal.class, 9, 4),
        SM1(BigDecimal.class, 9, 2),
        SM2(BigDecimal.class, 9, 2),
        SM3(BigDecimal.class, 9, 2),
        SM4(BigDecimal.class, 9, 2),
        SM5(BigDecimal.class, 9, 2),
        SM6(BigDecimal.class, 9, 2),
        SM7(BigDecimal.class, 9, 2),
        SM8(BigDecimal.class, 9, 2),
        SB1(BigDecimal.class, 9, 2),
        SB2(BigDecimal.class, 9, 2),
        SB3(BigDecimal.class, 9, 2),
        SB4(BigDecimal.class, 9, 2),
        SB5(BigDecimal.class, 9, 2),
        SB6(BigDecimal.class, 9, 2),
        SB7(BigDecimal.class, 9, 2),
        SB8(BigDecimal.class, 9, 2),
        OB1(BigDecimal.class, 9, 2),
        OB2(BigDecimal.class, 9, 2),
        OB3(BigDecimal.class, 9, 2),
        OB4(BigDecimal.class, 9, 2),
        OB5(BigDecimal.class, 9, 2),
        OB6(BigDecimal.class, 9, 2),
        OB7(BigDecimal.class, 9, 2),
        OB8(BigDecimal.class, 9, 2),
        SUMMA(BigDecimal.class, 13, 2),
        NUMM(Integer.class, 2),
        SUBS(BigDecimal.class, 13, 2),
        KVT(Integer.class, 3);
        private Class<?> type;
        private int length;
        private int scale = 0;

        SubsidyDBF(Class<?> type, int length, int scale) {
            this.type = type;
            this.length = length;
            this.scale = scale;
        }

        SubsidyDBF(Class<?> type, int length) {
            this.type = type;
            this.length = length;
        }

        SubsidyDBF(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getScale() {
            return scale;
        }
    }

    private enum TarifDBF implements DBF {

        T11_DATA_T(String.class, 10),
        T11_DATA_E(String.class, 10),
        T11_DATA_R(String.class, 10),
        T11_MARK(Integer.class, 3),
        T11_TARN(Integer.class, 6),
        T11_CODE1(Integer.class, 3),
        T11_CODE2(Integer.class, 6),
        T11_COD_NA(String.class, 40),
        T11_CODE3(Integer.class, 6),
        T11_NORM_U(BigDecimal.class, 19, 10),
        T11_NOR_US(BigDecimal.class, 19, 10),
        T11_CODE_N(Integer.class, 3),
        T11_COD_ND(Integer.class, 3),
        T11_CD_UNI(Integer.class, 3),
        T11_CS_UNI(BigDecimal.class, 19, 10),
        T11_NORM(BigDecimal.class, 19, 10),
        T11_NRM_DO(BigDecimal.class, 19, 10),
        T11_NRM_MA(BigDecimal.class, 19, 10),
        T11_K_NADL(BigDecimal.class, 19, 10);
        private Class<?> type;
        private int length;
        private int scale = 0;

        TarifDBF(Class<?> type, int length, int scale) {
            this.type = type;
            this.length = length;
            this.scale = scale;
        }

        TarifDBF(Class<?> type, int length) {
            this.type = type;
            this.length = length;
        }

        TarifDBF(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getScale() {
            return scale;
        }
    }

    public static FileDescriptions generate() {
        ObjectFactory factory = new ObjectFactory();
        FileDescriptions fds = factory.createFileDescriptions();

        Map<String, Class<? extends Enum<?>>> enumsMap =
                ImmutableMap.<String, Class<? extends Enum<?>>>of(
                RequestFile.TYPE.ACTUAL_PAYMENT.name(), ActualPaymentDBF.class,
                RequestFile.TYPE.PAYMENT.name(), PaymentDBF.class,
                RequestFile.TYPE.BENEFIT.name(), BenefitDBF.class,
                RequestFile.TYPE.SUBSIDY.name(), SubsidyDBF.class,
                RequestFile.TYPE.TARIF.name(), TarifDBF.class);

        for (String fileType : enumsMap.keySet()) {
            FileDescription fd = factory.createFileDescription();
            Fields fs = factory.createFields();
            fd.setFields(fs);


            fd.setType(fileType);


            Formatters frs = factory.createFormatters();

            DatePattern dp = factory.createDatePattern();
            dp.setPattern("dd.MM.yyyy");
            frs.setDatePattern(dp);
            fd.setFormatters(frs);

            final Class<? extends Enum<?>> enumClass = enumsMap.get(fileType);

            for (Enum<?> field : enumClass.getEnumConstants()) {
                Field f = factory.createField();
                f.setName(field.name());

                final Class<?> fieldType = ((DBF) field).getType();
                if (fieldType == String.class) {
                    f.setType(FieldType.STRING);
                } else if (fieldType == Integer.class) {
                    f.setType(FieldType.INTEGER);
                } else if (fieldType == BigDecimal.class) {
                    f.setType(FieldType.BIG_DECIMAL);
                } else if (fieldType == Date.class) {
                    f.setType(FieldType.DATE);
                }

                f.setLength(((DBF) field).getLength());
                final int scale = ((DBF) field).getScale();
                f.setScale(scale > 0 ? scale : null);
                fs.getFieldList().add(f);
            }

            fds.getFileDescriptionList().add(fd);
        }
        return fds;
    }

    public static void main(String[] args) {
        try {
            JAXBContext context = JAXBContext.newInstance(FileDescriptions.class.getPackage().getName());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(generate(), System.out);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
