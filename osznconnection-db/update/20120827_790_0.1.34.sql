-- Adds loading, binding and saving request files of new types: dwelling characteristics, facility service type.
-- Adds saving request files of new type - facility form2.
-- Adds loading of facility references - street types, streets and tarifs.

SET GLOBAL log_bin_trust_function_creators = 1;

-- Modify unique key of `request_file` table in order to reflect the situations of loading file
-- with the same name, oszn, registry, month and year parameters but different user organizations.
ALTER TABLE `request_file` DROP KEY `unique_id`, 
	ADD UNIQUE KEY `request_file_unique_id` (`name`, `organization_id`, `user_organization_id`, `registry`, `month`, `year`);

-- ------------------------------
-- Dwelling characteristic
-- ------------------------------

DROP TABLE IF EXISTS `dwelling_characteristics`;

CREATE TABLE `dwelling_characteristics` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта характеристик жилья',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запросов',
    `account_number` VARCHAR(100) COMMENT 'Номер счета',

    `internal_city_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `internal_street_id` BIGINT(20) COMMENT 'Идентификатор улицы',
    `internal_street_type_id` BIGINT(20) COMMENT 'Идентификатор типа улицы',
    `internal_building_id` BIGINT(20) COMMENT 'Идентификатор дома',

    `outgoing_city` VARCHAR(100) COMMENT 'Название населенного пункта используемое центром начисления',
    `outgoing_district` VARCHAR(100) COMMENT 'Название района используемое центром начисления',
    `outgoing_street` VARCHAR(100) COMMENT 'Название улицы используемое центром начисления',
    `outgoing_street_type` VARCHAR(100) COMMENT 'Название типа улицы используемое центром начисления',
    `outgoing_building_number` VARCHAR(100) COMMENT 'Номер дома используемый центром начисления',
    `outgoing_building_corp` VARCHAR(100) COMMENT 'Корпус используемый центром начисления',
    `outgoing_apartment` VARCHAR(100) COMMENT 'Номер квартиры. Не используется',

    `street_correction_id` BIGINT(20) COMMENT 'Идентификатор соответствия улицы',

    `date` DATE NOT NULL COMMENT 'Дата. Производное поле. Первое число месяца, который был указан при загрузке файла, содержащего данную запись.',

    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и класс RequestStatus',

    `COD` VARCHAR(100) COMMENT 'Код района',
    `CDPR` VARCHAR(100) COMMENT 'Код ЄДРПОУ (ОГРН) организации',
    `NCARD` VARCHAR(100) COMMENT 'Идентификатор льготника',
    `IDCODE` VARCHAR(100) COMMENT 'ИНН собственника жилья/льготника (ставят ИНН льготника)',
    `PASP` VARCHAR(100) COMMENT 'Серия и номер паспорта собственника жилья/льготника (ставят паспорт льготника)',
    `FIO` VARCHAR(100) COMMENT 'ФИО собственника жилья/льготника (ставят ФИО льготника)',
    `first_name` VARCHAR(100) COMMENT 'Имя',
    `last_name` VARCHAR(100) COMMENT 'Фамилия',
    `middle_name` VARCHAR(100) COMMENT 'Отчество',
    `IDPIL` VARCHAR(100) COMMENT 'ИНН льготника',
    `PASPPIL` VARCHAR(100) COMMENT 'Серия и номер паспорта льготника',
    `FIOPIL` VARCHAR(100) COMMENT 'ФИО льготника',
    `INDEX` VARCHAR(100) COMMENT 'Почтовый индекс',
    `city` VARCHAR(100) NOT NULL COMMENT 'Населенный пункт. Исскуственное поле(значение берется из конфигураций)',
    `CDUL` VARCHAR(100) COMMENT 'Код улицы',
    `street` VARCHAR(100) COMMENT 'Улица. Производное поле.',
    `street_type` VARCHAR(100) COMMENT 'Тип улицы. Производное поле.',
    `HOUSE` VARCHAR(100) COMMENT 'Номер дома',
    `BLILD` VARCHAR(100) COMMENT 'Корпус',
    `APT` VARCHAR(100) COMMENT 'Номер квартиры',
    `VL` VARCHAR(100) COMMENT 'Тип собственности',
    `PLZAG` VARCHAR(100) COMMENT 'Общая площадь помещения',
    `PLOPAL` VARCHAR(100) COMMENT 'Отапливаемая площадь помещения',

    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_street_type_id` (`internal_street_type_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_FIO` (`FIO`),
    KEY `key_CDUL` (`CDUL`),
    KEY `key_HOUSE` (`HOUSE`),
    KEY `key_BLILD` (`BLILD`),
    KEY `key_APT` (`APT`),
    CONSTRAINT `fk_dwelling_characteristics__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_dwelling_characteristics__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы-запросы характеристик жилья';

-- Load dwelling characteristics directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (922, 1, UPPER('Директория входящих файлов характеристик жилья')), (922, 2, UPPER('Директория входящих файлов характеристик жилья'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (921, 900, 0, 922, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (921, 921, UPPER('string'));

-- Save dwelling characteristics directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (923, 1, UPPER('Директория исходящих файлов характеристик жилья')), (923, 2, UPPER('Директория исходящих файлов характеристик жилья'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (922, 900, 0, 923, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (922, 922, UPPER('string'));

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('DWELLING_CHARACTERISTICS','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'COD','java.lang.Integer',4,NULL),(@request_file_description_id,'CDPR','java.lang.Integer',12,NULL),
(@request_file_description_id,'NCARD','java.lang.Integer',7,NULL),(@request_file_description_id,'IDCODE','java.lang.String',10,NULL),
(@request_file_description_id,'PASP','java.lang.String',14,NULL),(@request_file_description_id,'FIO','java.lang.String',50,NULL),
(@request_file_description_id,'IDPIL','java.lang.String',10,NULL),(@request_file_description_id,'PASPPIL','java.lang.String',14,NULL),
(@request_file_description_id,'FIOPIL','java.lang.String',50,NULL),(@request_file_description_id,'INDEX','java.lang.Integer',6,NULL),
(@request_file_description_id,'CDUL','java.lang.Integer',5,NULL),(@request_file_description_id,'HOUSE','java.lang.String',7,NULL),
(@request_file_description_id,'BLILD','java.lang.String',2,NULL),(@request_file_description_id,'APT','java.lang.String',4,NULL),
(@request_file_description_id,'VL','java.lang.Integer',3,NULL),(@request_file_description_id,'PLZAG','java.math.BigDecimal',6,2),
(@request_file_description_id,'PLOPAL','java.math.BigDecimal',6,2);
	
-- ------------------------------
-- Facility street type reference
-- ------------------------------

DROP TABLE IF EXISTS `facility_street_type_reference`;

CREATE TABLE `facility_street_type_reference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта тип улицы',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла типов улиц',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `KLKUL_CODE` VARCHAR(100) COMMENT 'Код типа улицы',
    `KLKUL_NAME` VARCHAR(100) COMMENT 'Наименование типа улицы',

    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы-справочники типов улиц';

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('FACILITY_STREET_TYPE','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'KLKUL_CODE','java.lang.Integer',3,NULL),
(@request_file_description_id,'KLKUL_NAME','java.lang.String',7,NULL);

-- ------------------------------
-- Facility street reference
-- ------------------------------

DROP TABLE IF EXISTS `facility_street_reference`;

CREATE TABLE `facility_street_reference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта улица',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла улиц',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `KL_CODERN` VARCHAR(100) COMMENT 'Код района',
    `KL_CODEUL` VARCHAR(100) COMMENT 'Код улицы',
    `KL_NAME` VARCHAR(100) COMMENT 'Наименование улицы',
    `KL_CODEKUL` VARCHAR(100) COMMENT 'Код типа улицы',

    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы-справочники улиц';

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('FACILITY_STREET','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'KL_CODERN','java.lang.Integer',5,NULL),(@request_file_description_id,'KL_CODEUL','java.lang.Integer',5,NULL),
(@request_file_description_id,'KL_NAME','java.lang.String',50,NULL),(@request_file_description_id,'KL_CODEKUL','java.lang.Integer',3,NULL);

-- ------------------------------
-- Facility Service Type
-- ------------------------------

DROP TABLE IF EXISTS `facility_service_type`;

CREATE TABLE `facility_service_type` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта вид услуги',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запросов',
    `account_number` VARCHAR(100) COMMENT 'Номер счета',

    `internal_city_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `internal_street_id` BIGINT(20) COMMENT 'Идентификатор улицы',
    `internal_street_type_id` BIGINT(20) COMMENT 'Идентификатор типа улицы',
    `internal_building_id` BIGINT(20) COMMENT 'Идентификатор дома',

    `outgoing_city` VARCHAR(100) COMMENT 'Название населенного пункта используемое центром начисления',
    `outgoing_district` VARCHAR(100) COMMENT 'Название района используемое центром начисления',
    `outgoing_street` VARCHAR(100) COMMENT 'Название улицы используемое центром начисления',
    `outgoing_street_type` VARCHAR(100) COMMENT 'Название типа улицы используемое центром начисления',
    `outgoing_building_number` VARCHAR(100) COMMENT 'Номер дома используемый центром начисления',
    `outgoing_building_corp` VARCHAR(100) COMMENT 'Корпус используемый центром начисления',
    `outgoing_apartment` VARCHAR(100) COMMENT 'Номер квартиры. Не используется',

    `street_correction_id` BIGINT(20) COMMENT 'Идентификатор соответствия улицы',

    `date` DATE NOT NULL COMMENT 'Дата. Производное поле. Первое число месяца, который был указан при загрузке файла, содержащего данную запись.',

    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и класс RequestStatus',

    `COD` VARCHAR(100) COMMENT 'Код района',
    `CDPR` VARCHAR(100) COMMENT 'Код ЄДРПОУ (ОГРН) организации',
    `NCARD` VARCHAR(100) COMMENT 'Идентификатор льготника',
    `IDCODE` VARCHAR(100) COMMENT 'ИНН собственника жилья/льготника (ставят ИНН льготника)',
    `PASP` VARCHAR(100) COMMENT 'Серия и номер паспорта собственника жилья/льготника (ставят паспорт льготника)',
    `FIO` VARCHAR(100) COMMENT 'ФИО собственника жилья/льготника (ставят ФИО льготника)',
    `first_name` VARCHAR(100) COMMENT 'Имя',
    `last_name` VARCHAR(100) COMMENT 'Фамилия',
    `middle_name` VARCHAR(100) COMMENT 'Отчество',
    `IDPIL` VARCHAR(100) COMMENT 'ИНН льготника',
    `PASPPIL` VARCHAR(100) COMMENT 'Серия и номер паспорта льготника',
    `FIOPIL` VARCHAR(100) COMMENT 'ФИО льготника',
    `INDEX` VARCHAR(100) COMMENT 'Почтовый индекс',
    `city` VARCHAR(100) NOT NULL COMMENT 'Населенный пункт. Исскуственное поле(значение берется из конфигураций)',
    `CDUL` VARCHAR(100) COMMENT 'Код улицы',
    `street` VARCHAR(100) COMMENT 'Улица. Производное поле.',
    `street_type` VARCHAR(100) COMMENT 'Тип улицы. Производное поле.',
    `HOUSE` VARCHAR(100) COMMENT 'Номер дома',
    `BLILD` VARCHAR(100) COMMENT 'Корпус',
    `APT` VARCHAR(100) COMMENT 'Номер квартиры',
    `KAT` VARCHAR(100) COMMENT 'Категория льготы ЕДАРП',
    `LGCODE` VARCHAR(100) COMMENT 'Код возмещения',
    `YEARIN` VARCHAR(100) COMMENT 'Год начала действия льготы',
    `MONTHIN` VARCHAR(100) COMMENT 'Месяц начала действия льготы',
    `YEAROUT` VARCHAR(100) COMMENT 'Год окончания действия льготы',
    `MONTHOUT` VARCHAR(100) COMMENT 'Месяц окончания действия льготы',
    `RAH` VARCHAR(100) COMMENT 'Номер л/с ПУ',
    `RIZN` VARCHAR(100) COMMENT 'Тип услуги',
    `TARIF` VARCHAR(100) COMMENT 'Код тарифа услуги',

    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_street_type_id` (`internal_street_type_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_FIO` (`FIO`),
    KEY `key_CDUL` (`CDUL`),
    KEY `key_HOUSE` (`HOUSE`),
    KEY `key_BLILD` (`BLILD`),
    KEY `key_APT` (`APT`),
    CONSTRAINT `fk_facility_service_type__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_facility_service_type__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_facility_service_type__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_facility_service_type__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_facility_service_type__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы-запросы видов услуг';

-- Load facility service type directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (924, 1, UPPER('Директория входящих файлов-запросов видов услуг')), (924, 2, UPPER('Директория входящих файлов-запросов видов услуг'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (923, 900, 0, 924, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (923, 923, UPPER('string'));

-- Save facility service type directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (925, 1, UPPER('Директория исходящих файлов-запросов видов услуг')), (925, 2, UPPER('Директория исходящих файлов-запросов видов услуг'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (924, 900, 0, 925, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (924, 924, UPPER('string'));

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('FACILITY_SERVICE_TYPE','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'COD','java.lang.Integer',4,NULL),(@request_file_description_id,'CDPR','java.lang.Integer',12,NULL),
(@request_file_description_id,'NCARD','java.lang.Integer',7,NULL),(@request_file_description_id,'IDCODE','java.lang.String',10,NULL),
(@request_file_description_id,'PASP','java.lang.String',14,NULL),(@request_file_description_id,'FIO','java.lang.String',50,NULL),
(@request_file_description_id,'IDPIL','java.lang.String',10,NULL),(@request_file_description_id,'PASPPIL','java.lang.String',14,NULL),
(@request_file_description_id,'FIOPIL','java.lang.String',50,NULL),(@request_file_description_id,'INDEX','java.lang.Integer',6,NULL),
(@request_file_description_id,'CDUL','java.lang.Integer',5,NULL),(@request_file_description_id,'HOUSE','java.lang.String',7,NULL),
(@request_file_description_id,'BLILD','java.lang.String',2,NULL),(@request_file_description_id,'APT','java.lang.String',4,NULL),
(@request_file_description_id,'KAT','java.lang.Integer',4,NULL),(@request_file_description_id,'LGCODE','java.lang.Integer',4,NULL),
(@request_file_description_id,'YEARIN','java.lang.Integer',4,NULL),(@request_file_description_id,'MONTHIN','java.lang.Integer',2,NULL),
(@request_file_description_id,'YEAROUT','java.lang.Integer',4,NULL),(@request_file_description_id,'MONTHOUT','java.lang.Integer',2,NULL),
(@request_file_description_id,'RAH','java.lang.String',25,NULL),(@request_file_description_id,'RIZN','java.lang.Integer',6,NULL),
(@request_file_description_id,'TARIF','java.lang.Integer',10,NULL);

-- ------------------------------
-- Facility tarif reference
-- ------------------------------

DROP TABLE IF EXISTS `facility_tarif_reference`;

CREATE TABLE `facility_tarif_reference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта тариф',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла тарифов',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `TAR_CODE` VARCHAR(100) COMMENT 'Код тарифа',
    `TAR_CDPLG` VARCHAR(100) COMMENT 'Код услуги',
    `TAR_SERV` VARCHAR(100) COMMENT 'Номер тарифа в услуге',
    `TAR_DATEB` VARCHAR(100) COMMENT 'Дата начала действия тарифа',
    `TAR_DATEE` VARCHAR(100) COMMENT 'Дата окончания действия тарифа',
    `TAR_COEF` VARCHAR(100) COMMENT '',
    `TAR_COST` VARCHAR(100) COMMENT 'Ставка тарифа',
    `TAR_UNIT` VARCHAR(100) COMMENT '',
    `TAR_METER` VARCHAR(100) COMMENT '',
    `TAR_NMBAS` VARCHAR(100) COMMENT '',
    `TAR_NMSUP` VARCHAR(100) COMMENT '',
    `TAR_NMUBS` VARCHAR(100) COMMENT '',
    `TAR_NMUSP` VARCHAR(100) COMMENT '',
    `TAR_NMUMX` VARCHAR(100) COMMENT '',
    `TAR_TPNMB` VARCHAR(100) COMMENT '',
    `TAR_TPNMS` VARCHAR(100) COMMENT '',
    `TAR_NMUPL` VARCHAR(100) COMMENT '',
    `TAR_PRIV` VARCHAR(100) COMMENT '',
 
    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы-справочники тарифов для запросов по льготам';

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('FACILITY_TARIF','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'TAR_CODE','java.lang.Integer',10,NULL),(@request_file_description_id,'TAR_CDPLG','java.lang.Integer',10,NULL),
(@request_file_description_id,'TAR_SERV','java.lang.Integer',10,NULL),(@request_file_description_id,'TAR_DATEB','java.util.Date',8,NULL),
(@request_file_description_id,'TAR_DATEE','java.util.Date',8,NULL),(@request_file_description_id,'TAR_COEF','java.math.BigDecimal',11,2),
(@request_file_description_id,'TAR_COST','java.math.BigDecimal',14,7),(@request_file_description_id,'TAR_UNIT','java.lang.Integer',10,NULL),
(@request_file_description_id,'TAR_METER','java.lang.Integer',3,NULL),(@request_file_description_id,'TAR_NMBAS','java.math.BigDecimal',11,2),
(@request_file_description_id,'TAR_NMSUP','java.math.BigDecimal',11,2),(@request_file_description_id,'TAR_NMUBS','java.math.BigDecimal',11,4),
(@request_file_description_id,'TAR_NMUSP','java.math.BigDecimal',11,4),(@request_file_description_id,'TAR_NMUMX','java.math.BigDecimal',11,4),
(@request_file_description_id,'TAR_TPNMB','java.lang.Integer',10,NULL),(@request_file_description_id,'TAR_TPNMS','java.lang.Integer',10,NULL),
(@request_file_description_id,'TAR_NMUPL','java.lang.Integer',3,NULL),(@request_file_description_id,'TAR_PRIV','java.lang.Integer',10,NULL);

-- ------------------------------
-- Facility Form-2
-- ------------------------------

DROP TABLE IF EXISTS `facility_form2`;

CREATE TABLE `facility_form2` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта форма-2 льгота',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запросов',

    `account_number` VARCHAR(100) COMMENT 'Номер счета',
    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и класс RequestStatus',

    `CDPR` VARCHAR(100) COMMENT 'Код ЄДРПОУ (ОГРН) организации',
    `IDCODE` VARCHAR(100) COMMENT 'ИНН льготника',
    `FIO` VARCHAR(100) COMMENT 'ФИО льготника',
    `first_name` VARCHAR(100) COMMENT 'Имя',
    `last_name` VARCHAR(100) COMMENT 'Фамилия',
    `middle_name` VARCHAR(100) COMMENT 'Отчество',
    `PPOS` VARCHAR(100) COMMENT '',
    `RS` VARCHAR(100) COMMENT 'Номер л/с ПУ',
    `YEARIN` VARCHAR(100) COMMENT 'Год выгрузки данных',
    `MONTHIN` VARCHAR(100) COMMENT 'Месяц выгрузки данных',
    `LGCODE` VARCHAR(100) COMMENT 'Код льготы',
    `DATA1` VARCHAR(100) COMMENT 'Дата начала периода',
    `DATA2` VARCHAR(100) COMMENT 'Дата окончания периода',
    `LGKOL` VARCHAR(100) COMMENT 'Кол-во пользующихся льготой',
    `LGKAT` VARCHAR(100) COMMENT 'Категория льготы ЕДАРП',
    `LGPRC` VARCHAR(100) COMMENT 'Процент льготы',
    `SUMM` VARCHAR(100) COMMENT 'Сумма возмещения',
    `FACT` VARCHAR(100) COMMENT 'Объем фактического потребления (для услуг со счетчиком)',
    `TARIF` VARCHAR(100) COMMENT 'Ставка тарифа',
    `FLAG` VARCHAR(100) COMMENT '',

    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    CONSTRAINT `fk_facility_form2__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы форма-2 льгота';

-- Save facility form2 directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (929, 1, UPPER('Директория исходящих файлов форма-2 льгота')), (929, 2, UPPER('Директория исходящих файлов форма-2 льгота'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (928, 900, 0, 929, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (928, 928, UPPER('string'));

-- File description
INSERT INTO `request_file_description`(`request_file_type`,`date_pattern`) VALUES ('FACILITY_FORM2','dd.MM.yyyy');
SET @request_file_description_id = LAST_INSERT_ID();
INSERT INTO `request_file_field_description`(`request_file_description_id`,`name`,`type`,`length`,`scale`) VALUES
(@request_file_description_id,'CDPR','java.lang.Integer',12,NULL),(@request_file_description_id,'IDCODE','java.lang.String',10,NULL),
(@request_file_description_id,'FIO','java.lang.String',50,NULL),(@request_file_description_id,'PPOS','java.lang.String',15,NULL),
(@request_file_description_id,'RS','java.lang.String',25,NULL),(@request_file_description_id,'YEARIN','java.lang.Integer',4,NULL),
(@request_file_description_id,'MONTHIN','java.lang.Integer',2,NULL),(@request_file_description_id,'LGCODE','java.lang.Integer',4,NULL),
(@request_file_description_id,'DATA1','java.util.Date',8,NULL),(@request_file_description_id,'DATA2','java.util.Date',8,NULL),
(@request_file_description_id,'LGKOL','java.lang.Integer',2,NULL),(@request_file_description_id,'LGKAT','java.lang.String',3,NULL),
(@request_file_description_id,'LGPRC','java.lang.Integer',3,NULL),(@request_file_description_id,'SUMM','java.math.BigDecimal',8,2),
(@request_file_description_id,'FACT','java.math.BigDecimal',19,6),(@request_file_description_id,'TARIF','java.math.BigDecimal',14,7),
(@request_file_description_id,'FLAG','java.lang.Integer',1,NULL);


INSERT INTO `update` (`version`) VALUE ('20120827_790_0.1.34');

SET GLOBAL log_bin_trust_function_creators = 0;