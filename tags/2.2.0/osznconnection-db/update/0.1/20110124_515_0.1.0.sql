-- Script adds support for actual payments.

-- ------------------------------
-- Actual payment
-- ------------------------------

DROP TABLE IF EXISTS `actual_payment`;

CREATE TABLE `actual_payment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` BIGINT(20) NOT NULL,
    `account_number` VARCHAR(100),

    `internal_city_id` BIGINT(20),
    `internal_street_id` BIGINT(20),
    `internal_street_type_id` BIGINT(20),
    `internal_building_id` BIGINT(20),

    `outgoing_city` VARCHAR(100),
    `outgoing_district` VARCHAR(100),
    `outgoing_street` VARCHAR(100),
    `outgoing_street_type` VARCHAR(100),
    `outgoing_building_number` VARCHAR(100),
    `outgoing_building_corp` VARCHAR(100),
    `outgoing_apartment` VARCHAR(100),

    `status` INTEGER NOT NULL DEFAULT 200 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus',

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
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE `person_account` DROP KEY `person_account_unique_key`, ADD COLUMN `street_type` VARCHAR(20) NULL AFTER `city`,
ADD COLUMN `street_code` VARCHAR(10) NULL AFTER `street`, MODIFY COLUMN `own_num_sr` VARCHAR(15) NULL;

ALTER TABLE `request_file` ADD COLUMN `status` INTEGER;

INSERT INTO `update` (`version`) VALUE ('20110124_515_0.1.0');

