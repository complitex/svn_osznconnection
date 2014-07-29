-- ------------------------------
--  Address Sync
-- ------------------------------

DROP TABLE IF EXISTS `address_sync`;
CREATE TABLE `address_sync`(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор адресного элемента синхронизации',
  `object_id` BIGINT(20) COMMENT 'Идентификатор адресного объекта',
  `parent_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор родительского адресного объекта',
  `external_id` VARCHAR(20) NOT NULL COMMENT 'Код адресного объекта (ID)',
  `additional_external_id` VARCHAR(20) COMMENT 'Код адресного объекта (ID)',
  `name` VARCHAR(100) NOT NULL COMMENT 'Название адресного элемента',
  `additional_name` VARCHAR(20) COMMENT 'Дополнительное название адресного элемента',
  `type` INTEGER NOT NULL COMMENT 'Тип адресного элемента синхронизации',
  `status` INTEGER NOT NULL COMMENT 'Статус синхронизации',
  `date` DATETIME NOT NULL COMMENT 'Дата актуальности',
  PRIMARY KEY (`id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_object_id` (`parent_object_id`),
  KEY `key_external_id` (`external_id`),
  KEY `key_additional_external_id` (`additional_external_id`),
  KEY `key_name` (`name`),
  KEY `key_additional_name` (`additional_name`),
  KEY `key_type` (`type`),
  KEY `key_status` (`status`),
  KEY `key_date` (`date`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Синхронизация адресного элемента';

-- ------------------------------
--  DROP Sync
-- ------------------------------

DROP TABLE IF EXISTS `district_sync`;
DROP TABLE IF EXISTS `street_type_sync`;
DROP TABLE IF EXISTS `street_sync`;
DROP TABLE IF EXISTS `building_address_sync`;

-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140729_1016_0.3.4');