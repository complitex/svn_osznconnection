-- Set mysql user-defined variable - system locale id.
SELECT (@system_locale_id := `id`) FROM `locales` WHERE `system` = 1;

-- Oszns and calculation centers organizations
insert into organization(object_id) values (1),(2);
insert into organization_string_culture(id, locale_id, value) values 
(3, 1, UPPER('ОСЗН 1')), (3,2,UPPER('ОСЗН 1')), (4,@system_locale_id, UPPER('1')),
(5, 1, UPPER('Модуль начислений №1')), (5, 2, UPPER('Модуль начислений №1')), (6,@system_locale_id, UPPER('2')),
(7,@system_locale_id, 'jdbc/osznconnectionRemoteResource');
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
insert into service_association (pk_id, service_provider_type_id, calculation_center_id) values (1,1,2),
  (2,2,2),(3,3,2),(4,4,2),(5,5,2),(6,6,2),(7,7,2),(8,8,2),
  (9,1,2);

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
  (2,3,914,2,914),(3,3,914,3,914),(4,3,914,4,914),(5,3,914,5,914),(6,3,914,6,914),(7,3,914,7,914),(8,3,914,8,914),
(1,4,900,22,900), (1,4,901,23,901), (1,4,903,3,903), (1,4,904,1,904), (1,4,914,9,914), (1,4,926,27,926), (1,4,927,25,927);

-- Request File Descriptions --
INSERT INTO `request_file_description` VALUES (1,'ACTUAL_PAYMENT','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (2,'PAYMENT','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (3,'BENEFIT','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (4,'SUBSIDY','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (5,'SUBSIDY_J_FILE','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (6,'SUBSIDY_S_FILE','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (7,'SUBSIDY_TARIF','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (8,'DWELLING_CHARACTERISTICS','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (9,'FACILITY_STREET_TYPE','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (10,'FACILITY_STREET','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (11,'FACILITY_SERVICE_TYPE','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (12,'FACILITY_TARIF','dd.MM.yyyy');
INSERT INTO `request_file_description` VALUES (13,'FACILITY_FORM2','dd.MM.yyyy');

-- Request File Descriptions Fields--
INSERT INTO `request_file_field_description` VALUES (1,1,'SUR_NAM','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (2,1,'F_NAM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (3,1,'M_NAM','java.lang.String',20,NULL);
INSERT INTO `request_file_field_description` VALUES (4,1,'INDX','java.lang.String',6,NULL);
INSERT INTO `request_file_field_description` VALUES (5,1,'N_NAME','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (6,1,'N_CODE','java.lang.String',5,NULL);
INSERT INTO `request_file_field_description` VALUES (7,1,'VUL_CAT','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (8,1,'VUL_NAME','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (9,1,'VUL_CODE','java.lang.String',5,NULL);
INSERT INTO `request_file_field_description` VALUES (10,1,'BLD_NUM','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (11,1,'CORP_NUM','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (12,1,'FLAT','java.lang.String',9,NULL);
INSERT INTO `request_file_field_description` VALUES (13,1,'OWN_NUM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (14,1,'APP_NUM','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (15,1,'DAT_BEG','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (16,1,'DAT_END','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (17,1,'CM_AREA','java.math.BigDecimal',7,2);
INSERT INTO `request_file_field_description` VALUES (18,1,'NM_AREA','java.math.BigDecimal',7,2);
INSERT INTO `request_file_field_description` VALUES (19,1,'BLC_AREA','java.math.BigDecimal',5,2);
INSERT INTO `request_file_field_description` VALUES (20,1,'FROG','java.math.BigDecimal',5,1);
INSERT INTO `request_file_field_description` VALUES (21,1,'DEBT','java.math.BigDecimal',10,2);
INSERT INTO `request_file_field_description` VALUES (22,1,'NUMB','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (23,1,'P1','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (24,1,'N1','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (25,1,'P2','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (26,1,'N2','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (27,1,'P3','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (28,1,'N3','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (29,1,'P4','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (30,1,'N4','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (31,1,'P5','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (32,1,'N5','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (33,1,'P6','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (34,1,'N6','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (35,1,'P7','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (36,1,'N7','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (37,1,'P8','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (38,1,'N8','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (39,2,'OWN_NUM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (40,2,'REE_NUM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (41,2,'OPP','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (42,2,'NUMB','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (43,2,'MARK','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (44,2,'CODE','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (45,2,'ENT_COD','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (46,2,'FROG','java.math.BigDecimal',5,1);
INSERT INTO `request_file_field_description` VALUES (47,2,'FL_PAY','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (48,2,'NM_PAY','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (49,2,'DEBT','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (50,2,'CODE2_1','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (51,2,'CODE2_2','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (52,2,'CODE2_3','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (53,2,'CODE2_4','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (54,2,'CODE2_5','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (55,2,'CODE2_6','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (56,2,'CODE2_7','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (57,2,'CODE2_8','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (58,2,'NORM_F_1','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (59,2,'NORM_F_2','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (60,2,'NORM_F_3','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (61,2,'NORM_F_4','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (62,2,'NORM_F_5','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (63,2,'NORM_F_6','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (64,2,'NORM_F_7','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (65,2,'NORM_F_8','java.math.BigDecimal',10,4);
INSERT INTO `request_file_field_description` VALUES (66,2,'OWN_NUM_SR','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (67,2,'DAT1','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (68,2,'DAT2','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (69,2,'OZN_PRZ','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (70,2,'DAT_F_1','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (71,2,'DAT_F_2','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (72,2,'DAT_FOP_1','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (73,2,'DAT_FOP_2','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (74,2,'ID_RAJ','java.lang.String',5,NULL);
INSERT INTO `request_file_field_description` VALUES (75,2,'SUR_NAM','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (76,2,'F_NAM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (77,2,'M_NAM','java.lang.String',20,NULL);
INSERT INTO `request_file_field_description` VALUES (78,2,'IND_COD','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (79,2,'INDX','java.lang.String',6,NULL);
INSERT INTO `request_file_field_description` VALUES (80,2,'N_NAME','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (81,2,'VUL_NAME','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (82,2,'BLD_NUM','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (83,2,'CORP_NUM','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (84,2,'FLAT','java.lang.String',9,NULL);
INSERT INTO `request_file_field_description` VALUES (85,2,'CODE3_1','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (86,2,'CODE3_2','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (87,2,'CODE3_3','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (88,2,'CODE3_4','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (89,2,'CODE3_5','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (90,2,'CODE3_6','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (91,2,'CODE3_7','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (92,2,'CODE3_8','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (93,2,'OPP_SERV','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (94,2,'RESERV1','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (95,2,'RESERV2','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (96,3,'OWN_NUM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (97,3,'REE_NUM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (98,3,'OWN_NUM_SR','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (99,3,'FAM_NUM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (100,3,'SUR_NAM','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (101,3,'F_NAM','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (102,3,'M_NAM','java.lang.String',20,NULL);
INSERT INTO `request_file_field_description` VALUES (103,3,'IND_COD','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (104,3,'PSP_SER','java.lang.String',6,NULL);
INSERT INTO `request_file_field_description` VALUES (105,3,'PSP_NUM','java.lang.String',6,NULL);
INSERT INTO `request_file_field_description` VALUES (106,3,'OZN','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (107,3,'CM_AREA','java.math.BigDecimal',10,2);
INSERT INTO `request_file_field_description` VALUES (108,3,'HEAT_AREA','java.math.BigDecimal',10,2);
INSERT INTO `request_file_field_description` VALUES (109,3,'OWN_FRM','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (110,3,'HOSTEL','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (111,3,'PRIV_CAT','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (112,3,'ORD_FAM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (113,3,'OZN_SQ_ADD','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (114,3,'OZN_ABS','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (115,3,'RESERV1','java.math.BigDecimal',10,2);
INSERT INTO `request_file_field_description` VALUES (116,3,'RESERV2','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (117,4,'FIO','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (118,4,'ID_RAJ','java.lang.String',5,NULL);
INSERT INTO `request_file_field_description` VALUES (119,4,'NP_CODE','java.lang.String',5,NULL);
INSERT INTO `request_file_field_description` VALUES (120,4,'NP_NAME','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (121,4,'CAT_V','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (122,4,'VULCOD','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (123,4,'NAME_V','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (124,4,'BLD','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (125,4,'CORP','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (126,4,'FLAT','java.lang.String',9,NULL);
INSERT INTO `request_file_field_description` VALUES (127,4,'RASH','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (128,4,'NUMB','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (129,4,'DAT1','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (130,4,'DAT2','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (131,4,'NM_PAY','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (132,4,'P1','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (133,4,'P2','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (134,4,'P3','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (135,4,'P4','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (136,4,'P5','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (137,4,'P6','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (138,4,'P7','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (139,4,'P8','java.math.BigDecimal',9,4);
INSERT INTO `request_file_field_description` VALUES (140,4,'SM1','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (141,4,'SM2','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (142,4,'SM3','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (143,4,'SM4','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (144,4,'SM5','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (145,4,'SM6','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (146,4,'SM7','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (147,4,'SM8','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (148,4,'SB1','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (149,4,'SB2','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (150,4,'SB3','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (151,4,'SB4','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (152,4,'SB5','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (153,4,'SB6','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (154,4,'SB7','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (155,4,'SB8','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (156,4,'OB1','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (157,4,'OB2','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (158,4,'OB3','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (159,4,'OB4','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (160,4,'OB5','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (161,4,'OB6','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (162,4,'OB7','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (163,4,'OB8','java.math.BigDecimal',9,2);
INSERT INTO `request_file_field_description` VALUES (164,4,'SUMMA','java.math.BigDecimal',13,2);
INSERT INTO `request_file_field_description` VALUES (165,4,'NUMM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (166,4,'SUBS','java.math.BigDecimal',13,2);
INSERT INTO `request_file_field_description` VALUES (167,4,'KVT','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (168,5,'DOM','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (169,5,'REG','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (170,5,'LS','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (171,5,'DELO','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (172,5,'TOT','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (173,5,'PERIOD','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (174,5,'FIO','java.lang.String',30,NULL);
INSERT INTO `request_file_field_description` VALUES (175,5,'ADRES','java.lang.String',34,NULL);
INSERT INTO `request_file_field_description` VALUES (176,5,'NKW','java.lang.String',9,NULL);
INSERT INTO `request_file_field_description` VALUES (177,5,'KWART','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (178,5,'OTOPL','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (179,5,'PODOGR','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (180,5,'WODA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (181,5,'GAZ','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (182,5,'ELEKTR','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (183,5,'STOKI','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (184,5,'TOT_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (185,5,'KWART_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (186,5,'OTOPL_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (187,5,'GORWODA_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (188,5,'WODA_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (189,5,'GAZ_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (190,5,'ELEKTR_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (191,5,'STOKI_O','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (192,5,'VC','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (193,5,'PLE','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (194,5,'BEGIN0','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (195,5,'END0','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (196,5,'PR_KV','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (197,6,'JEK','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (198,6,'P_ACCOUNT','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (199,6,'DT','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (200,6,'DDPP','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (201,6,'N_DEL','java.lang.String',8,NULL);
INSERT INTO `request_file_field_description` VALUES (202,6,'FIO','java.lang.String',20,NULL);
INSERT INTO `request_file_field_description` VALUES (203,6,'NA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (204,6,'OPTOT','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (205,6,'OP','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (206,6,'KWART','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (207,6,'KWARTG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (208,6,'NAKWART','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (209,6,'WODA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (210,6,'WODAG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (211,6,'NAWODA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (212,6,'WODAG1','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (213,6,'NAWODA1','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (214,6,'OTOPL','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (215,6,'OTOPLG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (216,6,'NAOTOPL','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (217,6,'GORWODA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (218,6,'GORWODAG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (219,6,'NAGORWODA','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (220,6,'STOKI','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (221,6,'STOKIG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (222,6,'NASTOKI','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (223,6,'GAZ','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (224,6,'GAZG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (225,6,'NAGAZ','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (226,6,'NAELEKTR','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (227,6,'ELEKTR','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (228,6,'ELEKTRG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (229,6,'NARADIO','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (230,6,'NATELEANT','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (231,6,'MUSOR','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (232,6,'MUSORG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (233,6,'ODS','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (234,6,'ODSG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (235,6,'LIFT','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (236,6,'LIFTG','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (237,6,'VC','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (238,6,'PR','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (239,6,'TOTRASKLAD','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (240,6,'I_GAZ','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (241,6,'I_WODA','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (242,6,'I_GORWODA','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (243,6,'I_STOKI','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (244,6,'I_OTOPL','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (245,6,'I_KWART','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (246,6,'I_ELEKTR','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (247,6,'I_MUSOR','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (248,6,'I_LIFT','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (249,6,'I_NASOS','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (250,6,'I_ODS','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (251,6,'DOM','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (252,6,'NACHSU','java.lang.String',6,NULL);
INSERT INTO `request_file_field_description` VALUES (253,6,'REG','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (254,6,'NDWODA','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (255,6,'NDGORW','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (256,6,'NDSTOKI','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (257,6,'NDOTOPL','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (258,6,'NDELEC','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (259,6,'NDMUSOR','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (260,6,'NDLIFT','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (261,6,'NDODS','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (262,6,'NDGAZ','java.lang.Integer',8,NULL);
INSERT INTO `request_file_field_description` VALUES (263,6,'PLE','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (264,6,'BEG0','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (265,6,'END0','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (266,6,'PR_KV','java.lang.Integer',1,NULL);
INSERT INTO `request_file_field_description` VALUES (267,6,'BEG_IH','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (268,6,'END_IH','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (269,6,'COUNTM','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (270,6,'NADL','java.math.BigDecimal',7,2);
INSERT INTO `request_file_field_description` VALUES (271,7,'T11_DATA_T','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (272,7,'T11_DATA_E','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (273,7,'T11_DATA_R','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (274,7,'T11_MARK','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (275,7,'T11_TARN','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (276,7,'T11_CODE1','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (277,7,'T11_CODE2','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (278,7,'T11_COD_NA','java.lang.String',40,NULL);
INSERT INTO `request_file_field_description` VALUES (279,7,'T11_CODE3','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (280,7,'T11_NORM_U','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (281,7,'T11_NOR_US','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (282,7,'T11_CODE_N','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (283,7,'T11_COD_ND','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (284,7,'T11_CD_UNI','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (285,7,'T11_CS_UNI','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (286,7,'T11_NORM','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (287,7,'T11_NRM_DO','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (288,7,'T11_NRM_MA','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (289,7,'T11_K_NADL','java.math.BigDecimal',19,10);
INSERT INTO `request_file_field_description` VALUES (290,8,'COD','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (291,8,'CDPR','java.lang.Integer',12,NULL);
INSERT INTO `request_file_field_description` VALUES (292,8,'NCARD','java.lang.Integer',7,NULL);
INSERT INTO `request_file_field_description` VALUES (293,8,'IDCODE','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (294,8,'PASP','java.lang.String',14,NULL);
INSERT INTO `request_file_field_description` VALUES (295,8,'FIO','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (296,8,'IDPIL','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (297,8,'PASPPIL','java.lang.String',14,NULL);
INSERT INTO `request_file_field_description` VALUES (298,8,'FIOPIL','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (299,8,'INDEX','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (300,8,'CDUL','java.lang.Integer',5,NULL);
INSERT INTO `request_file_field_description` VALUES (301,8,'HOUSE','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (302,8,'BUILD','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (303,8,'APT','java.lang.String',4,NULL);
INSERT INTO `request_file_field_description` VALUES (304,8,'VL','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (305,8,'PLZAG','java.math.BigDecimal',6,2);
INSERT INTO `request_file_field_description` VALUES (306,8,'PLOPAL','java.math.BigDecimal',6,2);
INSERT INTO `request_file_field_description` VALUES (307,9,'KLKUL_CODE','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (308,9,'KLKUL_NAME','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (309,10,'KL_CODERN','java.lang.Integer',5,NULL);
INSERT INTO `request_file_field_description` VALUES (310,10,'KL_CODEUL','java.lang.Integer',5,NULL);
INSERT INTO `request_file_field_description` VALUES (311,10,'KL_NAME','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (312,10,'KL_CODEKUL','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (313,11,'COD','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (314,11,'CDPR','java.lang.Integer',12,NULL);
INSERT INTO `request_file_field_description` VALUES (315,11,'NCARD','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (316,11,'IDCODE','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (317,11,'PASP','java.lang.String',14,NULL);
INSERT INTO `request_file_field_description` VALUES (318,11,'FIO','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (319,11,'IDPIL','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (320,11,'PASPPIL','java.lang.String',14,NULL);
INSERT INTO `request_file_field_description` VALUES (321,11,'FIOPIL','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (322,11,'INDEX','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (323,11,'CDUL','java.lang.Integer',5,NULL);
INSERT INTO `request_file_field_description` VALUES (324,11,'HOUSE','java.lang.String',7,NULL);
INSERT INTO `request_file_field_description` VALUES (325,11,'BUILD','java.lang.String',2,NULL);
INSERT INTO `request_file_field_description` VALUES (326,11,'APT','java.lang.String',4,NULL);
INSERT INTO `request_file_field_description` VALUES (327,11,'KAT','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (328,11,'LGCODE','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (329,11,'YEARIN','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (330,11,'MONTHIN','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (331,11,'YEAROUT','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (332,11,'MONTHOUT','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (333,11,'RAH','java.lang.String',25,NULL);
INSERT INTO `request_file_field_description` VALUES (334,11,'RIZN','java.lang.Integer',6,NULL);
INSERT INTO `request_file_field_description` VALUES (335,11,'TARIF','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (336,12,'TAR_CODE','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (337,12,'TAR_CDPLG','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (338,12,'TAR_SERV','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (339,12,'TAR_DATEB','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (340,12,'TAR_DATEE','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (341,12,'TAR_COEF','java.math.BigDecimal',11,2);
INSERT INTO `request_file_field_description` VALUES (342,12,'TAR_COST','java.math.BigDecimal',14,7);
INSERT INTO `request_file_field_description` VALUES (343,12,'TAR_UNIT','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (344,12,'TAR_METER','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (345,12,'TAR_NMBAS','java.math.BigDecimal',11,2);
INSERT INTO `request_file_field_description` VALUES (346,12,'TAR_NMSUP','java.math.BigDecimal',11,2);
INSERT INTO `request_file_field_description` VALUES (347,12,'TAR_NMUBS','java.math.BigDecimal',11,4);
INSERT INTO `request_file_field_description` VALUES (348,12,'TAR_NMUSP','java.math.BigDecimal',11,4);
INSERT INTO `request_file_field_description` VALUES (349,12,'TAR_NMUMX','java.math.BigDecimal',11,4);
INSERT INTO `request_file_field_description` VALUES (350,12,'TAR_TPNMB','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (351,12,'TAR_TPNMS','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (352,12,'TAR_NMUPL','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (353,12,'TAR_PRIV','java.lang.Integer',10,NULL);
INSERT INTO `request_file_field_description` VALUES (354,13,'CDPR','java.lang.Integer',12,NULL);
INSERT INTO `request_file_field_description` VALUES (355,13,'IDCODE','java.lang.String',10,NULL);
INSERT INTO `request_file_field_description` VALUES (356,13,'FIO','java.lang.String',50,NULL);
INSERT INTO `request_file_field_description` VALUES (357,13,'PPOS','java.lang.String',15,NULL);
INSERT INTO `request_file_field_description` VALUES (358,13,'RS','java.lang.String',25,NULL);
INSERT INTO `request_file_field_description` VALUES (359,13,'YEARIN','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (360,13,'MONTHIN','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (361,13,'LGCODE','java.lang.Integer',4,NULL);
INSERT INTO `request_file_field_description` VALUES (362,13,'DATA1','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (363,13,'DATA2','java.util.Date',8,NULL);
INSERT INTO `request_file_field_description` VALUES (364,13,'LGKOL','java.lang.Integer',2,NULL);
INSERT INTO `request_file_field_description` VALUES (365,13,'LGKAT','java.lang.String',3,NULL);
INSERT INTO `request_file_field_description` VALUES (366,13,'LGPRC','java.lang.Integer',3,NULL);
INSERT INTO `request_file_field_description` VALUES (367,13,'SUMM','java.math.BigDecimal',8,2);
INSERT INTO `request_file_field_description` VALUES (368,13,'FACT','java.math.BigDecimal',19,6);
INSERT INTO `request_file_field_description` VALUES (369,13,'TARIF','java.math.BigDecimal',14,7);
INSERT INTO `request_file_field_description` VALUES (370,13,'FLAG','java.lang.Integer',1,NULL);

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
    ('107374638','Матвеев Матвей Матвеевич', 'Матвей', 'Матвеевич', 'Матвеев', '2012-01-02', 'Харьков', '20', '25А','','19', 7);
--    ('107374638','Матвеев2 Матвей2 Матвеевич2', 'Матвей2', 'Матвеевич2', 'Матвеев2', '2012-01-02', 'Харьков', '124', '25А','','19', 7);

-- Facility service types
insert into `facility_service_type`(`IDCODE`, `FIO`, `first_name`, `middle_name`, `last_name`, `date`, `city`, `CDUL`, `HOUSE`, `BUILD`, `APT`, `request_file_id`)
values
    ('107374638','Матвеев Матвей Матвеевич', 'Матвей', 'Матвеевич', 'Матвеев', '2012-01-02', 'Харьков', '123', '25А','','19', 8);

-- INSERT INTO `person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street`, `street_type`, `building_num`, `building_corp`, `apartment`, `account_number`, `pu_account_number`, `oszn_id`, `calc_center_id`, `user_organization_id`) 
-- VALUES(UPPER('Матвей'),UPPER('Матвеевич'),UPPER('Матвеев'),UPPER('Харьков'),UPPER('ФРАНТИШЕКА КРАЛА'),UPPER('УЛ'),UPPER('25-/А'),'',UPPER('19'),'1000001109','107374638',1,2,4);

-- Address corrections

insert into city_correction(id, organization_id, correction, object_id, module_id)
  values (1,2,UPPER('Новосибирск Соответствие'),1,0);
insert into street_correction(id, city_object_id, street_type_object_id, organization_id, correction, object_id, module_id)
  values (1,1, 10012, 2,UPPER('Терешковой Соответствие'),1,0);
insert into building_correction(street_object_id, organization_id, correction, correction_corp, object_id, module_id)
  values (2, 1, UPPER('10'),UPPER('1'),3,0);

insert into city_correction(id, organization_id, correction, object_id, module_id)
  values (2,2,UPPER('Харьков Соответствие'),3,0);
insert into street_correction(id, city_object_id, street_type_object_id, organization_id, correction, object_id, module_id)
  values (2,3, 10012, 2,UPPER('Косиора Соответствие'),4,0);
insert into building_correction(street_object_id, organization_id, correction, correction_corp, object_id, module_id)
  values (2, 4, UPPER('154А'),UPPER(''),6,0);

insert into street_correction(id, city_object_id, street_type_object_id, organization_id, correction, object_id, module_id)
    values (3, 3, 10012, 2,UPPER('ФРАНТИШЕКА КРАЛА Соответствие'),5,0);
insert into building_correction(street_object_id, organization_id, correction, correction_corp, object_id, module_id)
  values (5, 2,UPPER('25А'),UPPER(''),7,0);

insert into street_correction(id, city_object_id, street_type_object_id, organization_id, correction, object_id, module_id)
    values (4,3, 10012, 2,UPPER('ФРАНТИШЕКА КРАЛА Соответствие'),5,0);
insert into building_correction(street_object_id, organization_id, correction, correction_corp, object_id, module_id)
  values (5, 2, UPPER('23'),UPPER(''),8,0);

insert into district_correction(city_object_id, organization_id, correction, object_id, module_id) values (2, 2,UPPER('Центральный'),3,0);


-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, external_id, module_id, user_organization_id) values
(1,UPPER('мiсцевих Рад'),1,UPPER('1'),0,3), (2,UPPER('ГОС'),1,UPPER('1'),0,null),
(1,UPPER('кооперативна'),2,UPPER('1'),0,3), (2,UPPER('КООП'),2,UPPER('2'),0,null),
(1,UPPER('приватна'),5,UPPER('5'),0,3), (2,UPPER('ВЫК'),5,UPPER('5'),0,null),
(1,UPPER('приватизована'),6,UPPER('6'),0,3), (2,UPPER('ЧАС'),6,UPPER('6'),0,null);

-- Privileges corrections
insert into privilege_correction(organization_id, correction, object_id, external_id, module_id, user_organization_id) values
(2,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ'),15,UPPER('34'),0,null),
(1,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ'),15,UPPER('100'),0,3);

-- Tarif
INSERT INTO `subsidy_tarif`(`T11_CS_UNI`, `T11_CODE2`, `request_file_id`, `T11_CODE1`) values ('0','123',3,'1');

-- config test
-- INSERT INTO `config`(`name`, `value`) VALUES ('FACILITY_STREET_TYPE_REFERENCE_FILENAME_MASK', 'KLKATUL\.DBF');
-- INSERT INTO `config`(`name`, `value`) VALUES ('FACILITY_STREET_REFERENCE_FILENAME_MASK', 'KLUL\.DBF');
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
