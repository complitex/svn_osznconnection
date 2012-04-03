-- Adds handling support of new file type - subsidy.

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

    `FIO` VARCHAR(30) COMMENT 'ФИО',
    `first_name` VARCHAR(30) COMMENT 'Имя',
    `last_name` VARCHAR(30) COMMENT 'Фамилия',
    `middle_name` VARCHAR(30) COMMENT 'Отчество',
    `ID_RAJ` VARCHAR(5) COMMENT 'Код района',
    `NP_CODE` VARCHAR(5) COMMENT 'Код населенного пункта',
    `NP_NAME` VARCHAR(30) COMMENT 'Название населенного пункта',
    `CAT_V` VARCHAR(7) COMMENT 'Тип улицы',
    `VULCOD` VARCHAR(8) COMMENT 'Код улицы',
    `NAME_V` VARCHAR(30) COMMENT 'Название улицы',
    `BLD` VARCHAR(7) COMMENT 'Номер дома',
    `CORP` VARCHAR(2) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(9) COMMENT 'Номер квартиры',
    `RASH` VARCHAR (15) COMMENT 'Номер л/с ПУ',
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
    `NUMM` INT(2) COMMENT '',
    `SUBS` DECIMAL(13,2) COMMENT '',
    `KVT` INT(3) COMMENT '',

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
) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Файлы субсидий';

INSERT INTO `update` (`version`) VALUE ('201203403_741_0.1.27');