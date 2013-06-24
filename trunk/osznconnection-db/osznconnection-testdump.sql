-- Set mysql user-defined variable - system locale id.
SELECT (@system_locale_id := `id`) FROM `locales` WHERE `system` = 1;

-- Oszns and calculation centers organizations
insert into organization(object_id) values (1),(2);
insert into organization_string_culture(id, locale_id, value) values 
(3, 1, UPPER('ОСЗН 1')), (3,2,UPPER('ОСЗН 1')), (4,@system_locale_id, UPPER('1')),
(5, 1, UPPER('Модуль начислений №1')), (5, 2, UPPER('Модуль начислений №1')), (6,@system_locale_id, UPPER('2')),
(7,@system_locale_id, 'jdbc/osznconnection_remote_resource');
-- Request files paths attribute values:
insert into organization_string_culture(id, locale_id, value) values 
(8,@system_locale_id,'in\\subs_reqs'),(9,@system_locale_id,'out\\subs_reqs'),
(10,@system_locale_id,'in\\fact'),(11,@system_locale_id,'out\\fact'),
(12,@system_locale_id,'in\\subs'),(13,@system_locale_id,'out\\subs'),
(14,@system_locale_id,'in\\charact'),(15,@system_locale_id,'out\\charact'),
(16,@system_locale_id,'in\\service'),(17,@system_locale_id,'out\\service'),
(18,@system_locale_id,'sprav'), (19,@system_locale_id,'out\\form2');
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
-- oszn:
(1,1,900,3,900), (1,1,901,4,901), (1,1,902,3,902), (1,1,904,2,904),
-- Request files paths attributes:
(1,1,915,8,915),(1,1,916,9,916),(1,1,917,10,917),(1,1,918,11,918),(1,1,919,12,919),(1,1,920,13,920),
(1,1,921,14,921),(1,1,922,15,922),
(1,1,923,16,923),(1,1,924,17,924),
(1,1,925,18,925), (1,1,928,19,928),
-- calculation center:
(1,2,900,5,900), (1,2,901,6,901), (1,2,904,3,904), (1,2,913,7,913);

-- User organizations
insert into service_association (pk_id, service_provider_type_id, calculation_center_id) values (1,1,2), (2,1,2);
insert into organization(object_id) values (3), (4);
insert into organization_string_culture(id, locale_id, value) values (20,@system_locale_id, UPPER('КП "ЖИЛКОМСЕРВИС"')),(21,@system_locale_id, UPPER('12345')),
(22,@system_locale_id,UPPER('ЛЕНИНСКИЙ ФИЛИАЛ КП "ЖИЛКОМСЕРВИС"')),(23,@system_locale_id, UPPER('123456')),
-- Root request files paths attribute values:
(24,@system_locale_id,'C:\\storage\\sz_files\\gks'),
(25,@system_locale_id,'C:\\storage\\sz_files\\gks'),
-- EDRPOU attribute values:
(26,@system_locale_id,'1'),
(27,@system_locale_id,'2');

insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,3,900,20,900), (1,3,901,21,901), (1,3,904,1,904), (1,3,914,1,914), (1,3,926,26,926), (1,3,927,24,927),
(1,4,900,22,900), (1,4,901,23,901), (1,4,903,3,903), (1,4,904,1,904), (1,4,914,2,914), (1,4,926,27,926), (1,4,927,25,927);

-- Request file descriptions --
INSERT INTO `request_file_description`(`id`,`request_file_type`,`date_pattern`) VALUES (1,'ACTUAL_PAYMENT','dd.MM.yyyy'),(2,'PAYMENT','dd.MM.yyyy'),(3,'BENEFIT','dd.MM.yyyy'),(4,'SUBSIDY','dd.MM.yyyy'),(5,'SUBSIDY_TARIF','dd.MM.yyyy'),(6,'DWELLING_CHARACTERISTICS','dd.MM.yyyy'),(7,'FACILITY_STREET_TYPE','dd.MM.yyyy'),(8,'FACILITY_STREET','dd.MM.yyyy'),(9,'FACILITY_SERVICE_TYPE','dd.MM.yyyy'),(10,'FACILITY_TARIF','dd.MM.yyyy'),(11,'FACILITY_FORM2','dd.MM.yyyy');
INSERT INTO `request_file_field_description`(`id`,`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES (1,1,'SUR_NAM','java.lang.String',30,NULL),(2,1,'F_NAM','java.lang.String',15,NULL),(3,1,'M_NAM','java.lang.String',20,NULL),(4,1,'INDX','java.lang.String',6,NULL),(5,1,'N_NAME','java.lang.String',30,NULL),(6,1,'N_CODE','java.lang.String',5,NULL),(7,1,'VUL_CAT','java.lang.String',7,NULL),(8,1,'VUL_NAME','java.lang.String',30,NULL),(9,1,'VUL_CODE','java.lang.String',5,NULL),(10,1,'BLD_NUM','java.lang.String',7,NULL),(11,1,'CORP_NUM','java.lang.String',2,NULL),(12,1,'FLAT','java.lang.String',9,NULL),(13,1,'OWN_NUM','java.lang.String',15,NULL),(14,1,'APP_NUM','java.lang.String',8,NULL),(15,1,'DAT_BEG','java.util.Date',8,NULL),(16,1,'DAT_END','java.util.Date',8,NULL),(17,1,'CM_AREA','java.math.BigDecimal',7,2),(18,1,'NM_AREA','java.math.BigDecimal',7,2),(19,1,'BLC_AREA','java.math.BigDecimal',5,2),(20,1,'FROG','java.math.BigDecimal',5,1),(21,1,'DEBT','java.math.BigDecimal',10,2),(22,1,'NUMB','java.lang.Integer',2,NULL),(23,1,'P1','java.math.BigDecimal',10,4),(24,1,'N1','java.math.BigDecimal',10,4),(25,1,'P2','java.math.BigDecimal',10,4),(26,1,'N2','java.math.BigDecimal',10,4),(27,1,'P3','java.math.BigDecimal',10,4),(28,1,'N3','java.math.BigDecimal',10,4),(29,1,'P4','java.math.BigDecimal',10,4),(30,1,'N4','java.math.BigDecimal',10,4),(31,1,'P5','java.math.BigDecimal',10,4),(32,1,'N5','java.math.BigDecimal',10,4),(33,1,'P6','java.math.BigDecimal',10,4),(34,1,'N6','java.math.BigDecimal',10,4),(35,1,'P7','java.math.BigDecimal',10,4),(36,1,'N7','java.math.BigDecimal',10,4),(37,1,'P8','java.math.BigDecimal',10,4),(38,1,'N8','java.math.BigDecimal',10,4),(39,2,'OWN_NUM','java.lang.String',15,NULL),(40,2,'REE_NUM','java.lang.Integer',2,NULL),(41,2,'OPP','java.lang.String',8,NULL),(42,2,'NUMB','java.lang.Integer',2,NULL),(43,2,'MARK','java.lang.Integer',2,NULL),(44,2,'CODE','java.lang.Integer',4,NULL),(45,2,'ENT_COD','java.lang.Integer',10,NULL),(46,2,'FROG','java.math.BigDecimal',5,1),(47,2,'FL_PAY','java.math.BigDecimal',9,2),(48,2,'NM_PAY','java.math.BigDecimal',9,2),(49,2,'DEBT','java.math.BigDecimal',9,2),(50,2,'CODE2_1','java.lang.Integer',6,NULL),(51,2,'CODE2_2','java.lang.Integer',6,NULL),(52,2,'CODE2_3','java.lang.Integer',6,NULL),(53,2,'CODE2_4','java.lang.Integer',6,NULL),(54,2,'CODE2_5','java.lang.Integer',6,NULL),(55,2,'CODE2_6','java.lang.Integer',6,NULL),(56,2,'CODE2_7','java.lang.Integer',6,NULL),(57,2,'CODE2_8','java.lang.Integer',6,NULL),(58,2,'NORM_F_1','java.math.BigDecimal',10,4),(59,2,'NORM_F_2','java.math.BigDecimal',10,4),(60,2,'NORM_F_3','java.math.BigDecimal',10,4),(61,2,'NORM_F_4','java.math.BigDecimal',10,4),(62,2,'NORM_F_5','java.math.BigDecimal',10,4),(63,2,'NORM_F_6','java.math.BigDecimal',10,4),(64,2,'NORM_F_7','java.math.BigDecimal',10,4),(65,2,'NORM_F_8','java.math.BigDecimal',10,4),(66,2,'OWN_NUM_SR','java.lang.String',15,NULL),(67,2,'DAT1','java.util.Date',8,NULL),(68,2,'DAT2','java.util.Date',8,NULL),(69,2,'OZN_PRZ','java.lang.Integer',1,NULL),(70,2,'DAT_F_1','java.util.Date',8,NULL),(71,2,'DAT_F_2','java.util.Date',8,NULL),(72,2,'DAT_FOP_1','java.util.Date',8,NULL),(73,2,'DAT_FOP_2','java.util.Date',8,NULL),(74,2,'ID_RAJ','java.lang.String',5,NULL),(75,2,'SUR_NAM','java.lang.String',30,NULL),(76,2,'F_NAM','java.lang.String',15,NULL),(77,2,'M_NAM','java.lang.String',20,NULL),(78,2,'IND_COD','java.lang.String',10,NULL),(79,2,'INDX','java.lang.String',6,NULL),(80,2,'N_NAME','java.lang.String',30,NULL),(81,2,'VUL_NAME','java.lang.String',30,NULL),(82,2,'BLD_NUM','java.lang.String',7,NULL),(83,2,'CORP_NUM','java.lang.String',2,NULL),(84,2,'FLAT','java.lang.String',9,NULL),(85,2,'CODE3_1','java.lang.Integer',6,NULL),(86,2,'CODE3_2','java.lang.Integer',6,NULL),(87,2,'CODE3_3','java.lang.Integer',6,NULL),(88,2,'CODE3_4','java.lang.Integer',6,NULL),(89,2,'CODE3_5','java.lang.Integer',6,NULL),(90,2,'CODE3_6','java.lang.Integer',6,NULL),(91,2,'CODE3_7','java.lang.Integer',6,NULL),(92,2,'CODE3_8','java.lang.Integer',6,NULL),(93,2,'OPP_SERV','java.lang.String',8,NULL),(94,2,'RESERV1','java.lang.Integer',10,NULL),(95,2,'RESERV2','java.lang.String',10,NULL),(96,3,'OWN_NUM','java.lang.String',15,NULL),(97,3,'REE_NUM','java.lang.Integer',2,NULL),(98,3,'OWN_NUM_SR','java.lang.String',15,NULL),(99,3,'FAM_NUM','java.lang.Integer',2,NULL),(100,3,'SUR_NAM','java.lang.String',30,NULL),(101,3,'F_NAM','java.lang.String',15,NULL),(102,3,'M_NAM','java.lang.String',20,NULL),(103,3,'IND_COD','java.lang.String',10,NULL),(104,3,'PSP_SER','java.lang.String',6,NULL),(105,3,'PSP_NUM','java.lang.String',6,NULL),(106,3,'OZN','java.lang.Integer',1,NULL),(107,3,'CM_AREA','java.math.BigDecimal',10,2),(108,3,'HEAT_AREA','java.math.BigDecimal',10,2),(109,3,'OWN_FRM','java.lang.Integer',6,NULL),(110,3,'HOSTEL','java.lang.Integer',2,NULL),(111,3,'PRIV_CAT','java.lang.Integer',3,NULL),(112,3,'ORD_FAM','java.lang.Integer',2,NULL),(113,3,'OZN_SQ_ADD','java.lang.Integer',1,NULL),(114,3,'OZN_ABS','java.lang.Integer',1,NULL),(115,3,'RESERV1','java.math.BigDecimal',10,2),(116,3,'RESERV2','java.lang.String',10,NULL),(117,4,'FIO','java.lang.String',30,NULL),(118,4,'ID_RAJ','java.lang.String',5,NULL),(119,4,'NP_CODE','java.lang.String',5,NULL),(120,4,'NP_NAME','java.lang.String',30,NULL),(121,4,'CAT_V','java.lang.String',7,NULL),(122,4,'VULCOD','java.lang.String',8,NULL),(123,4,'NAME_V','java.lang.String',30,NULL),(124,4,'BLD','java.lang.String',7,NULL),(125,4,'CORP','java.lang.String',2,NULL),(126,4,'FLAT','java.lang.String',9,NULL),(127,4,'RASH','java.lang.String',14,NULL),(128,4,'NUMB','java.lang.String',8,NULL),(129,4,'DAT1','java.util.Date',8,NULL),(130,4,'DAT2','java.util.Date',8,NULL),(131,4,'NM_PAY','java.math.BigDecimal',9,2),(132,4,'P1','java.math.BigDecimal',9,4),(133,4,'P2','java.math.BigDecimal',9,4),(134,4,'P3','java.math.BigDecimal',9,4),(135,4,'P4','java.math.BigDecimal',9,4),(136,4,'P5','java.math.BigDecimal',9,4),(137,4,'P6','java.math.BigDecimal',9,4),(138,4,'P7','java.math.BigDecimal',9,4),(139,4,'P8','java.math.BigDecimal',9,4),(140,4,'SM1','java.math.BigDecimal',9,2),(141,4,'SM2','java.math.BigDecimal',9,2),(142,4,'SM3','java.math.BigDecimal',9,2),(143,4,'SM4','java.math.BigDecimal',9,2),(144,4,'SM5','java.math.BigDecimal',9,2),(145,4,'SM6','java.math.BigDecimal',9,2),(146,4,'SM7','java.math.BigDecimal',9,2),(147,4,'SM8','java.math.BigDecimal',9,2),(148,4,'SB1','java.math.BigDecimal',9,2),(149,4,'SB2','java.math.BigDecimal',9,2),(150,4,'SB3','java.math.BigDecimal',9,2),(151,4,'SB4','java.math.BigDecimal',9,2),(152,4,'SB5','java.math.BigDecimal',9,2),(153,4,'SB6','java.math.BigDecimal',9,2),(154,4,'SB7','java.math.BigDecimal',9,2),(155,4,'SB8','java.math.BigDecimal',9,2),(156,4,'OB1','java.math.BigDecimal',9,2),(157,4,'OB2','java.math.BigDecimal',9,2),(158,4,'OB3','java.math.BigDecimal',9,2),(159,4,'OB4','java.math.BigDecimal',9,2),(160,4,'OB5','java.math.BigDecimal',9,2),(161,4,'OB6','java.math.BigDecimal',9,2),(162,4,'OB7','java.math.BigDecimal',9,2),(163,4,'OB8','java.math.BigDecimal',9,2),(164,4,'SUMMA','java.math.BigDecimal',13,2),(165,4,'NUMM','java.lang.Integer',2,NULL),(166,4,'SUBS','java.math.BigDecimal',13,2),(167,4,'KVT','java.lang.Integer',3,NULL),(168,5,'T11_DATA_T','java.lang.String',10,NULL),(169,5,'T11_DATA_E','java.lang.String',10,NULL),(170,5,'T11_DATA_R','java.lang.String',10,NULL),(171,5,'T11_MARK','java.lang.Integer',3,NULL),(172,5,'T11_TARN','java.lang.Integer',6,NULL),(173,5,'T11_CODE1','java.lang.Integer',3,NULL),(174,5,'T11_CODE2','java.lang.Integer',6,NULL),(175,5,'T11_COD_NA','java.lang.String',40,NULL),(176,5,'T11_CODE3','java.lang.Integer',6,NULL),(177,5,'T11_NORM_U','java.math.BigDecimal',19,10),(178,5,'T11_NOR_US','java.math.BigDecimal',19,10),(179,5,'T11_CODE_N','java.lang.Integer',3,NULL),(180,5,'T11_COD_ND','java.lang.Integer',3,NULL),(181,5,'T11_CD_UNI','java.lang.Integer',3,NULL),(182,5,'T11_CS_UNI','java.math.BigDecimal',19,10),(183,5,'T11_NORM','java.math.BigDecimal',19,10),(184,5,'T11_NRM_DO','java.math.BigDecimal',19,10),(185,5,'T11_NRM_MA','java.math.BigDecimal',19,10),(186,5,'T11_K_NADL','java.math.BigDecimal',19,10),(187,6,'COD','java.lang.Integer',4,NULL),(188,6,'CDPR','java.lang.Integer',12,NULL),(189,6,'NCARD','java.lang.Integer',7,NULL),(190,6,'IDCODE','java.lang.String',10,NULL),(191,6,'PASP','java.lang.String',14,NULL),(192,6,'FIO','java.lang.String',50,NULL),(193,6,'IDPIL','java.lang.String',10,NULL),(194,6,'PASPPIL','java.lang.String',14,NULL),(195,6,'FIOPIL','java.lang.String',50,NULL),(196,6,'INDEX','java.lang.Integer',6,NULL),(197,6,'CDUL','java.lang.Integer',5,NULL),(198,6,'HOUSE','java.lang.String',7,NULL),(199,6,'BUILD','java.lang.String',2,NULL),(200,6,'APT','java.lang.String',4,NULL),(201,6,'VL','java.lang.Integer',3,NULL),(202,6,'PLZAG','java.math.BigDecimal',6,2),(203,6,'PLOPAL','java.math.BigDecimal',6,2),(204,7,'KLKUL_CODE','java.lang.Integer',3,NULL),(205,7,'KLKUL_NAME','java.lang.String',7,NULL),(206,8,'KL_CODERN','java.lang.Integer',5,NULL),(207,8,'KL_CODEUL','java.lang.Integer',5,NULL),(208,8,'KL_NAME','java.lang.String',50,NULL),(209,8,'KL_CODEKUL','java.lang.Integer',3,NULL),(210,9,'COD','java.lang.Integer',4,NULL),(211,9,'CDPR','java.lang.Integer',12,NULL),(212,9,'NCARD','java.lang.Integer',7,NULL),(213,9,'IDCODE','java.lang.String',10,NULL),(214,9,'PASP','java.lang.String',14,NULL),(215,9,'FIO','java.lang.String',50,NULL),(216,9,'IDPIL','java.lang.String',10,NULL),(217,9,'PASPPIL','java.lang.String',14,NULL),(218,9,'FIOPIL','java.lang.String',50,NULL),(219,9,'INDEX','java.lang.Integer',6,NULL),(220,9,'CDUL','java.lang.Integer',5,NULL),(221,9,'HOUSE','java.lang.String',7,NULL),(222,9,'BUILD','java.lang.String',2,NULL),(223,9,'APT','java.lang.String',4,NULL),(224,9,'KAT','java.lang.Integer',4,NULL),(225,9,'LGCODE','java.lang.Integer',4,NULL),(226,9,'YEARIN','java.lang.Integer',4,NULL),(227,9,'MONTHIN','java.lang.Integer',2,NULL),(228,9,'YEAROUT','java.lang.Integer',4,NULL),(229,9,'MONTHOUT','java.lang.Integer',2,NULL),(230,9,'RAH','java.lang.String',25,NULL),(231,9,'RIZN','java.lang.Integer',6,NULL),(232,9,'TARIF','java.lang.Integer',10,NULL),(233,10,'TAR_CODE','java.lang.Integer',10,NULL),(234,10,'TAR_CDPLG','java.lang.Integer',10,NULL),(235,10,'TAR_SERV','java.lang.Integer',10,NULL),(236,10,'TAR_DATEB','java.util.Date',8,NULL),(237,10,'TAR_DATEE','java.util.Date',8,NULL),(238,10,'TAR_COEF','java.math.BigDecimal',11,2),(239,10,'TAR_COST','java.math.BigDecimal',14,7),(240,10,'TAR_UNIT','java.lang.Integer',10,NULL),(241,10,'TAR_METER','java.lang.Integer',3,NULL),(242,10,'TAR_NMBAS','java.math.BigDecimal',11,2),(243,10,'TAR_NMSUP','java.math.BigDecimal',11,2),(244,10,'TAR_NMUBS','java.math.BigDecimal',11,4),(245,10,'TAR_NMUSP','java.math.BigDecimal',11,4),(246,10,'TAR_NMUMX','java.math.BigDecimal',11,4),(247,10,'TAR_TPNMB','java.lang.Integer',10,NULL),(248,10,'TAR_TPNMS','java.lang.Integer',10,NULL),(249,10,'TAR_NMUPL','java.lang.Integer',3,NULL),(250,10,'TAR_PRIV','java.lang.Integer',10,NULL),(251,11,'CDPR','java.lang.Integer',12,NULL),(252,11,'IDCODE','java.lang.String',10,NULL),(253,11,'FIO','java.lang.String',50,NULL),(254,11,'PPOS','java.lang.String',15,NULL),(255,11,'RS','java.lang.String',25,NULL),(256,11,'YEARIN','java.lang.Integer',4,NULL),(257,11,'MONTHIN','java.lang.Integer',2,NULL),(258,11,'LGCODE','java.lang.Integer',4,NULL),(259,11,'DATA1','java.util.Date',8,NULL),(260,11,'DATA2','java.util.Date',8,NULL),(261,11,'LGKOL','java.lang.Integer',2,NULL),(262,11,'LGKAT','java.lang.String',3,NULL),(263,11,'LGPRC','java.lang.Integer',3,NULL),(264,11,'SUMM','java.math.BigDecimal',8,2),(265,11,'FACT','java.math.BigDecimal',19,6),(266,11,'TARIF','java.math.BigDecimal',14,7),(267,11,'FLAG','java.lang.Integer',1,NULL);

-- Request files -- 
INSERT INTO `request_file_group`(`id`, `status`) VALUES (1, 110);
INSERT INTO `request_file`(id, group_id, organization_id, `name`, `directory`, `registry`, `begin_date`, `loaded`, `type`,
    `user_organization_id`, `status`) 
    VALUES
(1,1,1,'A_123405.dbf', 'AB', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 3, 110),
(2,1,1,'AF123405.dbf', 'AB', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 3, 110),
(3,null,1,'TARIF12.dbf', 'AB', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3, 110),
(4,null,1,'B1170710.dbf', 'AB', 1, CURRENT_TIMESTAMP, '2011-01-12', 4, 4, 110),
(5,null,1,'J000001.dbf', '1234\\AB', 1, CURRENT_TIMESTAMP, '2011-01-12', 5, 4, 110),
-- (6,null,1,'J000002.dbf', '123456\\AB', 1, CURRENT_TIMESTAMP, '2011-01-12', 'SUBSIDY', 4, 110),
(7,null,1,'12345678.a01','',1,CURRENT_TIMESTAMP,'2012-01-01',6 ,4, 110),
(8,null,1,'12345678.b01','',1,CURRENT_TIMESTAMP,'2012-01-01',9 ,4, 110);

-- Benefit
insert into `benefit`(`OWN_NUM`, `OWN_NUM_SR`, `OZN`, `F_NAM`, `M_NAM`, `SUR_NAM`, `request_file_id`, `IND_COD`, `PSP_NUM`)
values
 ('32', '0000457', '1', 'Петр','Петрович','Петров', 2, '2142426432', null);

-- Payments
insert into `payment`(`OWN_NUM`, `OWN_NUM_SR`, `F_NAM`, `M_NAM`, `SUR_NAM`, `N_NAME`, `VUL_NAME`, `BLD_NUM`, `CORP_NUM`, `FLAT`, `DAT1`, `request_file_id`)
values
('32', '0000457','Матвей', 'Матвеевич', 'Матвеев', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '19', '01.05.2011',1);
-- ('32', '0000458','Матвей2', 'Матвеевич2', 'Матвеев2', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '40', '2010-09-09',1);

-- Actual payments
insert into `actual_payment`(`OWN_NUM`, `F_NAM`, `M_NAM`, `SUR_NAM`, `N_NAME`, `VUL_CAT`, `VUL_NAME`, `VUL_CODE`, `BLD_NUM`, `CORP_NUM`, `FLAT`, `DAT_BEG`, `request_file_id`)
values
    ('107374638','Матвей', 'Матвеевич', 'Матвеев', 'Харьков', 'УЛ', 'Франтишека Крала', '123', '25-/А','', '19', '09.09.2010', 4);
--    ('123','Матвей1', 'Матвеевич1', 'Матвеев1', 'Харьков', 'УЛ.', 'ФРАНТИШЕКА КРАЛА', '123', '25А','', '19', '2009-01-01', 4);

-- Subsidies
insert into `subsidy`(`RASH`, `first_name`, `middle_name`, `last_name`, `FIO`, `NP_NAME`, `CAT_V`, `NAME_V`, `VULCOD`, `BLD`, `CORP`, `FLAT`, `DAT1`, `request_file_id`)
values
--    ('107374638','М', '', 'Матвеев', 'Матвеев М...', 'Харьков', 'УЛ', 'ФРАНТИШЕКА КРАЛА', '123', '25-/А','', '19', '09.09.2010', 5),
    ('107374638','М', '', 'Матвеев', 'Матвеев M...', 'Харьков', 'УЛ', 'ФРАНТИШЕКА КРАЛА', '123', '25А','','19', '09.09.2010', 5),
    ('107374638','М', '', 'Матвеев', 'Матвеев M...', 'Харьков', 'УЛ', 'ФРАНТИШЕКА КРАЛА', '123', '25А','','19', '09.09.2010', 5);
--    ('107374638','М', '', 'Матвеев1', 'Матвеев1 M...', 'Харьков', 'УЛ', 'ФРАНТИШЕКА КРАЛА', '123', '25А','','19', '09.09.2010', 6);

-- Dwelling characteristics
insert into `dwelling_characteristics`(`IDCODE`, `FIO`, `first_name`, `middle_name`, `last_name`, `date`, `city`, `CDUL`, `HOUSE`, `BUILD`, `APT`, `request_file_id`)
values
    ('107374638','Матвеев Матвей Матвеевич', 'Матвей', 'Матвеевич', 'Матвеев', '2012-01-02', 'Харьков', '123', '25А','','19', 7);
--    ('107374638','Матвеев2 Матвей2 Матвеевич2', 'Матвей2', 'Матвеевич2', 'Матвеев2', '2012-01-02', 'Харьков', '124', '25А','','19', 7);

-- Facility service types
insert into `facility_service_type`(`IDCODE`, `FIO`, `first_name`, `middle_name`, `last_name`, `date`, `city`, `CDUL`, `HOUSE`, `BUILD`, `APT`, `request_file_id`)
values
    ('107374638','Матвеев Матвей Матвеевич', 'Матвей', 'Матвеевич', 'Матвеев', '2012-01-02', 'Харьков', '123', '25А','','19', 8);

-- INSERT INTO `person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street`, `street_type`, `building_num`, `building_corp`, `apartment`, `account_number`, `pu_account_number`, `oszn_id`, `calc_center_id`, `user_organization_id`) 
-- VALUES(UPPER('Матвей'),UPPER('Матвеевич'),UPPER('Матвеев'),UPPER('Харьков'),UPPER('ФРАНТИШЕКА КРАЛА'),UPPER('УЛ'),UPPER('25-/А'),'',UPPER('19'),'1000001109','107374638',1,2,4);

-- Address corrections
-- Calculation center's corrections
INSERT INTO `street_type_correction`(`id`, `object_id`, `correction`, `organization_id`, `external_id`, `module_id`) VALUES
(1,10000,UPPER('Б-Р'),2,'1',0), (2,10001,UPPER('М'),2,'1',0), (3,10002,UPPER('М-Н'),2,'1',0), (4,10003,UPPER('ПЕР'),2,'1',0), (5,10004,UPPER('ПЛ'),2,'1',0), (6,10005,UPPER('П'),2,'1',0),
(7,10006,UPPER('ПОС'),2,'1',0), (8,10007,UPPER('ПР-Д'),2,'1',0), (9,10008,UPPER('ПРОСП'),2,'1',0), (10,10009,UPPER('СП'),2,'1',0), (11,10010,UPPER('Т'),2,'1',0), (12,10011,UPPER('ТУП'),2,'1',0),
(13,10012,UPPER('УЛ'),2,'1',0), (14,10013,UPPER('ШОССЕ'),2,'1',0), (15,10014,UPPER('НАБ'),2,'1',0), (16,10015,UPPER('В-Д'),2,'1',0), (17,10016,UPPER('СТ'),2,'1',0);

insert into city_correction(id, organization_id, correction, object_id, module_id) values (1,2,UPPER('Новосибирск'),1,0);
insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id, street_type_correction_id) values (1,2,UPPER('Терешковой'),1,0,1,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, module_id, parent_id) values (2,UPPER('10'),UPPER('1'),3,0,1);

insert into city_correction(id, organization_id, correction, object_id, module_id) values (2,2,UPPER('Харьков'),3,0);
insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id, street_type_correction_id) values (2,2,UPPER('Косиора'),4,0,2,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, module_id, parent_id) values (2,UPPER('154А'),UPPER(''),6,0,2);

insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id, street_type_correction_id) 
    values (3,2,UPPER('ФРАНТИШЕКА КРАЛА'),5,0,2,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, module_id, parent_id) values (2,UPPER('25А'),UPPER(''),7,0,3);
insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id, street_type_correction_id) 
    values (4,2,UPPER('ФРАНТИШЕКА КРАЛА'),5,0,2,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, module_id, parent_id) values (2,UPPER('23'),UPPER(''),8,0,4);

insert into district_correction(organization_id, correction, object_id, module_id, parent_id) values (2,UPPER('Центральный'),3,0,2);

-- OSZN corrections
-- INSERT INTO `street_type_correction`(`id`, `object_id`, `correction`, `organization_id`, `module_id`, `user_organization_id`) VALUES
-- (18,10012,UPPER('УЛ'),1,0,4);
-- insert into city_correction(id, organization_id, correction, object_id, module_id, user_organization_id) values (3,1,UPPER('Харьков'),3,0,4);
-- insert into street_correction(id, organization_id, correction, object_id, module_id, user_organization_id, parent_id, street_type_correction_id, external_id)
--    values (5,1,UPPER('ФРАНТИШЕКА КРАЛА'),5,0,4,3,18,111);

-- insert into district_correction(organization_id, correction, object_id, module_id, parent_id) values (2,UPPER('Центральный123'),3,0,2);

-- insert into city_correction(id, organization_id, correction, object_id, module_id) values (3,1,UPPER('Харьков'),3,0);
-- insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id) values (4,1,UPPER('ФРАНТИШЕКА КРАЛА1'),5,0,3);

/* Corrections for testing situations where correction's building belongs another street than local address base' building */

-- insert into city_correction(id, organization_id, correction, object_id, module_id) values (3,1,UPPER('Новосибирск'),1,0);
-- insert into street_correction(id, organization_id, correction, object_id, module_id, parent_id) values (4,1,UPPER('Терешковой'),1,0,3);
-- insert into building_correction(organization_id, correction, correction_corp, object_id, module_id, parent_id) values (1,'25','',7,0,4);


-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code, internal_organization_id, user_organization_id) values
(1,UPPER('мiсцевих Рад'),1,UPPER('1'),0,3), (2,UPPER('ГОС'),1,UPPER('1'),0,null),
(1,UPPER('кооперативна'),2,UPPER('1'),0,3), (2,UPPER('КООП'),2,UPPER('2'),0,null),
(1,UPPER('приватна'),5,UPPER('5'),0,3), (2,UPPER('ВЫК'),5,UPPER('5'),0,null),
(1,UPPER('приватизована'),6,UPPER('6'),0,3), (2,UPPER('ЧАС'),6,UPPER('6'),0,null);

-- Privileges corrections
insert into privilege_correction(organization_id, correction, object_id, organization_code, internal_organization_id, user_organization_id) values
(2,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ'),15,UPPER('34'),0,null),
(1,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ'),15,UPPER('100'),0,3);

-- Tarif
INSERT INTO `subsidy_tarif`(`T11_CS_UNI`, `T11_CODE2`, `request_file_id`, `T11_CODE1`) values ('0','123',3,'1');

-- config test
INSERT INTO `config`(`name`, `value`) VALUES ('FACILITY_STREET_TYPE_REFERENCE_FILENAME_MASK', 'KLKATUL\.DBF');
INSERT INTO `config`(`name`, `value`) VALUES ('FACILITY_STREET_REFERENCE_FILENAME_MASK', 'KLUL\.DBF');
-- insert into config(`name`, `value`) values ('ADDRESS_IMPORT_FILE_STORAGE_DIR', 'D:\\Artem\\Projects\\org.complitex\\storage\\import');

-- test users
-- User '1'
insert into `first_name` (`id`, `name`) values('3','1');
insert into `last_name` (`id`, `name`) values('3','1');
insert into `middle_name` (`id`, `name`) values('3','1');

insert into `user_info` (`object_id`) values(3);
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','3','1000','3','1000');
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','3','1001','3','1001');
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','3','1002','3','1002');

insert into `user` (`id`, `login`, `password`, `user_info_object_id`) values('3','1','c4ca4238a0b923820dcc509a6f75849b','3');
insert into `user_organization` (`id`, `user_id`, `organization_object_id`, `main`) values('1','3','3','1');
insert into `usergroup` (`id`, `login`, `group_name`) values('4','1','EMPLOYEES');

-- User '2'
insert into `first_name` (`id`, `name`) values('4','2');
insert into `last_name` (`id`, `name`) values('4','2');
insert into `middle_name` (`id`, `name`) values('4','2');

insert into `user_info` (`object_id`) values(4);
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','4','1000','4','1000');
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','4','1001','4','1001');
insert into `user_info_attribute` (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) values('1','4','1002','4','1002');

insert into `user` (`id`, `login`, `password`, `user_info_object_id`) values('4','2','c81e728d9d4c2f636f067f89cc14862c','4');
insert into `user_organization` (`id`, `user_id`, `organization_object_id`, `main`) values('2','4','4','1');
insert into `usergroup` (`id`, `login`, `group_name`) values('5','2','EMPLOYEES');

update `sequence` set `sequence_value` = 5 where `sequence_name` = 'user_info'; 
