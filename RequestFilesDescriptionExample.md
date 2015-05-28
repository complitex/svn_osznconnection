# Пример xml описания структуры файлов запросов для Харькова #
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<file-descriptions>
    <file-description type="ACTUAL_PAYMENT">
        <formatters>
            <date-pattern pattern="dd.MM.yyyy"/>
        </formatters>
        <fields>
            <field length="30" type="java.lang.String" name="SUR_NAM"/>
            <field length="15" type="java.lang.String" name="F_NAM"/>
            <field length="20" type="java.lang.String" name="M_NAM"/>
            <field length="6" type="java.lang.String" name="INDX"/>
            <field length="30" type="java.lang.String" name="N_NAME"/>
            <field length="5" type="java.lang.String" name="N_CODE"/>
            <field length="7" type="java.lang.String" name="VUL_CAT"/>
            <field length="30" type="java.lang.String" name="VUL_NAME"/>
            <field length="5" type="java.lang.String" name="VUL_CODE"/>
            <field length="7" type="java.lang.String" name="BLD_NUM"/>
            <field length="2" type="java.lang.String" name="CORP_NUM"/>
            <field length="9" type="java.lang.String" name="FLAT"/>
            <field length="15" type="java.lang.String" name="OWN_NUM"/>
            <field length="8" type="java.lang.String" name="APP_NUM"/>
            <field length="8" type="java.util.Date" name="DAT_BEG"/>
            <field length="8" type="java.util.Date" name="DAT_END"/>
            <field scale="2" length="7" type="java.math.BigDecimal" name="CM_AREA"/>
            <field scale="2" length="7" type="java.math.BigDecimal" name="NM_AREA"/>
            <field scale="2" length="5" type="java.math.BigDecimal" name="BLC_AREA"/>
            <field scale="1" length="5" type="java.math.BigDecimal" name="FROG"/>
            <field scale="2" length="10" type="java.math.BigDecimal" name="DEBT"/>
            <field length="2" type="java.lang.Integer" name="NUMB"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P1"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N1"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P2"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N2"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P3"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N3"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P4"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N4"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P5"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N5"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P6"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N6"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P7"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N7"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="P8"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="N8"/>
        </fields>
    </file-description>
    <file-description type="PAYMENT">
        <formatters>
            <date-pattern pattern="dd.MM.yyyy"/>
        </formatters>
        <fields>
            <field length="15" type="java.lang.String" name="OWN_NUM"/>
            <field length="2" type="java.lang.Integer" name="REE_NUM"/>
            <field length="8" type="java.lang.String" name="OPP"/>
            <field length="2" type="java.lang.Integer" name="NUMB"/>
            <field length="2" type="java.lang.Integer" name="MARK"/>
            <field length="4" type="java.lang.Integer" name="CODE"/>
            <field length="10" type="java.lang.Integer" name="ENT_COD"/>
            <field scale="1" length="5" type="java.math.BigDecimal" name="FROG"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="FL_PAY"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="NM_PAY"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="DEBT"/>
            <field length="6" type="java.lang.Integer" name="CODE2_1"/>
            <field length="6" type="java.lang.Integer" name="CODE2_2"/>
            <field length="6" type="java.lang.Integer" name="CODE2_3"/>
            <field length="6" type="java.lang.Integer" name="CODE2_4"/>
            <field length="6" type="java.lang.Integer" name="CODE2_5"/>
            <field length="6" type="java.lang.Integer" name="CODE2_6"/>
            <field length="6" type="java.lang.Integer" name="CODE2_7"/>
            <field length="6" type="java.lang.Integer" name="CODE2_8"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_1"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_2"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_3"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_4"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_5"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_6"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_7"/>
            <field scale="4" length="10" type="java.math.BigDecimal" name="NORM_F_8"/>
            <field length="15" type="java.lang.String" name="OWN_NUM_SR"/>
            <field length="8" type="java.util.Date" name="DAT1"/>
            <field length="8" type="java.util.Date" name="DAT2"/>
            <field length="1" type="java.lang.Integer" name="OZN_PRZ"/>
            <field length="8" type="java.util.Date" name="DAT_F_1"/>
            <field length="8" type="java.util.Date" name="DAT_F_2"/>
            <field length="8" type="java.util.Date" name="DAT_FOP_1"/>
            <field length="8" type="java.util.Date" name="DAT_FOP_2"/>
            <field length="5" type="java.lang.String" name="ID_RAJ"/>
            <field length="30" type="java.lang.String" name="SUR_NAM"/>
            <field length="15" type="java.lang.String" name="F_NAM"/>
            <field length="20" type="java.lang.String" name="M_NAM"/>
            <field length="10" type="java.lang.String" name="IND_COD"/>
            <field length="6" type="java.lang.String" name="INDX"/>
            <field length="30" type="java.lang.String" name="N_NAME"/>
            <field length="30" type="java.lang.String" name="VUL_NAME"/>
            <field length="7" type="java.lang.String" name="BLD_NUM"/>
            <field length="2" type="java.lang.String" name="CORP_NUM"/>
            <field length="9" type="java.lang.String" name="FLAT"/>
            <field length="6" type="java.lang.Integer" name="CODE3_1"/>
            <field length="6" type="java.lang.Integer" name="CODE3_2"/>
            <field length="6" type="java.lang.Integer" name="CODE3_3"/>
            <field length="6" type="java.lang.Integer" name="CODE3_4"/>
            <field length="6" type="java.lang.Integer" name="CODE3_5"/>
            <field length="6" type="java.lang.Integer" name="CODE3_6"/>
            <field length="6" type="java.lang.Integer" name="CODE3_7"/>
            <field length="6" type="java.lang.Integer" name="CODE3_8"/>
            <field length="8" type="java.lang.String" name="OPP_SERV"/>
            <field length="10" type="java.lang.Integer" name="RESERV1"/>
            <field length="10" type="java.lang.String" name="RESERV2"/>
        </fields>
    </file-description>
    <file-description type="BENEFIT">
        <formatters>
            <date-pattern pattern="dd.MM.yyyy"/>
        </formatters>
        <fields>
            <field length="15" type="java.lang.String" name="OWN_NUM"/>
            <field length="2" type="java.lang.Integer" name="REE_NUM"/>
            <field length="15" type="java.lang.String" name="OWN_NUM_SR"/>
            <field length="2" type="java.lang.Integer" name="FAM_NUM"/>
            <field length="30" type="java.lang.String" name="SUR_NAM"/>
            <field length="15" type="java.lang.String" name="F_NAM"/>
            <field length="20" type="java.lang.String" name="M_NAM"/>
            <field length="10" type="java.lang.String" name="IND_COD"/>
            <field length="6" type="java.lang.String" name="PSP_SER"/>
            <field length="6" type="java.lang.String" name="PSP_NUM"/>
            <field length="1" type="java.lang.Integer" name="OZN"/>
            <field scale="2" length="10" type="java.math.BigDecimal" name="CM_AREA"/>
            <field scale="2" length="10" type="java.math.BigDecimal" name="HEAT_AREA"/>
            <field length="6" type="java.lang.Integer" name="OWN_FRM"/>
            <field length="2" type="java.lang.Integer" name="HOSTEL"/>
            <field length="3" type="java.lang.Integer" name="PRIV_CAT"/>
            <field length="2" type="java.lang.Integer" name="ORD_FAM"/>
            <field length="1" type="java.lang.Integer" name="OZN_SQ_ADD"/>
            <field length="1" type="java.lang.Integer" name="OZN_ABS"/>
            <field scale="2" length="10" type="java.math.BigDecimal" name="RESERV1"/>
            <field length="10" type="java.lang.String" name="RESERV2"/>
        </fields>
    </file-description>
    <file-description type="SUBSIDY">
        <formatters>
            <date-pattern pattern="dd.MM.yyyy"/>
        </formatters>
        <fields>
            <field length="30" type="java.lang.String" name="FIO"/>
            <field length="5" type="java.lang.String" name="ID_RAJ"/>
            <field length="5" type="java.lang.String" name="NP_CODE"/>
            <field length="30" type="java.lang.String" name="NP_NAME"/>
            <field length="7" type="java.lang.String" name="CAT_V"/>
            <field length="8" type="java.lang.String" name="VULCOD"/>
            <field length="30" type="java.lang.String" name="NAME_V"/>
            <field length="7" type="java.lang.String" name="BLD"/>
            <field length="2" type="java.lang.String" name="CORP"/>
            <field length="9" type="java.lang.String" name="FLAT"/>
            <field length="14" type="java.lang.String" name="RASH"/>
            <field length="8" type="java.lang.String" name="NUMB"/>
            <field length="8" type="java.util.Date" name="DAT1"/>
            <field length="8" type="java.util.Date" name="DAT2"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="NM_PAY"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P1"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P2"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P3"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P4"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P5"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P6"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P7"/>
            <field scale="4" length="9" type="java.math.BigDecimal" name="P8"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM1"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM2"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM3"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM4"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM5"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM6"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM7"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SM8"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB1"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB2"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB3"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB4"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB5"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB6"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB7"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="SB8"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB1"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB2"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB3"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB4"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB5"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB6"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB7"/>
            <field scale="2" length="9" type="java.math.BigDecimal" name="OB8"/>
            <field scale="2" length="13" type="java.math.BigDecimal" name="SUMMA"/>
            <field length="2" type="java.lang.Integer" name="NUMM"/>
            <field scale="2" length="13" type="java.math.BigDecimal" name="SUBS"/>
            <field length="3" type="java.lang.Integer" name="KVT"/>
        </fields>
    </file-description>
    <file-description type="TARIF">
        <formatters>
            <date-pattern pattern="dd.MM.yyyy"/>
        </formatters>
        <fields>
            <field length="10" type="java.lang.String" name="T11_DATA_T"/>
            <field length="10" type="java.lang.String" name="T11_DATA_E"/>
            <field length="10" type="java.lang.String" name="T11_DATA_R"/>
            <field length="3" type="java.lang.Integer" name="T11_MARK"/>
            <field length="6" type="java.lang.Integer" name="T11_TARN"/>
            <field length="3" type="java.lang.Integer" name="T11_CODE1"/>
            <field length="6" type="java.lang.Integer" name="T11_CODE2"/>
            <field length="40" type="java.lang.String" name="T11_COD_NA"/>
            <field length="6" type="java.lang.Integer" name="T11_CODE3"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_NORM_U"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_NOR_US"/>
            <field length="3" type="java.lang.Integer" name="T11_CODE_N"/>
            <field length="3" type="java.lang.Integer" name="T11_COD_ND"/>
            <field length="3" type="java.lang.Integer" name="T11_CD_UNI"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_CS_UNI"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_NORM"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_NRM_DO"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_NRM_MA"/>
            <field scale="10" length="19" type="java.math.BigDecimal" name="T11_K_NADL"/>
        </fields>
    </file-description>
</file-descriptions>
```