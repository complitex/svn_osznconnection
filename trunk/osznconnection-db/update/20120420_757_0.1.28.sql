-- 1. Converts all request files data to string representation.
-- 2. Adds support for request file description metadata.

-- 1. Create temporal tables.

-- ------------------------------
-- Temp Payment
-- ------------------------------
CREATE TABLE `temp_payment` (
    `id` BIGINT(20) NOT NULL,
    `request_file_id` BIGINT(20) NOT NULL,
    `account_number` VARCHAR(100),

    `internal_city_id` BIGINT(20),
    `internal_street_id` BIGINT(20),
    `internal_street_type_id` BIGINT(20),
    `internal_building_id` BIGINT(20),
    `internal_apartment_id` BIGINT(20),

    `outgoing_city` VARCHAR(100),
    `outgoing_district` VARCHAR(100),
    `outgoing_street` VARCHAR(100),
    `outgoing_street_type` VARCHAR(100),
    `outgoing_building_number` VARCHAR(100),
    `outgoing_building_corp` VARCHAR(100),
    `outgoing_apartment` VARCHAR(100),

    `status` INTEGER NOT NULL DEFAULT 240,

    `OWN_NUM` VARCHAR (15),
    `REE_NUM` INT(2),
    `OPP` VARCHAR(8),
    `NUMB` INT(2),
    `MARK` INT(2),
    `CODE` INT(4),
    `ENT_COD` INT(10),
    `FROG`  DECIMAL(5,1),
    `FL_PAY` DECIMAL(9,2),
    `NM_PAY` DECIMAL(9,2),
    `DEBT` DECIMAL(9,2),
    `CODE2_1` INT(6),
    `CODE2_2` INT(6),
    `CODE2_3` INT(6),
    `CODE2_4` INT(6),
    `CODE2_5` INT(6),
    `CODE2_6` INT(6),
    `CODE2_7` INT(6),
    `CODE2_8` INT(6),
    `NORM_F_1` DECIMAL(10,4),
    `NORM_F_2` DECIMAL(10,4),
    `NORM_F_3` DECIMAL(10,4),
    `NORM_F_4` DECIMAL(10,4),
    `NORM_F_5` DECIMAL(10,4),
    `NORM_F_6` DECIMAL(10,4),
    `NORM_F_7` DECIMAL(10,4),
    `NORM_F_8` DECIMAL(10,4),
    `OWN_NUM_SR` VARCHAR(15),
    `DAT1` DATE,
    `DAT2` DATE,
    `OZN_PRZ` INT(1),
    `DAT_F_1` DATE,
    `DAT_F_2` DATE,
    `DAT_FOP_1` DATE,
    `DAT_FOP_2` DATE,
    `ID_RAJ` VARCHAR(5),
    `SUR_NAM` VARCHAR(30),
    `F_NAM` VARCHAR(15),
    `M_NAM` VARCHAR(20),
    `IND_COD` VARCHAR(10),
    `INDX` VARCHAR(6),
    `N_NAME` VARCHAR(30),
    `VUL_NAME` VARCHAR(30),
    `BLD_NUM` VARCHAR(7),
    `CORP_NUM` VARCHAR(2),
    `FLAT` VARCHAR(9),
    `CODE3_1` INT(6),
    `CODE3_2` INT(6),
    `CODE3_3` INT(6),
    `CODE3_4` INT(6),
    `CODE3_5` INT(6),
    `CODE3_6` INT(6),
    `CODE3_7` INT(6),
    `CODE3_8` INT(6),
    `OPP_SERV` VARCHAR(8),
    `RESERV1` INT(10),
    `RESERV2` VARCHAR(10),
    PRIMARY KEY (`id`)    
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Temp Benefit
-- ------------------------------
CREATE TABLE `temp_benefit` (
    `id` BIGINT(20) NOT NULL,
    `request_file_id` BIGINT(20) NULL,
    `account_number` VARCHAR(100) NULL,
    `status` INTEGER NOT NULL DEFAULT 240,

    `OWN_NUM` VARCHAR(15),
    `REE_NUM` INT(2),
    `OWN_NUM_SR` VARCHAR(15),
    `FAM_NUM` INT(2),
    `SUR_NAM` VARCHAR(30),
    `F_NAM` VARCHAR(15),
    `M_NAM` VARCHAR(20),
    `IND_COD` VARCHAR(10),
    `PSP_SER` VARCHAR(6),
    `PSP_NUM` VARCHAR(6),
    `OZN` INT(1),
    `CM_AREA` DECIMAL(10,2),
    `HEAT_AREA` DECIMAL(10,2),
    `OWN_FRM` INT(6),
    `HOSTEL` INT(2),
    `PRIV_CAT` INT(3),
    `ORD_FAM` INT(2),
    `OZN_SQ_ADD` INT(1),
    `OZN_ABS` INT(1),
    `RESERV1` DECIMAL(10,2),
    `RESERV2` VARCHAR(10),
    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Temp Actual payment
-- ------------------------------
CREATE TABLE `temp_actual_payment` (
    `id` BIGINT(20) NOT NULL,
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

    `status` INTEGER NOT NULL DEFAULT 240,

    `SUR_NAM` VARCHAR(30),
    `F_NAM` VARCHAR(15),
    `M_NAM` VARCHAR(20),
    `INDX` VARCHAR(6),
    `N_NAME` VARCHAR(30),
    `N_CODE` VARCHAR(5),
    `VUL_CAT` VARCHAR(7),
    `VUL_NAME` VARCHAR(30),
    `VUL_CODE` VARCHAR(5),
    `BLD_NUM` VARCHAR(7),
    `CORP_NUM` VARCHAR(2),
    `FLAT` VARCHAR(9),
    `OWN_NUM` VARCHAR (15),
    `APP_NUM` VARCHAR(8),
    `DAT_BEG` DATE,
    `DAT_END` DATE,
    `CM_AREA` DECIMAL(7,2),
    `NM_AREA` DECIMAL(7,2),
    `BLC_AREA` DECIMAL(5,2),
    `FROG` DECIMAL(5,1),
    `DEBT` DECIMAL(10,2),
    `NUMB` INT(2),
    `P1` DECIMAL(10,4),
    `N1` DECIMAL(10,4),
    `P2` DECIMAL(10,4),
    `N2` DECIMAL(10,4),
    `P3` DECIMAL(10,4),
    `N3` DECIMAL(10,4),
    `P4` DECIMAL(10,4),
    `N4` DECIMAL(10,4),
    `P5` DECIMAL(10,4),
    `N5` DECIMAL(10,4),
    `P6` DECIMAL(10,4),
    `N6` DECIMAL(10,4),
    `P7` DECIMAL(10,4),
    `N7` DECIMAL(10,4),
    `P8` DECIMAL(10,4),
    `N8` DECIMAL(10,4),

    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Temp Subsidy
-- ------------------------------
CREATE TABLE `temp_subsidy` (
    `id` BIGINT(20) NOT NULL,
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

    `status` INTEGER NOT NULL DEFAULT 240,

    `FIO` VARCHAR(30),
    `first_name` VARCHAR(30),
    `last_name` VARCHAR(30),
    `middle_name` VARCHAR(30),
    `ID_RAJ` VARCHAR(5),
    `NP_CODE` VARCHAR(5),
    `NP_NAME` VARCHAR(30),
    `CAT_V` VARCHAR(7),
    `VULCOD` VARCHAR(8),
    `NAME_V` VARCHAR(30),
    `BLD` VARCHAR(7),
    `CORP` VARCHAR(2),
    `FLAT` VARCHAR(9),
    `RASH` VARCHAR (15),
    `NUMB` VARCHAR(8),
    `DAT1` DATE,
    `DAT2` DATE,
    `NM_PAY` DECIMAL(9,2),
    
    `P1` DECIMAL(9,4),
    `P2` DECIMAL(9,4),
    `P3` DECIMAL(9,4),
    `P4` DECIMAL(9,4),
    `P5` DECIMAL(9,4),
    `P6` DECIMAL(9,4),
    `P7` DECIMAL(9,4),
    `P8` DECIMAL(9,4),

    `SM1` DECIMAL(9,2),
    `SM2` DECIMAL(9,2),
    `SM3` DECIMAL(9,2),
    `SM4` DECIMAL(9,2),
    `SM5` DECIMAL(9,2),
    `SM6` DECIMAL(9,2),
    `SM7` DECIMAL(9,2),
    `SM8` DECIMAL(9,2),

    `SB1` DECIMAL(9,2),
    `SB2` DECIMAL(9,2),
    `SB3` DECIMAL(9,2),
    `SB4` DECIMAL(9,2),
    `SB5` DECIMAL(9,2),
    `SB6` DECIMAL(9,2),
    `SB7` DECIMAL(9,2),
    `SB8` DECIMAL(9,2),

    `OB1` DECIMAL(9,2),
    `OB2` DECIMAL(9,2),
    `OB3` DECIMAL(9,2),
    `OB4` DECIMAL(9,2),
    `OB5` DECIMAL(9,2),
    `OB6` DECIMAL(9,2),
    `OB7` DECIMAL(9,2),
    `OB8` DECIMAL(9,2),

    `SUMMA` DECIMAL(13,2),
    `NUMM` INT(2),
    `SUBS` DECIMAL(13,2),
    `KVT` INT(3),

    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Temp Tarif
-- ------------------------------
CREATE TABLE `temp_tarif` (
    `id` BIGINT(20) NOT NULL,
    `request_file_id` BIGINT(20) NOT NULL,
    `status` INTEGER NULL,

    `T11_DATA_T` VARCHAR(10),
    `T11_DATA_E` VARCHAR(10),
    `T11_DATA_R` VARCHAR(10),
    `T11_MARK` INT(3),
    `T11_TARN` INT(6),
    `T11_CODE1` INT(3),
    `T11_CODE2` INT(6),
    `T11_COD_NA` VARCHAR(40),
    `T11_CODE3` INT(6),
    `T11_NORM_U` DECIMAL(19, 10),
    `T11_NOR_US` DECIMAL(19, 10),
    `T11_CODE_N` INT(3),
    `T11_COD_ND` INT(3),
    `T11_CD_UNI` INT(3),
    `T11_CS_UNI` DECIMAL (19, 10),
    `T11_NORM` DECIMAL (19, 10),
    `T11_NRM_DO` DECIMAL (19, 10),
    `T11_NRM_MA` DECIMAL (19, 10),
    `T11_K_NADL` DECIMAL (19, 10),
     PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- 2. Copy all data to temporal tables.
INSERT INTO `temp_payment` SELECT * FROM `payment`;
INSERT INTO `temp_benefit` SELECT * FROM `benefit`;
INSERT INTO `temp_actual_payment` SELECT * FROM `actual_payment`;
INSERT INTO `temp_subsidy` SELECT * FROM `subsidy`;
INSERT INTO `temp_tarif` SELECT * FROM `tarif`;

-- 3. Remove data tables.
DROP TABLE `payment`;
DROP TABLE `benefit`;
DROP TABLE `actual_payment`;
DROP TABLE `subsidy`;
DROP TABLE `tarif`;

-- 4. Create new data tables.

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

    `OWN_NUM` VARCHAR(100) COMMENT 'Номер дела',
    `REE_NUM` VARCHAR(100) COMMENT 'Номер реестра',
    `OPP` VARCHAR(100) COMMENT 'Признаки наличия услуг',
    `NUMB` VARCHAR(100) COMMENT 'Общее число зарегистрированных',
    `MARK` VARCHAR(100) COMMENT 'К-во людей, которые пользуются льготами',
    `CODE` VARCHAR(100) COMMENT 'Код ЖЭО',
    `ENT_COD` VARCHAR(100) COMMENT 'Код ЖЭО ОКПО',
    `FROG` VARCHAR(100) COMMENT 'Процент льгот',
    `FL_PAY` VARCHAR(100) COMMENT 'Общая плата',
    `NM_PAY` VARCHAR(100) COMMENT 'Плата в пределах норм потребления',
    `DEBT` VARCHAR(100) COMMENT 'Сумма долга',
    `CODE2_1` VARCHAR(100) COMMENT 'Оплата жилья',
    `CODE2_2` VARCHAR(100) COMMENT 'система',
    `CODE2_3` VARCHAR(100) COMMENT 'Горячее водоснабжение',
    `CODE2_4` VARCHAR(100) COMMENT 'Холодное водоснабжение',
    `CODE2_5` VARCHAR(100) COMMENT 'Газоснабжение',
    `CODE2_6` VARCHAR(100) COMMENT 'Электроэнергия',
    `CODE2_7` VARCHAR(100) COMMENT 'Вывоз мусора',
    `CODE2_8` VARCHAR(100) COMMENT 'Водоотведение',
    `NORM_F_1` VARCHAR(100) COMMENT 'Общая площадь (оплата жилья)',
    `NORM_F_2` VARCHAR(100) COMMENT 'Объемы потребления (отопление)',
    `NORM_F_3` VARCHAR(100) COMMENT 'Объемы потребления (горячего водо.)',
    `NORM_F_4` VARCHAR(100) COMMENT 'Объемы потребления (холодное водо.)',
    `NORM_F_5` VARCHAR(100) COMMENT 'Объемы потребления (газоснабжение)',
    `NORM_F_6` VARCHAR(100) COMMENT 'Объемы потребления (электроэнергия)',
    `NORM_F_7` VARCHAR(100) COMMENT 'Объемы потребления (вывоз мусора)',
    `NORM_F_8` VARCHAR(100) COMMENT 'Объемы потребления (водоотведение)',
    `OWN_NUM_SR` VARCHAR(100) COMMENT 'Лицевой счет в обслуж. организации',
    `DAT1` VARCHAR(100) COMMENT 'Дата начала действия субсидии',
    `DAT2` VARCHAR(100) COMMENT 'Дата формирования запроса',
    `OZN_PRZ` VARCHAR(100) COMMENT 'Признак (0 - автоматическое назначение, 1-для ручного расчета)',
    `DAT_F_1` VARCHAR(100) COMMENT 'Дата начала для факта',
    `DAT_F_2` VARCHAR(100) COMMENT 'Дата конца для факта',
    `DAT_FOP_1` VARCHAR(100) COMMENT 'Дата начала для факта отопления',
    `DAT_FOP_2` VARCHAR(100) COMMENT 'Дата конца для факта отопления',
    `ID_RAJ` VARCHAR(100) COMMENT 'Код района',
    `SUR_NAM` VARCHAR(100) COMMENT 'Фамилия',
    `F_NAM` VARCHAR(100) COMMENT 'Имя',
    `M_NAM` VARCHAR(100) COMMENT 'Отчество',
    `IND_COD` VARCHAR(100) COMMENT 'Идентификационный номер',
    `INDX` VARCHAR(100) COMMENT 'Индекс почтового отделения',
    `N_NAME` VARCHAR(100) COMMENT 'Название населенного пункта',
    `VUL_NAME` VARCHAR(100) COMMENT 'Название улицы',
    `BLD_NUM` VARCHAR(100) COMMENT 'Номер дома',
    `CORP_NUM` VARCHAR(100) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(100) COMMENT 'Номер квартиры',
    `CODE3_1` VARCHAR(100) COMMENT 'Код тарифа оплаты жилья',
    `CODE3_2` VARCHAR(100) COMMENT 'Код тарифа отопления',
    `CODE3_3` VARCHAR(100) COMMENT 'Код тарифа горячего водоснабжения',
    `CODE3_4` VARCHAR(100) COMMENT 'Код тарифа холодного водоснабжения',
    `CODE3_5` VARCHAR(100) COMMENT 'Код тарифа - газоснабжение',
    `CODE3_6` VARCHAR(100) COMMENT 'Код тарифа-электроэнергии',
    `CODE3_7` VARCHAR(100) COMMENT 'Код тарифа - вывоз мусора',
    `CODE3_8` VARCHAR(100) COMMENT 'Код тарифа - водоотведение',
    `OPP_SERV` VARCHAR(100) COMMENT 'Резерв',
    `RESERV1` VARCHAR(100) COMMENT 'Резерв',
    `RESERV2` VARCHAR(100) COMMENT 'Резер',
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
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Начисления';

-- ------------------------------
-- Benefit
-- ------------------------------
DROP TABLE IF EXISTS `benefit`;

CREATE TABLE `benefit` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор льготы',
    `request_file_id` BIGINT(20) NULL COMMENT 'Идентификатор файла запросов',
    `account_number` VARCHAR(100) NULL COMMENT 'Номер счета',
    `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `OWN_NUM` VARCHAR(100) COMMENT 'Номер дела',
    `REE_NUM` VARCHAR(100) COMMENT 'Номер реестра',
    `OWN_NUM_SR` VARCHAR(100) COMMENT 'Лицевой счет в обслуж. организации',
    `FAM_NUM` VARCHAR(100) COMMENT 'Номер члена семьи',
    `SUR_NAM` VARCHAR(100) COMMENT 'Фамилия',
    `F_NAM` VARCHAR(100) COMMENT 'Имя',
    `M_NAM` VARCHAR(100) COMMENT 'Отчество',
    `IND_COD` VARCHAR(100) COMMENT 'Идентификационный номер',
    `PSP_SER` VARCHAR(100) COMMENT 'Серия паспорта',
    `PSP_NUM` VARCHAR(100) COMMENT 'Номер паспорта',
    `OZN` VARCHAR(100) COMMENT 'Признак владельца',
    `CM_AREA` VARCHAR(100) COMMENT 'Общая площадь',
    `HEAT_AREA` VARCHAR(100) COMMENT 'Обогреваемая площадь',
    `OWN_FRM` VARCHAR(100) COMMENT 'Форма собственности',
    `HOSTEL` VARCHAR(100) COMMENT 'Количество комнат',
    `PRIV_CAT` VARCHAR(100) COMMENT 'Категория льготы на платежи',
    `ORD_FAM` VARCHAR(100) COMMENT 'Порядок семьи льготников для расчета платежей',
    `OZN_SQ_ADD` VARCHAR(100) COMMENT 'Признак учета дополнительной площади',
    `OZN_ABS` VARCHAR(100) COMMENT 'Признак отсутствия данных в базе ЖЭО',
    `RESERV1` VARCHAR(100) COMMENT 'Резерв',
    `RESERV2` VARCHAR(100) COMMENT 'Резерв',
    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),   
    KEY `key_OWN_NUM_SR` (`OWN_NUM_SR`),
    CONSTRAINT `fk_benefit__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Льготы';

-- ------------------------------
-- Tarif
-- ------------------------------
DROP TABLE IF EXISTS `tarif`;

CREATE TABLE `tarif` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор тарифа',
    `request_file_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор файла тарифов',
    `status` INTEGER NULL COMMENT 'Код статуса. См. таблицу status_description и класс RequestStatus',

    `T11_DATA_T` VARCHAR(100) COMMENT '',
    `T11_DATA_E` VARCHAR(100) COMMENT '',
    `T11_DATA_R` VARCHAR(100) COMMENT '',
    `T11_MARK` VARCHAR(100) COMMENT '',
    `T11_TARN` VARCHAR(100) COMMENT '',
    `T11_CODE1` VARCHAR(100) COMMENT '',
    `T11_CODE2` VARCHAR(100) COMMENT '',
    `T11_COD_NA` VARCHAR(100) COMMENT '',
    `T11_CODE3` VARCHAR(100) COMMENT '',
    `T11_NORM_U` VARCHAR(100) COMMENT '',
    `T11_NOR_US` VARCHAR(100) COMMENT '',
    `T11_CODE_N` VARCHAR(100) COMMENT '',
    `T11_COD_ND` VARCHAR(100) COMMENT '',
    `T11_CD_UNI` VARCHAR(100) COMMENT '',
    `T11_CS_UNI` VARCHAR(100) COMMENT '',
    `T11_NORM` VARCHAR(100) COMMENT '',
    `T11_NRM_DO` VARCHAR(100) COMMENT '',
    `T11_NRM_MA` VARCHAR(100) COMMENT '',
    `T11_K_NADL` VARCHAR(100) COMMENT '',
     PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Тарифы';

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

    `SUR_NAM` VARCHAR(100) COMMENT 'Фамилия',
    `F_NAM` VARCHAR(100) COMMENT 'Имя',
    `M_NAM` VARCHAR(100) COMMENT 'Отчество',
    `INDX` VARCHAR(100) COMMENT 'Индекс почтового отделения',
    `N_NAME` VARCHAR(100) COMMENT 'Название населенного пункта',
    `N_CODE` VARCHAR(100) COMMENT '',
    `VUL_CAT` VARCHAR(100) COMMENT 'Тип улицы',
    `VUL_NAME` VARCHAR(100) COMMENT 'Название улицы',
    `VUL_CODE` VARCHAR(100) COMMENT 'Код улицы',
    `BLD_NUM` VARCHAR(100) COMMENT 'Номер дома',
    `CORP_NUM` VARCHAR(100) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(100) COMMENT 'Номер квартиры',
    `OWN_NUM` VARCHAR (100) COMMENT 'Номер дела',
    `APP_NUM` VARCHAR(100) COMMENT '',
    `DAT_BEG` VARCHAR(100) COMMENT '',
    `DAT_END` VARCHAR(100) COMMENT '',
    `CM_AREA` VARCHAR(100) COMMENT '',
    `NM_AREA` VARCHAR(100) COMMENT '',
    `BLC_AREA` VARCHAR(100) COMMENT '',
    `FROG` VARCHAR(100) COMMENT '',
    `DEBT` VARCHAR(100) COMMENT '',
    `NUMB` VARCHAR(100) COMMENT '',
    `P1` VARCHAR(100) COMMENT 'фактическое начисление',
    `N1` VARCHAR(100) COMMENT 'фактический тариф',
    `P2` VARCHAR(100) COMMENT '',
    `N2` VARCHAR(100) COMMENT '',
    `P3` VARCHAR(100) COMMENT '',
    `N3` VARCHAR(100) COMMENT '',
    `P4` VARCHAR(100) COMMENT '',
    `N4` VARCHAR(100) COMMENT '',
    `P5` VARCHAR(100) COMMENT '',
    `N5` VARCHAR(100) COMMENT '',
    `P6` VARCHAR(100) COMMENT '',
    `N6` VARCHAR(100) COMMENT '',
    `P7` VARCHAR(100) COMMENT '',
    `N7` VARCHAR(100) COMMENT '',
    `P8` VARCHAR(100) COMMENT '',
    `N8` VARCHAR(100) COMMENT '',

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
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Фактические начисления';

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

    `FIO` VARCHAR(100) COMMENT 'ФИО',
    `first_name` VARCHAR(100) COMMENT 'Имя',
    `last_name` VARCHAR(100) COMMENT 'Фамилия',
    `middle_name` VARCHAR(100) COMMENT 'Отчество',
    `ID_RAJ` VARCHAR(100) COMMENT 'Код района',
    `NP_CODE` VARCHAR(100) COMMENT 'Код населенного пункта',
    `NP_NAME`VARCHAR(100) COMMENT 'Название населенного пункта',
    `CAT_V` VARCHAR(100) COMMENT 'Тип улицы',
    `VULCOD` VARCHAR(100) COMMENT 'Код улицы',
    `NAME_V` VARCHAR(100) COMMENT 'Название улицы',
    `BLD` VARCHAR(100) COMMENT 'Номер дома',
    `CORP` VARCHAR(100) COMMENT 'Номер корпуса',
    `FLAT` VARCHAR(100) COMMENT 'Номер квартиры',
    `RASH` VARCHAR(100) COMMENT 'Номер л/с ПУ',
    `NUMB` VARCHAR(100) COMMENT '',
    `DAT1` VARCHAR(100) COMMENT 'Дата начала периода, на который предоставляется субсидия',
    `DAT2` VARCHAR(100) COMMENT 'Дата конца периода, на который предоставляется субсидия',
    `NM_PAY` VARCHAR(100) COMMENT 'Начисление в пределах нормы',
    
    `P1` VARCHAR(100) COMMENT '',
    `P2` VARCHAR(100) COMMENT '',
    `P3` VARCHAR(100) COMMENT '',
    `P4` VARCHAR(100) COMMENT '',
    `P5` VARCHAR(100) COMMENT '',
    `P6` VARCHAR(100) COMMENT '',
    `P7` VARCHAR(100) COMMENT '',
    `P8` VARCHAR(100) COMMENT '',

    `SM1` VARCHAR(100) COMMENT '',
    `SM2` VARCHAR(100) COMMENT '',
    `SM3` VARCHAR(100) COMMENT '',
    `SM4` VARCHAR(100) COMMENT '',
    `SM5` VARCHAR(100) COMMENT '',
    `SM6` VARCHAR(100) COMMENT '',
    `SM7` VARCHAR(100) COMMENT '',
    `SM8` VARCHAR(100) COMMENT '',

    `SB1` VARCHAR(100) COMMENT '',
    `SB2` VARCHAR(100) COMMENT '',
    `SB3` VARCHAR(100) COMMENT '',
    `SB4` VARCHAR(100) COMMENT '',
    `SB5` VARCHAR(100) COMMENT '',
    `SB6` VARCHAR(100) COMMENT '',
    `SB7` VARCHAR(100) COMMENT '',
    `SB8` VARCHAR(100) COMMENT '',

    `OB1` VARCHAR(100) COMMENT '',
    `OB2` VARCHAR(100) COMMENT '',
    `OB3` VARCHAR(100) COMMENT '',
    `OB4` VARCHAR(100) COMMENT '',
    `OB5` VARCHAR(100) COMMENT '',
    `OB6` VARCHAR(100) COMMENT '',
    `OB7` VARCHAR(100) COMMENT '',
    `OB8` VARCHAR(100) COMMENT '',

    `SUMMA` VARCHAR(100) COMMENT '',
    `NUMM` VARCHAR(100) COMMENT '',
    `SUBS` VARCHAR(100) COMMENT '',
    `KVT` VARCHAR(100) COMMENT '',

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
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Файлы субсидий';

-- 5. Copy all data from temporal tables to new data tables converting data to string representation.
INSERT INTO `payment` 
	(`id`, 
	`request_file_id`, 
	`account_number`, 
	`internal_city_id`, 
	`internal_street_id`, 
	`internal_street_type_id`, 
	`internal_building_id`, 
	`internal_apartment_id`, 
	`outgoing_city`, 
	`outgoing_district`, 
	`outgoing_street`, 
	`outgoing_street_type`, 
	`outgoing_building_number`, 
	`outgoing_building_corp`, 
	`outgoing_apartment`, 
	`status`, 
	OWN_NUM, 
	REE_NUM, 
	OPP, 
	NUMB, 
	MARK, 
	`CODE`, 
	ENT_COD, 
	FROG, 
	FL_PAY, 
	NM_PAY, 
	DEBT, 
	CODE2_1, 
	CODE2_2, 
	CODE2_3, 
	CODE2_4, 
	CODE2_5, 
	CODE2_6, 
	CODE2_7, 
	CODE2_8, 
	NORM_F_1, 
	NORM_F_2, 
	NORM_F_3, 
	NORM_F_4, 
	NORM_F_5, 
	NORM_F_6, 
	NORM_F_7, 
	NORM_F_8, 
	OWN_NUM_SR, 
	DAT1, 
	DAT2, 
	OZN_PRZ, 
	DAT_F_1, 
	DAT_F_2, 
	DAT_FOP_1, 
	DAT_FOP_2, 
	ID_RAJ, 
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	IND_COD, 
	INDX, 
	N_NAME, 
	VUL_NAME, 
	BLD_NUM, 
	CORP_NUM, 
	FLAT, 
	CODE3_1, 
	CODE3_2, 
	CODE3_3, 
	CODE3_4, 
	CODE3_5, 
	CODE3_6, 
	CODE3_7, 
	CODE3_8, 
	OPP_SERV, 
	RESERV1, 
	RESERV2) 
	SELECT 
	id, 
	request_file_id, 
	account_number, 
	internal_city_id, 
	internal_street_id, 
	internal_street_type_id, 
	internal_building_id, 
	internal_apartment_id, 
	outgoing_city, 
	outgoing_district, 
	outgoing_street, 
	outgoing_street_type, 
	outgoing_building_number, 
	outgoing_building_corp, 
	outgoing_apartment, 
	`status`, 
	OWN_NUM, 
	CONVERT(REE_NUM, CHAR), 
	OPP, 
	CONVERT(NUMB, CHAR), 
	CONVERT(MARK, CHAR),
	CONVERT(`CODE`, CHAR), 
	CONVERT(ENT_COD, CHAR),
	CONVERT(FROG, CHAR),
	CONVERT(FL_PAY, CHAR),
	CONVERT(NM_PAY, CHAR),
	CONVERT(DEBT, CHAR),
	CONVERT(CODE2_1, CHAR),
	CONVERT(CODE2_2, CHAR), 
	CONVERT(CODE2_3, CHAR), 
	CONVERT(CODE2_4, CHAR), 
	CONVERT(CODE2_5, CHAR), 
	CONVERT(CODE2_6, CHAR), 
	CONVERT(CODE2_7, CHAR), 
	CONVERT(CODE2_8, CHAR), 
	CONVERT(NORM_F_1, CHAR), 
	CONVERT(NORM_F_2, CHAR), 
	CONVERT(NORM_F_3, CHAR), 
	CONVERT(NORM_F_4, CHAR), 
	CONVERT(NORM_F_5, CHAR), 
	CONVERT(NORM_F_6, CHAR), 
	CONVERT(NORM_F_7, CHAR), 
	CONVERT(NORM_F_8, CHAR),
	OWN_NUM_SR, 
	DATE_FORMAT(DAT1, '%d.%m.%Y'), 
	DATE_FORMAT(DAT2, '%d.%m.%Y'), 
	CONVERT(OZN_PRZ, CHAR),
	DATE_FORMAT(DAT_F_1, '%d.%m.%Y'),
	DATE_FORMAT(DAT_F_2, '%d.%m.%Y'), 
	DATE_FORMAT(DAT_FOP_1, '%d.%m.%Y'), 
	DATE_FORMAT(DAT_FOP_2, '%d.%m.%Y'), 
	ID_RAJ, 
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	IND_COD, 
	INDX, 
	N_NAME, 
	VUL_NAME, 
	BLD_NUM, 
	CORP_NUM, 
	FLAT, 
	CONVERT(CODE3_1, CHAR),
	CONVERT(CODE3_2, CHAR), 
	CONVERT(CODE3_3, CHAR), 
	CONVERT(CODE3_4, CHAR), 
	CONVERT(CODE3_5, CHAR), 
	CONVERT(CODE3_6, CHAR), 
	CONVERT(CODE3_7, CHAR), 
	CONVERT(CODE3_8, CHAR), 
	OPP_SERV, 
	CONVERT(RESERV1, CHAR),
	RESERV2
	FROM `temp_payment`;
	
INSERT INTO `benefit` 
	(id, 
	request_file_id, 
	account_number, 
	`status`, 
	OWN_NUM, 
	REE_NUM, 
	OWN_NUM_SR, 
	FAM_NUM, 
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	IND_COD, 
	PSP_SER, 
	PSP_NUM, 
	OZN, 
	CM_AREA, 
	HEAT_AREA, 
	OWN_FRM, 
	HOSTEL, 
	PRIV_CAT, 
	ORD_FAM, 
	OZN_SQ_ADD, 
	OZN_ABS, 
	RESERV1, 
	RESERV2)
	SELECT 
	id, 
	request_file_id, 
	account_number, 
	`status`, 
	OWN_NUM, 
	CONVERT(REE_NUM, CHAR),
	OWN_NUM_SR, 
	CONVERT(FAM_NUM, CHAR),
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	IND_COD, 
	PSP_SER, 
	PSP_NUM, 
	CONVERT(OZN, CHAR),
	CONVERT(CM_AREA, CHAR),
	CONVERT(HEAT_AREA, CHAR),
	CONVERT(OWN_FRM, CHAR),
	CONVERT(HOSTEL, CHAR),
	CONVERT(PRIV_CAT, CHAR),
	CONVERT(ORD_FAM, CHAR),
	CONVERT(OZN_SQ_ADD, CHAR),
	CONVERT(OZN_ABS, CHAR),
	CONVERT(RESERV1, CHAR),
	RESERV2
	FROM `temp_benefit`;

INSERT INTO `actual_payment` 
	(id, 
	request_file_id, 
	account_number, 
	internal_city_id, 
	internal_street_id, 
	internal_street_type_id, 
	internal_building_id, 
	outgoing_city, 
	outgoing_district, 
	outgoing_street, 
	outgoing_street_type, 
	outgoing_building_number, 
	outgoing_building_corp, 
	outgoing_apartment, 
	`status`, 
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	INDX, 
	N_NAME, 
	N_CODE, 
	VUL_CAT, 
	VUL_NAME, 
	VUL_CODE, 
	BLD_NUM, 
	CORP_NUM, 
	FLAT, 
	OWN_NUM, 
	APP_NUM, 
	DAT_BEG, 
	DAT_END, 
	CM_AREA, 
	NM_AREA, 
	BLC_AREA, 
	FROG, 
	DEBT, 
	NUMB, 
	P1, 
	N1, 
	P2, 
	N2, 
	P3, 
	N3, 
	P4, 
	N4, 
	P5, 
	N5, 
	P6, 
	N6, 
	P7, 
	N7, 
	P8, 
	N8)
	SELECT 
	id, 
	request_file_id, 
	account_number, 
	internal_city_id, 
	internal_street_id, 
	internal_street_type_id, 
	internal_building_id, 
	outgoing_city, 
	outgoing_district, 
	outgoing_street, 
	outgoing_street_type, 
	outgoing_building_number, 
	outgoing_building_corp, 
	outgoing_apartment, 
	`status`, 
	SUR_NAM, 
	F_NAM, 
	M_NAM, 
	INDX, 
	N_NAME, 
	N_CODE, 
	VUL_CAT, 
	VUL_NAME, 
	VUL_CODE, 
	BLD_NUM, 
	CORP_NUM, 
	FLAT, 
	OWN_NUM, 
	APP_NUM, 
	DATE_FORMAT(DAT_BEG, '%d.%m.%Y'),
	DATE_FORMAT(DAT_END, '%d.%m.%Y'),
	CONVERT(CM_AREA, CHAR),
	CONVERT(NM_AREA, CHAR),
	CONVERT(BLC_AREA, CHAR),
	CONVERT(FROG, CHAR),
	CONVERT(DEBT, CHAR),
	CONVERT(NUMB, CHAR),
	CONVERT(P1, CHAR),
	CONVERT(N1, CHAR),
	CONVERT(P2, CHAR),
	CONVERT(N2, CHAR),
	CONVERT(P3, CHAR),
	CONVERT(N3, CHAR),
	CONVERT(P4, CHAR),
	CONVERT(N4, CHAR),
	CONVERT(P5, CHAR),
	CONVERT(N5, CHAR),
	CONVERT(P6, CHAR),
	CONVERT(N6, CHAR),
	CONVERT(P7, CHAR),
	CONVERT(N7, CHAR),
	CONVERT(P8, CHAR),
	CONVERT(N8, CHAR)
	FROM `temp_actual_payment`;
	
INSERT INTO `subsidy` 
	(id, 
	request_file_id, 
	account_number, 
	internal_city_id, 
	internal_street_id, 
	internal_street_type_id, 
	internal_building_id, 
	outgoing_city, 
	outgoing_district, 
	outgoing_street, 
	outgoing_street_type, 
	outgoing_building_number, 
	outgoing_building_corp, 
	outgoing_apartment, 
	`status`, 
	FIO, 
	first_name, 
	last_name, 
	middle_name, 
	ID_RAJ, 
	NP_CODE, 
	NP_NAME, 
	CAT_V, 
	VULCOD, 
	NAME_V, 
	BLD, 
	CORP, 
	FLAT, 
	RASH, 
	NUMB, 
	DAT1, 
	DAT2, 
	NM_PAY, 
	P1, 
	P2, 
	P3, 
	P4, 
	P5, 
	P6, 
	P7, 
	P8, 
	SM1, 
	SM2, 
	SM3, 
	SM4, 
	SM5, 
	SM6, 
	SM7, 
	SM8, 
	SB1, 
	SB2, 
	SB3, 
	SB4, 
	SB5, 
	SB6, 
	SB7, 
	SB8, 
	OB1, 
	OB2, 
	OB3, 
	OB4, 
	OB5, 
	OB6, 
	OB7, 
	OB8, 
	SUMMA, 
	NUMM, 
	SUBS, 
	KVT)
	SELECT 
	id, 
	request_file_id, 
	account_number, 
	internal_city_id, 
	internal_street_id, 
	internal_street_type_id, 
	internal_building_id, 
	outgoing_city, 
	outgoing_district, 
	outgoing_street, 
	outgoing_street_type, 
	outgoing_building_number, 
	outgoing_building_corp, 
	outgoing_apartment, 
	`status`, 
	FIO, 
	first_name, 
	last_name, 
	middle_name, 
	ID_RAJ, 
	NP_CODE, 
	NP_NAME, 
	CAT_V, 
	VULCOD, 
	NAME_V, 
	BLD, 
	CORP, 
	FLAT, 
	RASH, 
	NUMB, 
	DATE_FORMAT(DAT1, '%d.%m.%Y'),
	DATE_FORMAT(DAT2, '%d.%m.%Y'),
	CONVERT(NM_PAY, CHAR),
	CONVERT(P1, CHAR),
	CONVERT(P2, CHAR),
	CONVERT(P3, CHAR),
	CONVERT(P4, CHAR),
	CONVERT(P5, CHAR),
	CONVERT(P6, CHAR),
	CONVERT(P7, CHAR),
	CONVERT(P8, CHAR),
	CONVERT(SM1, CHAR),
	CONVERT(SM2, CHAR),
	CONVERT(SM3, CHAR),
	CONVERT(SM4, CHAR),
	CONVERT(SM5, CHAR),
	CONVERT(SM6, CHAR),
	CONVERT(SM7, CHAR),
	CONVERT(SM8, CHAR),
	CONVERT(SB1, CHAR),
	CONVERT(SB2, CHAR),
	CONVERT(SB3, CHAR),
	CONVERT(SB4, CHAR),
	CONVERT(SB5, CHAR),
	CONVERT(SB6, CHAR),
	CONVERT(SB7, CHAR),
	CONVERT(SB8, CHAR),
	CONVERT(OB1, CHAR),
	CONVERT(OB2, CHAR),
	CONVERT(OB3, CHAR),
	CONVERT(OB4, CHAR),
	CONVERT(OB5, CHAR),
	CONVERT(OB6, CHAR),
	CONVERT(OB7, CHAR),
	CONVERT(OB8, CHAR),
	CONVERT(SUMMA, CHAR),
	CONVERT(NUMM, CHAR),
	CONVERT(SUBS, CHAR),
	CONVERT(KVT, CHAR)
	FROM `temp_subsidy`;
	
INSERT INTO `tarif` 
	(id, 
	request_file_id, 
	`status`, 
	T11_DATA_T, 
	T11_DATA_E, 
	T11_DATA_R, 
	T11_MARK, 
	T11_TARN, 
	T11_CODE1, 
	T11_CODE2, 
	T11_COD_NA, 
	T11_CODE3, 
	T11_NORM_U, 
	T11_NOR_US, 
	T11_CODE_N, 
	T11_COD_ND, 
	T11_CD_UNI, 
	T11_CS_UNI, 
	T11_NORM, 
	T11_NRM_DO, 
	T11_NRM_MA, 
	T11_K_NADL)
	SELECT 
	id, 
	request_file_id, 
	`status`, 
	T11_DATA_T, 
	T11_DATA_E, 
	T11_DATA_R, 
	CONVERT(T11_MARK, CHAR),
	CONVERT(T11_TARN, CHAR),
	CONVERT(T11_CODE1, CHAR),
	CONVERT(T11_CODE2, CHAR),
	T11_COD_NA, 
	CONVERT(T11_CODE3, CHAR),
	CONVERT(T11_NORM_U, CHAR),
	CONVERT(T11_NOR_US, CHAR),
	CONVERT(T11_CODE_N, CHAR),
	CONVERT(T11_COD_ND, CHAR),
	CONVERT(T11_CD_UNI, CHAR),
	CONVERT(T11_CS_UNI, CHAR),
	CONVERT(T11_NORM, CHAR),
	CONVERT(T11_NRM_DO, CHAR),
	CONVERT(T11_NRM_MA, CHAR),
	CONVERT(T11_K_NADL, CHAR)
	FROM `temp_tarif`;

-- 6. Remove temporal tables.
DROP TABLE IF EXISTS `temp_payment`;
DROP TABLE IF EXISTS `temp_benefit`;
DROP TABLE IF EXISTS `temp_actual_payment`;
DROP TABLE IF EXISTS `temp_subsidy`;
DROP TABLE IF EXISTS `temp_tarif`;

-- 7. Add request file description tables.

-- ------------------------------
-- Request file description
-- ------------------------------
DROP TABLE IF EXISTS `request_file_description`;

CREATE TABLE `request_file_description` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `request_file_type` VARCHAR(50) NOT NULL COMMENT 'Тип файла запроса',
  `date_pattern` VARCHAR(50) NOT NULL COMMENT 'Шаблон значений типа дата',
  PRIMARY KEY (`id`),
  KEY `key_request_file_type` (`request_file_type`)
) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Описание структуры файлов запросов';

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
) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Описание структуры поля файла запросов';

INSERT INTO `update` (`version`) VALUE ('20120420_757_0.1.28');