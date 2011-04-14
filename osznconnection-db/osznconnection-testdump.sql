-- Organizations
insert into organization(object_id, entity_type_id) values (1,900), (2,901);
insert into organization_string_culture(id, locale_id, value) values (3, 1, UPPER('ОСЗН 1')), (3,2,UPPER('ОСЗН 1')), (4, 1, UPPER('1234')),
(5, 1, UPPER('Модуль начислений №1')), (5, 2, UPPER('Модуль начислений №1')), (6, 1, UPPER('123')), (7, 1, UPPER('true'));
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,900,3,900), (1,1,901,4,901), (1,1,902,3,902),
(1,2,900,5,900), (1,2,901,6,901), (1,2,904,7,904);

-- User organizations
insert into organization(object_id, entity_type_id) values (3,902), (4,902);
insert into organization_string_culture(id, locale_id, value) values (8, 1, UPPER('КП "ЖИЛКОМСЕРВИС"')), (9,1,UPPER('ЛЕНИНСКИЙ ФИЛИАЛ КП "ЖИЛКОМСЕРВИС"')),
(10, 1, UPPER('12345')), (11, 1, UPPER('123456'));
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,3,900,8,900), (1,3,901,10,901),
(1,4,900,9,900), (1,4,901,11,901), (1,4,903,3,903);

-- Files
insert into request_file_group(id) values (1);
--insert into request_file_group(id) values (2);
insert into `request_file`(id, group_id, organization_id, `name`, `directory`, `registry`, `month`, `year`, `loaded`, `type`) values
(1,1,1,'A_123405.dbf', 'AB', 1, 10, 2010, CURRENT_TIMESTAMP, 'PAYMENT'),
(2,1,1,'AF123405.dbf', 'AB', 1, 10, 2010, CURRENT_TIMESTAMP, 'BENEFIT'),
(3,null,1,'TARIF12.dbf', 'AB', 1, 10, 2010, CURRENT_TIMESTAMP, 'TARIF'),
(4,null,1,'B1170710.dbf', 'AB', 1, 10, 2010, '2011-01-12', 'ACTUAL_PAYMENT');

-- (4,2,1,'A_123406.dbf', 'AB', 1, 10, 2010, CURRENT_TIMESTAMP, 'PAYMENT'),
-- (5,2,1,'AF123406.dbf', 'CE', 1, 10, 2010, CURRENT_TIMESTAMP, 'BENEFIT');

-- Benefit
insert into benefit(OWN_NUM, OWN_NUM_SR, OZN, F_NAM, M_NAM, SUR_NAM, request_file_id, IND_COD, PSP_NUM)
values
-- (1, 1, 1, 'Иван', 'Иванович', 'Иванов',2),
-- (2, 1, 0, 'Иван2', 'Иванович2', 'Иванов2',2, '0000000', null),
--(3, 3, 0, 'Петр0','Петрович0','Петров0',2, '11111111', null),
 (4, 4, 1, 'Петр','Петрович','Петров',2, '2142426432', null);
-- (5, 4, 0, 'Петр0','Петрович0','Петров0',5, '11111111', null);

-- Payments
insert into payment(OWN_NUM, OWN_NUM_SR, F_NAM, M_NAM, SUR_NAM, N_NAME, VUL_NAME, BLD_NUM, CORP_NUM, FLAT, DAT1, request_file_id)
values
-- (6, 1,'Иван', 'Иванович', 'Иванов', 'Новосибирск', 'Терешковой1', '18','1', '10', '2010-09-08',1),
-- (7, 2,'Сидор', 'Сидорович', 'Сидоров', 'Новосибирск', 'ул. Терешковой', 'д. 11','', 'кв. 11', '2010-09-08',1),
-- (8, 3,'Петр', 'Петрович', 'Петров', 'Харьков', 'Косиора', '154A','', '1', '2010-09-08',1),
-- (9, 3,'Петр1', 'Петрович1', 'Петров1', 'Харьков', 'Kоcиорa', '154A','', '1', '2010-09-08',1),
-- (10, 3,'Матвей1', 'Матвеевич1', 'Матвеев1', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '40', '2010-09-09',1),
--(11, 4,'Матвей2', 'Матвеевич2', 'Матвеев2', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '19', '2010-09-09',1);
(12, 4,'Матвей', 'Матвеевич', 'Матвеев', 'Харьков', 'ФРАНТИШЕКА КРАЛА1', '  2 7А','  ', '19', '2010-09-09',1);
-- (13, 5,'Матвей22', 'Матвеевич22', 'Матвеев22', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '  2 6А','  ', '19', '2010-09-09',1);
-- (14, 4,'Матвей', 'Матвеевич', 'Матвеев', 'Новосибирск', 'Терешковой', '25','  ', '19', '2010-09-09',1);

-- Actual payments
insert into actual_payment(OWN_NUM, F_NAM, M_NAM, SUR_NAM, N_NAME, VUL_CAT, VUL_NAME, VUL_CODE, BLD_NUM, CORP_NUM, FLAT, DAT_BEG, request_file_id)
values
--    (123,'Матвей1', 'Матвеевич1', 'Матвеев1', 'Харьков', 'УЛ1', 'ФРАНТИШЕКА КРАЛА', 123, '  2 5A',' ', '40', '2010-09-09', 4),
    (123,'Матвей2', 'Матвеевич2', 'Матвеев2', 'Харьkов', 'УЛ', 'ФРАНТИШЕКА КРАЛА', 123, '  2 5A',' ', '1 9', '2009-01-01', 4);


--insert into `person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street`, `building_num`, `building_corp`, `apartment`, `account_number`, `own_num_sr`, `oszn_id`, `calc_center_id`) values('Матвей','Матвеевич','Матвеев','Харьков','ФРАНТИШЕКА КРАЛА','  2 5А','  ','19','1000001108','4','1','2');

-- Address corrections
INSERT INTO `street_type_correction`(`id`, `object_id`, `correction`, `organization_id`, `organization_code`, `internal_organization_id`) VALUES
(1,10000,'Б-Р',2,'1',0), (2,10001,'М',2,'1',0), (3,10002,'М-Н',2,'1',0), (4,10003,'ПЕР',2,'1',0), (5,10004,'ПЛ',2,'1',0), (6,10005,'П',2,'1',0),
(7,10006,'ПОС',2,'1',0), (8,10007,'ПР-Д',2,'1',0), (9,10008,'ПРОСП',2,'1',0), (10,10009,'СП',2,'1',0), (11,10010,'Т',2,'1',0), (12,10011,'ТУП',2,'1',0),
(13,10012,'УЛ',2,'1',0), (14,10013,'ШОССЕ',2,'1',0), (15,10014,'НАБ',2,'1',0), (16,10015,'В-Д',2,'1',0), (17,10016,'СТ',2,'1',0);

insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (1,2,UPPER('Новосибирск'),1,0);
insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id, street_type_correction_id) values (1,2,UPPER('Терешковой'),1,0,1,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'10','1',3,0,1);

insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (2,2,UPPER('Харьков'),3,0);
insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id, street_type_correction_id) values (2,2,UPPER('Косиора'),4,0,2,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'154А','',6,0,2);

insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id, street_type_correction_id) values (3,2,UPPER('ФРАНТИШЕКА КРАЛА'),5,0,2,13);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'25А','',7,0,3);

insert into district_correction(organization_id, correction, object_id, internal_organization_id, parent_id) values (2,UPPER('Центральный'),3,0,2);

--insert into district_correction(organization_id, correction, object_id, internal_organization_id, parent_id) values (2,UPPER('Центральный123'),3,0,2);

-- insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (3,1,UPPER('Харьков'),3,0);
-- insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (4,1,UPPER('ФРАНТИШЕКА КРАЛА1'),5,0,3);

/* Corrections for testing situations where correction's building belongs another street than local address base' building */

--insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (3,1,UPPER('Новосибирск'),1,0);
--insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (4,1,UPPER('Терешковой'),1,0,3);
--insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (1,'25','',7,0,4);


-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(1,UPPER('мiсцевих Рад'),1,'1',0), (2,UPPER('ГОС'),1,'1',0),
(1,UPPER('кооперативна'),2,'1',0), (2,UPPER('КООП'),2,'2',0),
(1,UPPER('приватна'),5,'5',0), (2,UPPER('ВЫК'),5,'5',0),
(1,UPPER('приватизована'),6,'6',0), (2,UPPER('ЧАС'),6,'6',0);

-- Privileges corrections
insert into privilege_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(2,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,'34',0),
(1,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,'1000',0);

-- Tarif
insert into tarif(`T11_CS_UNI`, `T11_CODE2`, `request_file_id`, `T11_CODE1`) values (0,123,3,1);

-- config test
--insert into config(`name`, `value`) values ('SAVE_OUTPUT_FILE_STORAGE_DIR', 'D:\\Artem\\Projects\\org.complitex\\storage\\out');
--insert into config(`name`, `value`) values ('ADDRESS_IMPORT_FILE_STORAGE_DIR', 'D:\\Artem\\Projects\\org.complitex\\storage\\import');
