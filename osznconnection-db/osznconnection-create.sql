/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ------------------------------
-- Forms of ownership
-- ------------------------------
DROP TABLE IF EXISTS `ownership`;

CREATE TABLE `ownership` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Не используется',
  `parent_entity_id` BIGINT(20) COMMENT 'Не используется',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия параметров объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата завершения периода действия параметров объекта',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус объекта: ACTIVE, INACTIVE или ARCHIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_ownership__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_ownership__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Форма собственности';

DROP TABLE IF EXISTS `ownership_attribute`;

CREATE TABLE `ownership_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута. Возможные значения: 1100 - НАЗВАНИЕ',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа. Возможные значение: 1100 - STRING_CULTURE',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия параметров атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия параметров атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус атрибута: ACTIVE, INACTIVE или ARCHIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_ownership_attribute__ownership` FOREIGN KEY (`object_id`) REFERENCES `ownership`(`object_id`),
  CONSTRAINT `fk_ownership_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_ownership_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Атрибуты объекта формы собственности';

DROP TABLE IF EXISTS `ownership_string_culture`;

CREATE TABLE `ownership_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор значения',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_ownership_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Локализированное значение атрибута формы собственности';

-- ------------------------------
-- Privileges
-- ------------------------------
DROP TABLE IF EXISTS `privilege`;

CREATE TABLE `privilege` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Не используется',
  `parent_entity_id` BIGINT(20) COMMENT 'Не используется',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия параметров объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия параметров объекта',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус объекта: ACTIVE, INACTIVE или ARCHIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_privilege__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_privilege__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Привилегия';

DROP TABLE IF EXISTS `privilege_attribute`;

CREATE TABLE `privilege_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута. Возможные значения: 1200 - НАЗВАНИЕ, 1201 - КОД',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа. Возможные значения: 1200 - STRING_CULTURE, 1201 - STRING',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия значений атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата завершения периода действия значений атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус атрибута: ACTIVE, INACTIVE или ARCHIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_privilege_attribute__privilege` FOREIGN KEY (`object_id`) REFERENCES `privilege`(`object_id`),
  CONSTRAINT `fk_privilege_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_privilege_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Атрибуты объекта привилегии';

DROP TABLE IF EXISTS `privilege_string_culture`;

CREATE TABLE `privilege_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор значения',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_privilege_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Локализированное значение атрибута привилегий';

-- ------------------------------
-- Request File Group
-- ------------------------------
DROP TABLE IF EXISTS `request_file_group`;

CREATE TABLE `request_file_group` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор группы фалов запросов',
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата создания',
    `status` INTEGER COMMENT 'Код статуса. См. таблицу status_description и класс RequestFileStatus',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Группа файлов запросов (начисления + льготы)';

-- ------------------------------
-- Request File
-- ------------------------------
DROP TABLE IF EXISTS `request_file`;

CREATE TABLE `request_file` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор файла запроса',
    `group_id` BIGINT(20) COMMENT 'Идентификатор группы',
    `loaded` DATETIME NOT NULL COMMENT 'Дата загрузки',
    `name` VARCHAR(20) NOT NULL COMMENT 'Имя файла',
    `directory` VARCHAR(255) COMMENT 'Директория файла',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `registry` INT(2) NOT NULL COMMENT 'Номер реестра',
    `month` INT(2) NOT NULL COMMENT 'Номер месяца',
    `year` INT(4) NOT NULL COMMENT 'Номер года',
    `dbf_record_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Количество записей в исходном файле',
    `length` BIGINT(20) COMMENT 'Размер файла. Не используется',
    `check_sum` VARCHAR(32) COMMENT 'Контрольная сумма. Не используется',
    `type` VARCHAR(50) COMMENT 'Тип файла. Возможные значения: BENEFIT, PAYMENT, TARIF, ACTUAL_PAYMENT',
    `status` INTEGER COMMENT 'См. таблицу status_description и класс RequestFileStatus',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_id` (`name`, `organization_id`, `registry`, `month`, `year`), 
    KEY `key_group_id` (`group_id`),
    KEY `key_loaded` (`loaded`),
    KEY `key_name` (`name`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_registry` (`registry`),
    KEY `key_month` (`month`),
    KEY `key_year` (`year`) ,
    KEY `key_type` (`type`) ,
    CONSTRAINT `fk_request_file__request_file_group` FOREIGN KEY (`group_id`) REFERENCES `request_file_group` (`id`),
    CONSTRAINT `fk_request_file__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Файл запросов';

-- ------------------------------
-- Payment
-- ------------------------------
DROP TABLE IF EXISTS `payment`;

CREATE TABLE `payment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор начисления',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файло запросов',
    `account_number` VARCHAR(100) COMMENT 'Номер счета',

    `internal_city_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `internal_street_id` BIGINT(20) COMMENT 'Идентификатор улицы',
    `internal_street_type_id` BIGINT(20) COMMENT 'Идентификатор типа улицы',
    `internal_building_id` BIGINT(20) COMMENT 'Идентификатор дома',
    `internal_apartment_id` BIGINT(20) COMMENT 'Идентификатор квартиры. Не используется',

    `outgoing_city` VARCHAR(100) COMMENT 'Название населенного пункта используемое центром начисления',
    `outgoing_district` VARCHAR(100) COMMENT 'Название района используемое центром начисления',
    `outgoing_street` VARCHAR(100) COMMENT 'Название улицы используемое центром начисления',
    `outgoing_street_type` VARCHAR(100) COMMENT 'Название типа улицы используемое центром начисления',
    `outgoing_building_number` VARCHAR(100) COMMENT 'Номер дома используемый центром начисления',
    `outgoing_building_corp` VARCHAR(100) COMMENT 'Корпус используемый центром начисления',
    `outgoing_apartment` VARCHAR(100) COMMENT 'Номер квартиры. Не используется',

    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `OWN_NUM` VARCHAR (15) COMMENT 'Номер дела',
    `REE_NUM` INT(2) COMMENT 'Номер реестра',
    `OPP` VARCHAR(8) COMMENT 'Признаки наличия услуг',
	`NUMB` INT(2) COMMENT 'Общее число зарегистрированных',
	`MARK` INT(2) COMMENT 'К-во людей, которые пользуются льготами',
	`CODE` INT(4) COMMENT 'Код ЖЭО',
	`ENT_COD` INT(10) COMMENT 'Код ЖЭО ОКПО',
	`FROG`  DECIMAL(5,1) COMMENT 'Процент льгот',
    `FL_PAY` DECIMAL(9,2) COMMENT 'Общая плата',
	`NM_PAY` DECIMAL(9,2) COMMENT 'Плата в пределах норм потребления',
	`DEBT` DECIMAL(9,2) COMMENT 'Сумма долга',
	`CODE2_1` INT(6) COMMENT 'Оплата жилья',
	`CODE2_2` INT(6) COMMENT 'система',
	`CODE2_3` INT(6) COMMENT 'Горячее водоснабжение',
	`CODE2_4` INT(6) COMMENT 'Холодное водоснабжение',
	`CODE2_5` INT(6) COMMENT 'Газоснабжение',
	`CODE2_6` INT(6) COMMENT 'Электроэнергия',
	`CODE2_7` INT(6) COMMENT 'Вывоз мусора',
	`CODE2_8` INT(6) COMMENT 'Водоотведение',
	`NORM_F_1` DECIMAL(10,4) COMMENT 'Общая площадь (оплата жилья)',
	`NORM_F_2` DECIMAL(10,4) COMMENT 'Объемы потребления (отопление)',
	`NORM_F_3` DECIMAL(10,4) COMMENT 'Объемы потребления (горячего водо.)',
	`NORM_F_4` DECIMAL(10,4) COMMENT 'Объемы потребления (холодное водо.)',
	`NORM_F_5` DECIMAL(10,4) COMMENT 'Объемы потребления (газоснабжение)',
	`NORM_F_6` DECIMAL(10,4) COMMENT 'Объемы потребления (электроэнергия)',
	`NORM_F_7` DECIMAL(10,4) COMMENT 'Объемы потребления (вывоз мусора)',
	`NORM_F_8` DECIMAL(10,4) COMMENT 'Объемы потребления (водоотведение)',
	`OWN_NUM_SR` VARCHAR(15) COMMENT 'Лицевой счет в обслуж. организации',
	`DAT1` DATE COMMENT 'Дата начала действия субсидии',
	`DAT2` DATE COMMENT 'Дата формирования запроса',
	`OZN_PRZ` INT(1) COMMENT 'Признак (0 - автоматическое назначение, 1-для ручного расчета)',
	`DAT_F_1` DATE COMMENT 'Дата начала для факта',
	`DAT_F_2` DATE COMMENT 'Дата конца для факта',
	`DAT_FOP_1` DATE COMMENT 'Дата начала для факта отопления',
	`DAT_FOP_2` DATE COMMENT 'Дата конца для факта отопления',
	`ID_RAJ` VARCHAR(5) COMMENT 'Код района',
	`SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
	`F_NAM` VARCHAR(15) COMMENT 'Имя',
	`M_NAM` VARCHAR(20) COMMENT 'Отчество',
	`IND_COD` VARCHAR(10) COMMENT 'Идентификационный номер',
	`INDX` VARCHAR(6) COMMENT 'Индекс почтового отделения',
	`N_NAME` VARCHAR(30) COMMENT 'Название населенного пункта',
	`VUL_NAME` VARCHAR(30) COMMENT 'Название улицы',
	`BLD_NUM` VARCHAR(7) COMMENT 'Номер дома',
	`CORP_NUM` VARCHAR(2) COMMENT 'Номер корпуса',
	`FLAT` VARCHAR(9) COMMENT 'Номер квартиры',
	`CODE3_1` INT(6) COMMENT 'Код тарифа оплаты жилья',
	`CODE3_2` INT(6) COMMENT 'Код тарифа отопления',
	`CODE3_3` INT(6) COMMENT 'Код тарифа горячего водоснабжения',
	`CODE3_4` INT(6) COMMENT 'Код тарифа холодного водоснабжения',
	`CODE3_5` INT(6) COMMENT 'Код тарифа - газоснабжение',
	`CODE3_6` INT(6) COMMENT 'Код тарифа-электроэнергии',
	`CODE3_7` INT(6) COMMENT 'Код тарифа - вывоз мусора',
	`CODE3_8` INT(6) COMMENT 'Код тарифа - водоотведение',
	`OPP_SERV` VARCHAR(8) COMMENT 'Резерв',
	`RESERV1` INT(10) COMMENT 'Резерв',
	`RESERV2` VARCHAR(10) COMMENT 'Резер',
    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_street_type_id` (`internal_street_type_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_internal_apartment_id` (`internal_apartment_id`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),
    KEY `key_N_NAME` (`N_NAME`),
    KEY `key_VUL_NAME` (`VUL_NAME`),
    KEY `key_BLD_NUM` (`BLD_NUM`),
    KEY `key_FLAT` (`FLAT`),
    KEY `key_OWN_NUM_SR` (`OWN_NUM_SR`),
    CONSTRAINT `fk_payment__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_payment__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_payment__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_payment__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_payment__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`),
    CONSTRAINT `fk_payment__apartment` FOREIGN KEY (`internal_apartment_id`) REFERENCES `apartment` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Начисления';

-- ------------------------------
-- Benefit
-- ------------------------------
DROP TABLE IF EXISTS `benefit`;

CREATE TABLE `benefit` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор льготы',
    `request_file_id` BIGINT(20) NULL COMMENT 'Идентификатор файла запросов',
    `account_number` VARCHAR(100) NULL COMMENT 'Номер счета',
    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

	`OWN_NUM` VARCHAR(15) COMMENT 'Номер дела',
	`REE_NUM` INT(2) COMMENT 'Номер реестра',
	`OWN_NUM_SR` VARCHAR(15) COMMENT 'Лицевой счет в обслуж. организации',
	`FAM_NUM` INT(2) COMMENT 'Номер члена семьи',
	`SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
	`F_NAM` VARCHAR(15) COMMENT 'Имя',
	`M_NAM` VARCHAR(20) COMMENT 'Отчество',
	`IND_COD` VARCHAR(10) COMMENT 'Идентификационный номер',
	`PSP_SER` VARCHAR(6) COMMENT 'Серия паспорта',
	`PSP_NUM` VARCHAR(6) COMMENT 'Номер паспорта',
	`OZN` INT(1) COMMENT 'Признак владельца',
	`CM_AREA` DECIMAL(10,2) COMMENT 'Общая площадь',
	`HEAT_AREA` DECIMAL(10,2) COMMENT 'Обогреваемая площадь',
	`OWN_FRM` INT(6) COMMENT 'Форма собственности',
	`HOSTEL` INT(2) COMMENT 'Количество комнат',
	`PRIV_CAT` INT(3) COMMENT 'Категория льготы на платежи',
	`ORD_FAM` INT(2) COMMENT 'Порядок семьи льготников для расчета платежей',
	`OZN_SQ_ADD` INT(1) COMMENT 'Признак учета дополнительной площади',
	`OZN_ABS` INT(1) COMMENT 'Признак отсутствия данных в базе ЖЭО',
	`RESERV1` DECIMAL(10,2) COMMENT 'Резерв',
	`RESERV2` VARCHAR(10) COMMENT 'Резерв',
	PRIMARY KEY (`id`),
	KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),   
    KEY `key_OWN_NUM_SR` (`OWN_NUM_SR`),
    CONSTRAINT `fk_benefit__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Льготы';

-- ------------------------------
-- Tarif
-- ------------------------------
DROP TABLE IF EXISTS `tarif`;

CREATE TABLE `tarif` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор тарифа',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла тарифов',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `T11_DATA_T` VARCHAR(10) COMMENT '',
    `T11_DATA_E` VARCHAR(10) COMMENT '',
    `T11_DATA_R` VARCHAR(10) COMMENT '',
    `T11_MARK` INT(3) COMMENT '',
    `T11_TARN` INT(6) COMMENT '',
    `T11_CODE1` INT(3) COMMENT '',
    `T11_CODE2` INT(6) COMMENT '',
    `T11_COD_NA` VARCHAR(40) COMMENT '',
    `T11_CODE3` INT(6) COMMENT '',
    `T11_NORM_U` DECIMAL(19, 10) COMMENT '',
    `T11_NOR_US` DECIMAL(19, 10) COMMENT '',
    `T11_CODE_N` INT(3) COMMENT '',
    `T11_COD_ND` INT(3) COMMENT '',
    `T11_CD_UNI` INT(3) COMMENT '',
    `T11_CS_UNI` DECIMAL (19, 10) COMMENT '',
    `T11_NORM` DECIMAL (19, 10) COMMENT '',
    `T11_NRM_DO` DECIMAL (19, 10) COMMENT '',
    `T11_NRM_MA` DECIMAL (19, 10) COMMENT '',
    `T11_K_NADL` DECIMAL (19, 10) COMMENT '',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Тарифы';


-- ------------------------------
-- Actual payment
-- ------------------------------

DROP TABLE IF EXISTS `actual_payment`;

CREATE TABLE `actual_payment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор фактического начисления',
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

    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и класс RequestStatus',

    `SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
    `F_NAM` VARCHAR(15) COMMENT 'Имя',
    `M_NAM` VARCHAR(20) COMMENT 'Отчество',
    `INDX` VARCHAR(6) COMMENT 'Индекс почтового отделения',
    `N_NAME` VARCHAR(30) COMMENT 'Название населенного пункта',
    `N_CODE` VARCHAR(5) COMMENT '',
    `VUL_CAT` VARCHAR(7) COMMENT 'Тип улицы',
    `VUL_NAME` VARCHAR(30) COMMENT 'Название улицы',
    `VUL_CODE` VARCHAR(5) COMMENT 'Код улицы',
    `BLD_NUM` VARCHAR(7) COMMENT 'Номер дома',
    `CORP_NUM` VARCHAR(2) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(9) COMMENT 'Номер квартиры',
    `OWN_NUM` VARCHAR (15) COMMENT 'Номер дела',
    `APP_NUM` VARCHAR(8) COMMENT '',
    `DAT_BEG` DATE COMMENT '',
    `DAT_END` DATE COMMENT '',
    `CM_AREA` DECIMAL(7,2) COMMENT '',
    `NM_AREA` DECIMAL(7,2) COMMENT '',
    `BLC_AREA` DECIMAL(5,2) COMMENT '',
    `FROG` DECIMAL(5,1) COMMENT '',
    `DEBT` DECIMAL(10,2) COMMENT '',
    `NUMB` INT(2) COMMENT '',
    `P1` DECIMAL(10,4) COMMENT 'фактическое начисление',
    `N1` DECIMAL(10,4) COMMENT 'фактический тариф',
    `P2` DECIMAL(10,4) COMMENT '',
    `N2` DECIMAL(10,4) COMMENT '',
    `P3` DECIMAL(10,4) COMMENT '',
    `N3` DECIMAL(10,4) COMMENT '',
    `P4` DECIMAL(10,4) COMMENT '',
    `N4` DECIMAL(10,4) COMMENT '',
    `P5` DECIMAL(10,4) COMMENT '',
    `N5` DECIMAL(10,4) COMMENT '',
    `P6` DECIMAL(10,4) COMMENT '',
    `N6` DECIMAL(10,4) COMMENT '',
    `P7` DECIMAL(10,4) COMMENT '',
    `N7` DECIMAL(10,4) COMMENT '',
    `P8` DECIMAL(10,4) COMMENT '',
    `N8` DECIMAL(10,4) COMMENT '',

    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_street_type_id` (`internal_street_type_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),
    KEY `key_N_NAME` (`N_NAME`),
    KEY `key_VUL_NAME` (`VUL_NAME`),
    KEY `key_BLD_NUM` (`BLD_NUM`),
    KEY `key_FLAT` (`FLAT`),
    CONSTRAINT `fk_actual_payment__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_actual_payment__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_actual_payment__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_actual_payment__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_actual_payment__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Фактические начисления';

-- ------------------------------
-- Person account
-- ------------------------------
DROP TABLE IF EXISTS `person_account`;

CREATE TABLE `person_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор счета абонента',
    `first_name`VARCHAR(100) NOT NULL COMMENT 'Имя',
    `middle_name` VARCHAR(100) NOT NULL COMMENT 'Отчество',
    `last_name` VARCHAR(100) NOT NULL COMMENT 'Фамилия',
    `city` VARCHAR(100) NOT NULL COMMENT 'Населенный пункт',
    `street_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'Тип улицы',
    `street` VARCHAR(100) NOT NULL COMMENT 'Улица',
    `building_num` VARCHAR(100) NOT NULL COMMENT 'Номер дома',
    `building_corp` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Корпус',
    `apartment` VARCHAR(100) NOT NULL COMMENT 'Номер квартиры',
    `account_number` VARCHAR(100) NOT NULL COMMENT 'Счет абонента',
    `oszn_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор отдела соц. защиты населения',
    `calc_center_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор центра начислений',
    `pu_account_number` VARCHAR(100) NOT NULL COMMENT 'Личный счет в обслуживающей организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street_type`, `street`, `building_num`,
        `building_corp`, `apartment`, `oszn_id`, `calc_center_id`, `pu_account_number`),
    KEY `key_oszn_id` (`oszn_id`),
    KEY `key_calc_center_id` (`calc_center_id`),
    CONSTRAINT `fk_person_account__osnz_organization` FOREIGN KEY (`oszn_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_person_account__calc_center_organization` FOREIGN KEY (`calc_center_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Счет абонента';

-- ------------------------------
-- Corrections
-- ------------------------------
DROP TABLE IF EXISTS `city_correction`;

CREATE TABLE `city_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Не используется',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название населенного пункта',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_city_correction__city` FOREIGN KEY (`object_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_city_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_city_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Коррекция населенного пункта';

DROP TABLE IF EXISTS `city_type_correction`;

CREATE TABLE `city_type_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Не используется',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа населенного пункта',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа населенного пункта',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_type_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_city_type_correction__city_type` FOREIGN KEY (`object_id`) REFERENCES `city_type` (`object_id`),
    CONSTRAINT `fk_city_type_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_city_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Коррекция типа населенного пункта';

DROP TABLE IF EXISTS `district_correction`;

CREATE TABLE `district_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта района',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название района',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_district_correction` (`parent_id`, `object_id`, `correction`, `organization_id`, `internal_organization_id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_parent_id` (`parent_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_district_correction__district` FOREIGN KEY (`object_id`) REFERENCES `district` (`object_id`),
    CONSTRAINT `fk_district_correction__city_correction` FOREIGN KEY (`parent_id`) REFERENCES `city_correction` (`id`),
    CONSTRAINT `fk_district_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_district_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Коррекция района';

DROP TABLE IF EXISTS `street_correction`;

CREATE TABLE `street_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта улицы',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название улицы',
    `street_type_correction_id` BIGINT(20) NULL COMMENT 'Идентификатор коррекции типа улицы',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_street_correction` (`parent_id`, `correction`, `organization_id`, `internal_organization_id`,
            `street_type_correction_id`, `object_id`, `organization_code`),
    KEY `key_object_id` (`object_id`),
    KEY `key_parent_id` (`parent_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    KEY `key_street_type_correction_id` (`street_type_correction_id`),
    CONSTRAINT `fk_street_correction__street` FOREIGN KEY (`object_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_street_correction__city_correction` FOREIGN KEY (`parent_id`) REFERENCES `city_correction` (`id`),
    CONSTRAINT `fk_street_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_street_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_street_correction__street_type_correctionn` FOREIGN KEY (`street_type_correction_id`) REFERENCES `street_type_correction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Коррекция улицы';

DROP TABLE IF EXISTS `street_type_correction`;

CREATE TABLE `street_type_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Не используется',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа улицы',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа улицы',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_street_type_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_street_type_correction__street_type` FOREIGN KEY (`object_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_street_type_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_street_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Коррекция типа улицы';

DROP TABLE IF EXISTS `building_correction`;

CREATE TABLE `building_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `parent_id` BIGINT(20) COMMENT 'Идентификатор коррекции улицы',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта дома',
    `correction` VARCHAR(20) NOT NULL COMMENT 'Номер дома',
    `correction_corp` VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'Корпус дома',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_building_correction` (`parent_id`, `object_id`, `correction`, `correction_corp`, `organization_id`, `internal_organization_id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_parent_id` (`parent_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_building_correction__building` FOREIGN KEY (`object_id`) REFERENCES `building` (`object_id`),
    CONSTRAINT `fk_building_correction__street_correction` FOREIGN KEY (`parent_id`) REFERENCES `street_correction` (`id`),
    CONSTRAINT `fk_building_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_building_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Коррекция дома';

DROP TABLE IF EXISTS `ownership_correction`;

CREATE TABLE `ownership_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта формы собственности',
    `parent_id` BIGINT(20) COMMENT 'Не используется',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название формы собственности',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ownership_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`, `organization_code`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_ownership_correction` FOREIGN KEY (`object_id`) REFERENCES `ownership` (`object_id`),
    CONSTRAINT `fk_ownership_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_ownership_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Коррекция формы собственности';

DROP TABLE IF EXISTS `privilege_correction`;

CREATE TABLE `privilege_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта льготы',
    `parent_id` BIGINT(20) COMMENT 'Не используется',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название льготы',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `organization_code` VARCHAR(100) COMMENT 'Код организации',
    `internal_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_privilege_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`, `organization_code`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_privilege_correction` FOREIGN KEY (`object_id`) REFERENCES `privilege` (`object_id`),
    CONSTRAINT `fk_privilege_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_privilege_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Коррекция льгот';

-- ------------------------------
-- Status descriptions. Read only, use only for reports.
-- ------------------------------

DROP TABLE IF EXISTS `status_description`;

CREATE TABLE `status_description` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
    `code` INTEGER NOT NULL COMMENT 'Код описания статуса',
    `name` VARCHAR(500) NOT NULL COMMENT 'Описание статуса',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_status_description` (`code`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Описание статутов';

-- ------------------------------
-- Request warning
-- ------------------------------

DROP TABLE IF EXISTS `request_warning`;

CREATE TABLE `request_warning` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор предупреждения',
    `request_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запроса',
    `request_file_type` VARCHAR(50) NOT NULL COMMENT 'Типа файла запроса. Возможные значения: BENEFIT, PAYMENT, TARIF, ACTUAL_PAYMENT',
    `status` BIGINT(20) NOT NULL COMMENT 'Код статуса. См. класс RequestWarningStatus',
    PRIMARY KEY (`id`),
    KEY `key_request_warning__request` (`request_id`),
    KEY `key_request_warning__request_file` (`request_file_type`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Расширенные сообщения предупреждений обработки файлов запросов';

DROP TABLE IF EXISTS `request_warning_parameter`;

CREATE TABLE `request_warning_parameter` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор параметра предупреждений',
    `request_warning_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор предупреждения',
    `order` INTEGER NOT NULL COMMENT 'Порядок',
    `type` VARCHAR(100) NULL COMMENT 'Тип',
    `value` VARCHAR(500) NOT NULL COMMENT 'Значение',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_warning_parameter` (`request_warning_id`, `order`),
    CONSTRAINT `fk_request_warning_parameter__request_warning` FOREIGN KEY (`request_warning_id`) REFERENCES `request_warning` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Параметры предупреждений';

-- ------------------------------
-- Service provider types
-- ------------------------------
DROP TABLE IF EXISTS `service_provider_type`;

CREATE TABLE `service_provider_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Не используется',
  `parent_entity_id` BIGINT(20) COMMENT 'Не используется',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия параметров объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия параметров объекта',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус объекта: ACTIVE, INACTIVE или ARCHIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_service_provider_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_service_provider_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Типы поставщиков услуг';

DROP TABLE IF EXISTS `service_provider_type_attribute`;

CREATE TABLE `service_provider_type_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута. Возможные значения: 1600 - НАЗВАНИЕ',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа. Возможные значения: 1600 - STRING_CULTURE',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия значений атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата завершения периода действия значений атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус атрибута: ACTIVE, INACTIVE или ARCHIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_service_provider_type_attribute__privilege` FOREIGN KEY (`object_id`) REFERENCES `privilege`(`object_id`),
  CONSTRAINT `fk_service_provider_type_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_service_provider_type_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Атрибуты объекта типа поставщика услуг';

DROP TABLE IF EXISTS `service_provider_type_string_culture`;

CREATE TABLE `service_provider_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор значения',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_service_provider_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Локализация атрибутов типа поставщика услуг';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
