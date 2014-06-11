/*!40101 SET NAMES 'utf8' */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ------------------------------
-- Forms of Ownership
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Форма собственности';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Атрибуты объекта формы собственности';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Локализированное значение атрибута формы собственности';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Привилегия';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Атрибуты объекта привилегии';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Локализированное значение атрибута привилегий';

-- ------------------------------
-- Request File Group
-- ------------------------------
DROP TABLE IF EXISTS `request_file_group`;

CREATE TABLE `request_file_group` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор группы фалов запросов',
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата создания',
    `status` INTEGER COMMENT 'Код статуса. См. таблицу status_description и класс RequestFileStatus',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Группа файлов запросов (начисления + льготы)';

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
    `registry` INTEGER(2) NOT NULL COMMENT 'Номер реестра',
    `begin_date` DATE NOT NULL COMMENT 'Дата начала',
    `end_date` DATE NULL COMMENT 'Дата окончания',
    `dbf_record_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Количество записей в исходном файле',
    `length` BIGINT(20) COMMENT 'Размер файла. Не используется',
    `check_sum` VARCHAR(32) COMMENT 'Контрольная сумма. Не используется',
    `type` INTEGER COMMENT 'См. таблицу type_description и класс RequestFileType',
    `status` INTEGER COMMENT 'См. таблицу status_description и класс RequestFileStatus',
    `user_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации пользователя, который загрузил текущий файл',
    PRIMARY KEY (`id`),
    UNIQUE KEY `request_file_unique_id` (`name`, `organization_id`, `user_organization_id`, `registry`, `begin_date`, `end_date`),
    KEY `key_group_id` (`group_id`),
    KEY `key_loaded` (`loaded`),
    KEY `key_name` (`name`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_registry` (`registry`),
    KEY `key_begin_date` (`begin_date`),
    KEY `key_end_date` (`end_date`),
    KEY `key_type` (`type`) ,
    KEY `key_user_organization_id` (`user_organization_id`),
    CONSTRAINT `fk_request_file__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_request_file__request_file_group` FOREIGN KEY (`group_id`) REFERENCES `request_file_group` (`id`),
    CONSTRAINT `fk_request_file__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файл запросов';

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

    `OWN_NUM` VARCHAR(15) COMMENT 'Номер дела',
    `REE_NUM` INTEGER(2) COMMENT 'Номер реестра',
    `OPP` VARCHAR(8) COMMENT 'Признаки наличия услуг',
	`NUMB` INTEGER(2) COMMENT 'Общее число зарегистрированных',
	`MARK` INTEGER(2) COMMENT 'К-во людей, которые пользуются льготами',
	`CODE` INTEGER(4) COMMENT 'Код ЖЭО',
	`ENT_COD` INTEGER(10) COMMENT 'Код ЖЭО ОКПО',
	`FROG`  DECIMAL(5,1) COMMENT 'Процент льгот',
    `FL_PAY` DECIMAL(9,2) COMMENT 'Общая плата',
	`NM_PAY` DECIMAL(9,2) COMMENT 'Плата в пределах норм потребления',
	`DEBT` DECIMAL(9,2) COMMENT 'Сумма долга',
	`CODE2_1` INTEGER(6) COMMENT 'Оплата жилья',
	`CODE2_2` INTEGER(6) COMMENT 'система',
	`CODE2_3` INTEGER(6) COMMENT 'Горячее водоснабжение',
	`CODE2_4` INTEGER(6) COMMENT 'Холодное водоснабжение',
	`CODE2_5` INTEGER(6) COMMENT 'Газоснабжение',
	`CODE2_6` INTEGER(6) COMMENT 'Электроэнергия',
	`CODE2_7` INTEGER(6) COMMENT 'Вывоз мусора',
	`CODE2_8` INTEGER(6) COMMENT 'Водоотведение',
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
	`OZN_PRZ` INTEGER(1) COMMENT 'Признак (0 - автоматическое назначение, 1-для ручного расчета)',
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
	`CODE3_1` INTEGER(6) COMMENT 'Код тарифа оплаты жилья',
	`CODE3_2` INTEGER(6) COMMENT 'Код тарифа отопления',
	`CODE3_3` INTEGER(6) COMMENT 'Код тарифа горячего водоснабжения',
	`CODE3_4` INTEGER(6) COMMENT 'Код тарифа холодного водоснабжения',
	`CODE3_5` INTEGER(6) COMMENT 'Код тарифа - газоснабжение',
	`CODE3_6` INTEGER(6) COMMENT 'Код тарифа-электроэнергии',
	`CODE3_7` INTEGER(6) COMMENT 'Код тарифа - вывоз мусора',
	`CODE3_8` INTEGER(6) COMMENT 'Код тарифа - водоотведение',
	`OPP_SERV` VARCHAR(8) COMMENT 'Резерв',
	`RESERV1` INTEGER(10) COMMENT 'Резерв',
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Начисления';

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
	`REE_NUM` INTEGER(2) COMMENT 'Номер реестра',
	`OWN_NUM_SR` VARCHAR(15) COMMENT 'Лицевой счет в обслуж. организации',
	`FAM_NUM` INTEGER(2) COMMENT 'Номер члена семьи',
	`SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
	`F_NAM` VARCHAR(15) COMMENT 'Имя',
	`M_NAM` VARCHAR(20) COMMENT 'Отчество',
	`IND_COD` VARCHAR(10) COMMENT 'Идентификационный номер',
	`PSP_SER` VARCHAR(6) COMMENT 'Серия паспорта',
	`PSP_NUM` VARCHAR(6) COMMENT 'Номер паспорта',
	`OZN` INTEGER(1) COMMENT 'Признак владельца',
	`CM_AREA` DECIMAL(10,2) COMMENT 'Общая площадь',
	`HEAT_AREA` DECIMAL(10,2) COMMENT 'Обогреваемая площадь',
	`OWN_FRM` INTEGER(6) COMMENT 'Форма собственности',
	`HOSTEL` INTEGER(2) COMMENT 'Количество комнат',
	`PRIV_CAT` INTEGER(3) COMMENT 'Категория льготы на платежи',
	`ORD_FAM` INTEGER(2) COMMENT 'Порядок семьи льготников для расчета платежей',
	`OZN_SQ_ADD` INTEGER(1) COMMENT 'Признак учета дополнительной площади',
	`OZN_ABS` INTEGER(1) COMMENT 'Признак отсутствия данных в базе ЖЭО',
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Льготы';

-- ------------------------------
-- Subsudy Tarif
-- ------------------------------
DROP TABLE IF EXISTS `subsidy_tarif`;

CREATE TABLE `subsidy_tarif` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор тарифа',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла тарифов',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `T11_DATA_T` VARCHAR(10) COMMENT '',
    `T11_DATA_E` VARCHAR(10) COMMENT '',
    `T11_DATA_R` VARCHAR(10) COMMENT '',
    `T11_MARK` INTEGER(3) COMMENT '',
    `T11_TARN` INTEGER(6) COMMENT '',
    `T11_CODE1` INTEGER(3) COMMENT '',
    `T11_CODE2` INTEGER(6) COMMENT '',
    `T11_COD_NA` VARCHAR(40) COMMENT '',
    `T11_CODE3` INTEGER(6) COMMENT '',
    `T11_NORM_U` DECIMAL(19, 10) COMMENT '',
    `T11_NOR_US` DECIMAL(19, 10) COMMENT '',
    `T11_CODE_N` INTEGER(3) COMMENT '',
    `T11_COD_ND` INTEGER(3) COMMENT '',
    `T11_CD_UNI` INTEGER(3) COMMENT '',
    `T11_CS_UNI` DECIMAL (19, 10) COMMENT '',
    `T11_NORM` DECIMAL (19, 10) COMMENT '',
    `T11_NRM_DO` DECIMAL (19, 10) COMMENT '',
    `T11_NRM_MA` DECIMAL (19, 10) COMMENT '',
    `T11_K_NADL` DECIMAL (19, 10) COMMENT '',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Тарифы';

-- ------------------------------
-- Actual Payment
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
    `NUMB` INTEGER(2) COMMENT '',
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Фактические начисления';

-- ------------------------------
-- Subsidy
-- ------------------------------

DROP TABLE IF EXISTS `subsidy`;

CREATE TABLE `subsidy` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор субсидии',
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

    `first_name` VARCHAR(100) COMMENT 'Имя',
    `last_name` VARCHAR(100) COMMENT 'Фамилия',
    `middle_name` VARCHAR(100) COMMENT 'Отчество',

    `FIO` VARCHAR(30) COMMENT 'ФИО',
    `ID_RAJ` VARCHAR(5) COMMENT 'Код района',
    `NP_CODE` VARCHAR(5) COMMENT 'Код населенного пункта',
    `NP_NAME`VARCHAR(30) COMMENT 'Название населенного пункта',
    `CAT_V` VARCHAR(7) COMMENT 'Тип улицы',
    `VULCOD` VARCHAR(8) COMMENT 'Код улицы',
    `NAME_V` VARCHAR(30) COMMENT 'Название улицы',
    `BLD` VARCHAR(7) COMMENT 'Номер дома',
    `CORP` VARCHAR(2) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(9) COMMENT 'Номер квартиры',
    `RASH` VARCHAR(15) COMMENT 'Номер л/с ПУ',
    `NUMB` VARCHAR(8) COMMENT '',
    `DAT1` DATE COMMENT 'Дата начала периода, на который предоставляется субсидия',
    `DAT2` DATE COMMENT 'Дата конца периода, на который предоставляется субсидия',
    `NM_PAY` DECIMAL(9,2) COMMENT 'Начисление в пределах нормы',

    `P1` DECIMAL(9,4) COMMENT '',
    `P2` DECIMAL(9,4) COMMENT '',
    `P3` DECIMAL(9,4) COMMENT '',
    `P4` DECIMAL(9,4) COMMENT '',
    `P5` DECIMAL(9,4) COMMENT '',
    `P6` DECIMAL(9,4) COMMENT '',
    `P7` DECIMAL(9,4) COMMENT '',
    `P8` DECIMAL(9,4) COMMENT '',

    `SM1` DECIMAL(9,2) COMMENT '',
    `SM2` DECIMAL(9,2) COMMENT '',
    `SM3` DECIMAL(9,2) COMMENT '',
    `SM4` DECIMAL(9,2) COMMENT '',
    `SM5` DECIMAL(9,2) COMMENT '',
    `SM6` DECIMAL(9,2) COMMENT '',
    `SM7` DECIMAL(9,2) COMMENT '',
    `SM8` DECIMAL(9,2) COMMENT '',

    `SB1` DECIMAL(9,2) COMMENT '',
    `SB2` DECIMAL(9,2) COMMENT '',
    `SB3` DECIMAL(9,2) COMMENT '',
    `SB4` DECIMAL(9,2) COMMENT '',
    `SB5` DECIMAL(9,2) COMMENT '',
    `SB6` DECIMAL(9,2) COMMENT '',
    `SB7` DECIMAL(9,2) COMMENT '',
    `SB8` DECIMAL(9,2) COMMENT '',

    `OB1` DECIMAL(9,2) COMMENT '',
    `OB2` DECIMAL(9,2) COMMENT '',
    `OB3` DECIMAL(9,2) COMMENT '',
    `OB4` DECIMAL(9,2) COMMENT '',
    `OB5` DECIMAL(9,2) COMMENT '',
    `OB6` DECIMAL(9,2) COMMENT '',
    `OB7` DECIMAL(9,2) COMMENT '',
    `OB8` DECIMAL(9,2) COMMENT '',

    `SUMMA` DECIMAL(13,2) COMMENT '',
    `NUMM` INTEGER(2) COMMENT '',
    `SUBS` DECIMAL(13,2) COMMENT '',
    `KVT` INTEGER(3) COMMENT '',

    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_street_type_id` (`internal_street_type_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_FIO` (`FIO`),
    KEY `key_NP_NAME` (`NP_NAME`),
    KEY `key_NAME_V` (`NAME_V`),
    KEY `key_BLD` (`BLD`),
    KEY `key_CORP` (`CORP`),
    KEY `key_FLAT` (`FLAT`),
    CONSTRAINT `fk_subsidy__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_subsidy__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_subsidy__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_subsidy__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_subsidy__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы субсидий';

-- ------------------------------
-- Subsidy Master Data
-- ------------------------------

DROP TABLE IF EXISTS `subsidy_master_data`;

CREATE TABLE `subsidy_master_data` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `servicing_organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор обслуживающей организации',
  `DOM` INTEGER(3) COMMENT 'код дома (заполняется при обработке)',
  `REG` INTEGER(3) COMMENT 'код района (заполняется при обработке)',
  `LS`  INTEGER(6) COMMENT 'лицевой счет (заполняется при обработке)',
  `DELO` VARCHAR(8) COMMENT 'номер дела (в отделе субсидий) (NUMB)',
  `TOT` DECIMAL(8,2) COMMENT 'всего начислено субсидии (SUBS)',
  `PERIOD` DATE COMMENT 'период (YYMM – месяц файла субсидии)',
  `FIO` VARCHAR(30) COMMENT 'ФИО (FIO)',
  `ADRES` VARCHAR(34) COMMENT 'адрес (NAME_V + “ ” + CAT_V + “ ”+ BLD + “/” + CORP + “кв.” + FLAT)',
  `NKW` VARCHAR(9) COMMENT 'номер квартиры (FLAT)',
  `KWART` DECIMAL(8,2) COMMENT 'начислено субсидии за квартплату (SB1)',
  `OTOPL` DECIMAL(8,2) COMMENT 'начислено субсидии за отопление (SB2)',
  `PODOGR` DECIMAL(8,2) COMMENT 'начислено субсидии за горячее водоснабжение (SB3)',
  `WODA` DECIMAL(8,2) COMMENT 'начислено субсидии за холодную воду с учетом водоотведения холодной води (SB4)',
  `GAZ` DECIMAL(8,2) COMMENT 'начислено субсидии за газ (SB5)',
  `ELEKTR` DECIMAL(8,2) COMMENT 'начислено субсидии за электроэнергию (SB6)',
  `STOKI` DECIMAL(8,2) COMMENT 'начислено субсидии за отведение горячей воды (SB8)',
  `TOT_O` DECIMAL(8,2) COMMENT 'общая сумма обязательного платежа',
  `KWART_O` DECIMAL(8,2) COMMENT 'обязательный платеж за квартплату (OB1)',
  `OTOPL_O` DECIMAL(8,2) COMMENT 'обязательный платеж за отопление (OB2)',
  `GORWODA_O` DECIMAL(8,2) COMMENT 'обязательный платеж за горячее водоснабжение (OB3)',
  `WODA_O` DECIMAL(8,2) COMMENT 'обязательный платеж за холодную воду с учетом водоотведения (OB4)',
  `GAZ_O` DECIMAL(8,2) COMMENT 'обязательный платеж за газ (OB5)',
  `ELEKTR_O` DECIMAL(8,2) COMMENT 'обязательный платеж за электроэнергию (OB6)',
  `STOKI_O` DECIMAL(8,2) COMMENT 'обязательный платеж за отведение гор.воды (OB8)',
  `VC` TINYINT(1) DEFAULT 0 COMMENT 'Всегда 0',
  `PLE` TINYINT(1) DEFAULT 0 COMMENT 'Всегда 0',
  `BEGIN0` DATE COMMENT 'начало действия субсидии (YYYYMMDD)',
  `END0` DATE COMMENT 'конец действия субсидии (YYYYMMDD)',
  `PR_KV` TINYINT(1) COMMENT 'признак назначения субсидии на текущий период (1 — да, 0 - нет)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Таблица мастер-данных файла субсидий';

-- ------------------------------
-- Subsidy Master Data Part
-- ------------------------------

DROP TABLE IF EXISTS `subsidy_master_data_part`;

CREATE TABLE `subsidy_master_data_part` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `subsidy_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор записи файла субсидии',
  `subsidy_master_data_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор записи мастер данных',
  PRIMARY KEY (`id`),
  KEY `key_subsidy_id` (`subsidy_id`),
  KEY `key_subsidy_master_data_id` (`subsidy_master_data_id`),
  UNIQUE KEY `key_unique` (`subsidy_id`, subsidy_master_data_id),
  CONSTRAINT `fk_subsidy_master_data_part__subsidy` FOREIGN KEY (`subsidy_id`)
    REFERENCES `subsidy` (`id`),
  CONSTRAINT `fk_subsidy_master_data_part__subsidy_master_data` FOREIGN KEY (`subsidy_master_data_id`)
    REFERENCES `subsidy_master_data` (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Много мастер-данных файла субсидий';

-- ------------------------------
-- Dwelling Characteristic
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
    `HOUSE` VARCHAR(100) COMMENT 'Номер дома',
    `BUILD` VARCHAR(100) COMMENT 'Корпус',
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
    KEY `key_BUILD` (`BUILD`),
    KEY `key_APT` (`APT`),
    CONSTRAINT `fk_dwelling_characteristics__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_dwelling_characteristics__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_dwelling_characteristics__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы-запросы характеристик жилья';

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
    `HOUSE` VARCHAR(100) COMMENT 'Номер дома',
    `BUILD` VARCHAR(100) COMMENT 'Корпус',
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
    KEY `key_BUILD` (`BUILD`),
    KEY `key_APT` (`APT`),
    CONSTRAINT `fk_facility_service_type__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_facility_service_type__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_facility_service_type__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_facility_service_type__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_facility_service_type__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы-запросы видов услуг';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы форма-2 льгота';

-- ------------------------------
-- Facility Street Type Reference
-- ------------------------------

DROP TABLE IF EXISTS `facility_street_type_reference`;

CREATE TABLE `facility_street_type_reference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор объекта тип улицы',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла типов улиц',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `KLKUL_CODE` VARCHAR(100) COMMENT 'Код типа улицы',
    `KLKUL_NAME` VARCHAR(100) COMMENT 'Наименование типа улицы',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы-справочники типов улиц';

-- ------------------------------
-- Facility Street Reference
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы-справочники улиц';

-- ------------------------------
-- Facility Tarif Reference
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Файлы-справочники тарифов для запросов по льготам';

-- ------------------------------
-- Person Account
-- ------------------------------
DROP TABLE IF EXISTS `person_account`;

CREATE TABLE `person_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор счета абонента',
    `first_name`VARCHAR(100) NOT NULL COMMENT 'Имя',
    `middle_name` VARCHAR(100) NOT NULL COMMENT 'Отчество',
    `last_name` VARCHAR(100) NOT NULL COMMENT 'Фамилия',

    `city` VARCHAR(100) COMMENT 'Населенный пункт',
    `street_type` VARCHAR(50) COMMENT 'Тип улицы',
    `street` VARCHAR(100) COMMENT 'Улица',
    `building_number` VARCHAR(20) COMMENT 'Номер дома',
    `building_corp` VARCHAR(20) COMMENT 'Корпус',
    `apartment` VARCHAR(20) COMMENT 'Номер квартиры',

    `city_object_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
    `street_object_id` BIGINT(20) COMMENT 'Идентификатор улицы',
    `street_type_object_id` BIGINT(20) COMMENT 'Идентификатор типа улицы',
    `building_object_id` BIGINT(20) COMMENT 'Идентификатор дома',
    `apartment_object_id` BIGINT (20) COMMENT 'Идентификатор квартиры',

    `account_number` VARCHAR(100) NOT NULL COMMENT 'Счет абонента',
    `pu_account_number` VARCHAR(100) NOT NULL COMMENT 'Личный счет в обслуживающей организации',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор отдела соц. защиты населения',
    `user_organization_id` BIGINT(20),
    `calc_center_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор центра начислений',

    PRIMARY KEY (`id`),

    UNIQUE KEY `unique_person_account` (`first_name`, `middle_name`, `last_name`, `city_object_id`, `street_object_id`,
      `building_object_id`, `apartment_object_id`,`pu_account_number`,`organization_id`, `user_organization_id`,
      `calc_center_id`),

    KEY `key_city_object_id` (`city_object_id`),
    KEY `key_street_object_id` (`street_object_id`),
    KEY `key_street_type_object_id` (`street_type_object_id`),
    KEY `key_building_object_id` (`building_object_id`),
    KEY `key_apartment_object_id` (`apartment_object_id`),

    KEY `key_organization_id` (`organization_id`),
    KEY `key_calc_center_id` (`calc_center_id`),
    KEY `key_user_organization_id` (`user_organization_id`),

    CONSTRAINT `fk_person_account__city` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_person_account__street` FOREIGN KEY (`street_object_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_person_account__street_type` FOREIGN KEY (`street_type_object_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_person_account__building` FOREIGN KEY (`building_object_id`) REFERENCES `building` (`object_id`),
    CONSTRAINT `fk_person_account__apartment` FOREIGN KEY (`apartment_object_id`) REFERENCES `apartment` (`object_id`),

    CONSTRAINT `fk_person_account__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_person_account__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_person_account__calc_center_organization` FOREIGN KEY (`calc_center_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Соответствие лицевого счета';

DROP TABLE IF EXISTS `ownership_correction`;

CREATE TABLE `ownership_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `external_id` VARCHAR(20) COMMENT 'Код организации',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта формы собственности',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название формы собственности',
    `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
    `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `user_organization_id` BIGINT(20),
    `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор внутренней организации',
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_user_organization_id` (`user_organization_id`),
    CONSTRAINT `fk_ownership_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_ownership_correction` FOREIGN KEY (`object_id`) REFERENCES `ownership` (`object_id`),
    CONSTRAINT `fk_ownership_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция формы собственности';

DROP TABLE IF EXISTS `privilege_correction`;

CREATE TABLE `privilege_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
    `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта льготы',
    `correction` VARCHAR(100) NOT NULL COMMENT 'Название льготы',
    `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
    `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
    `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `user_organization_id` BIGINT(20),
    `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
    `status` INTEGER COMMENT 'Статус',
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_begin_date` (`begin_date`),
    KEY `key_end_date` (`end_date`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_user_organization_id` (`user_organization_id`),
    KEY `key_module_id` (`module_id`),
    KEY `key_status` (`status`),
    CONSTRAINT `fk_privilege_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_privilege_correction` FOREIGN KEY (`object_id`) REFERENCES `privilege` (`object_id`),
    CONSTRAINT `fk_privilege_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция льгот';

-- ------------------------------
-- Status Descriptions. Read only, use only for reports.
-- ------------------------------

DROP TABLE IF EXISTS `status_description`;

CREATE TABLE `status_description` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
    `code` INTEGER NOT NULL COMMENT 'Код описания статуса',
    `name` VARCHAR(500) NOT NULL COMMENT 'Описание статуса',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_status_description` (`code`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Описание статутов';

-- ------------------------------
-- Type Descriptions. Read only, use only for reports.
-- ------------------------------

DROP TABLE IF EXISTS `type_description`;

CREATE TABLE `type_description` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `code` INTEGER NOT NULL COMMENT 'Код описания типа',
  `name` VARCHAR(500) NOT NULL COMMENT 'Описание типа',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_description` (`code`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Описание типов';

-- ------------------------------
-- Request Warning
-- ------------------------------

DROP TABLE IF EXISTS `request_warning`;

CREATE TABLE `request_warning` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор предупреждения',
    `request_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запроса',
    `request_file_type` INTEGER NOT NULL COMMENT 'Типа файла запроса. См. RequestFileType',
    `status` BIGINT(20) NOT NULL COMMENT 'Код статуса. См. класс RequestWarningStatus',
    PRIMARY KEY (`id`),
    KEY `key_request_warning__request` (`request_id`),
    KEY `key_request_warning__request_file` (`request_file_type`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Расширенные сообщения предупреждений обработки файлов запросов';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Параметры предупреждений';

-- ------------------------------
-- Service Provider Types
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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Типы поставщиков услуг';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Атрибуты объекта типа поставщика услуг';

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Локализация атрибутов типа поставщика услуг';

-- ------------------------------
-- Service Association
-- ------------------------------
DROP TABLE IF EXISTS `service_association`;

CREATE TABLE `service_association` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `service_provider_type_id` BIGINT(20) NOT NULL COMMENT 'ID объекта типа поставщика услуги',
  `calculation_center_id` BIGINT(20) NOT NULL COMMENT 'ID модуля начислений',
  PRIMARY KEY (`pk_id`),
  KEY `key_service_provider_type_id` (`service_provider_type_id`),
  KEY `key_calculation_center_id` (`calculation_center_id`),
  CONSTRAINT `fk_service_association__service_provider_type` FOREIGN KEY (`service_provider_type_id`) REFERENCES `service_provider_type` (`object_id`),
  CONSTRAINT `fk_service_association__calculation_center` FOREIGN KEY (`calculation_center_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Пары ассоциаций: тип услуги - модуль начислений';

-- ------------------------------
-- Request File Description
-- ------------------------------
DROP TABLE IF EXISTS `request_file_description`;

CREATE TABLE `request_file_description` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `request_file_type` VARCHAR(50) NOT NULL COMMENT 'Тип файла запроса',
  `date_pattern` VARCHAR(50) NOT NULL COMMENT 'Шаблон значений типа дата',
  PRIMARY KEY (`id`),
  KEY `key_request_file_type` (`request_file_type`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Описание структуры файлов запросов';

DROP TABLE IF EXISTS `request_file_field_description`;

CREATE TABLE `request_file_field_description` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `request_file_description_id` BIGINT(20) NOT NULL COMMENT 'Ссылка на описание структуры файла запроса',
  `name` VARCHAR(100) NOT NULL COMMENT 'Имя поля',
  `type` VARCHAR(100) NOT NULL COMMENT 'Java тип значений поля',
  `length` INTEGER NOT NULL COMMENT 'Длина поля',
  `scale` INTEGER COMMENT 'Количество знаков после запятой, если тип поля дробный',
  PRIMARY KEY (`id`),
  KEY `key_request_file_description_id` (`request_file_description_id`),
  CONSTRAINT `fk_request_file_field_description__request_file_description_id` FOREIGN KEY (`request_file_description_id`) REFERENCES `request_file_description` (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Описание структуры поля файла запросов';

-- ------------------------------
-- Request File Status
-- ------------------------------

DROP TABLE IF EXISTS `request_file_history`;

CREATE TABLE `request_file_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла запроса',
  `status` INTEGER NOT NULL COMMENT 'Статус файла запроса',
  `date` TIMESTAMP NOT NULL COMMENT 'Дата утановки статуса',
  PRIMARY KEY (`id`),
  KEY `key_request_file_id` (`request_file_id`),
  KEY `status` (`status`),
  KEY `date` (`date`),
  CONSTRAINT `fk_request_file_history__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'История файла запроса';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
