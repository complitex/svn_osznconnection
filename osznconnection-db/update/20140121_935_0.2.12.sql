-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140121_935_0.2.12');

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

