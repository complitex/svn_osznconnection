-- insert servicing organization
INSERT INTO `organization_type`(`object_id`) VALUES (4);
INSERT INTO `organization_type_string_culture`(`id`, `locale_id`, `value`)
VALUES (4, 1, UPPER('ОБСЛУЖИВАЮЩАЯ ОРГАНИЗАЦИЯ')), (4, 2, UPPER('ОБСЛУЖИВАЮЩАЯ ОРГАНИЗАЦИЯ'));
INSERT INTO `organization_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1, 4, 2300, 4, 2300);

-- create organization import table
CREATE TABLE `organization_import` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'ID организации',
  `code` VARCHAR(100) NOT NULL COMMENT 'Код организации',
  `short_name` VARCHAR(100) NOT NULL COMMENT 'Короткое название организации',
  `full_name` VARCHAR(500) NOT NULL COMMENT 'Полное название организации',
  `hlevel` BIGINT(20) COMMENT 'Ссылка на вышестоящую организацию',
  PRIMARY KEY (`pk_id`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_hlevel` (`hlevel`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Вспомогательная таблица для импорта организаций';

-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20131126_878_0.2.7');