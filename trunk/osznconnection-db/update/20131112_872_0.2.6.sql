-- ------------------------------
-- Building Code Attribute
-- ------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (503, 1, UPPER('Список кодов дома')), (503, 2, UPPER('Список кодов дома'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (502, 500, 0, 503, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (502, 502, 'building_organization_association');

-- ------------------------------
-- Building Code
-- ------------------------------

CREATE TABLE `building_code` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'ID обслуживающей организации',
  `code` INTEGER NOT NULL COMMENT 'Код дома для данной обслуживающей организации',
  `building_id` BIGINT(20) NOT NULL COMMENT 'ID дома',
  PRIMARY KEY (`id`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_building_id` (`building_id`),
  CONSTRAINT `fk_building_code__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_building_code__building` FOREIGN KEY (`building_id`) REFERENCES `building` (`object_id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Код дома';

-- ------------------------------
-- Building Import
-- ------------------------------

CREATE TABLE `building_import` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `distr_id` BIGINT(20) NOT NULL COMMENT 'ID района',
  `street_id` BIGINT(20) NOT NULL COMMENT 'ID улицы',
  `num` VARCHAR(10) NOT NULL COMMENT 'Номер дома',
  `part` VARCHAR(10) NOT NULL COMMENT 'Корпус дома',
  `processed` TINYINT(1) NOT NULL default 0 COMMENT 'Индикатор импорта',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_import` (`street_id`, `num`, `part`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Вспомогательная таблица для импорта домов';

CREATE TABLE `building_segment_import` (
  `id` BIGINT(20) NOT NULL COMMENT 'ID части дома',
  `gek` BIGINT(20) COMMENT 'ID организации',
  `code` VARCHAR(10) COMMENT 'Код дома',
  `building_import_id` BIGINT(20) NOT NULL COMMENT 'Ссылка на building_import запись',
  PRIMARY KEY (`id`),
  KEY `key_building_import_id` (`building_import_id`),
  CONSTRAINT `fk_building_segment_import__building_import` FOREIGN KEY (`building_import_id`) REFERENCES `building_import` (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Вспомогательная таблица для импорта домов';

-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20131112_872_0.2.6');