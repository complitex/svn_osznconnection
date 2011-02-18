ALTER TABLE `request_file_group` DROP COLUMN `permission_id`,
DROP KEY `key_permission_id`,
DROP FOREIGN KEY `fk_request_file_group__permission`;

ALTER TABLE `request_file` DROP COLUMN `permission_id`,
DROP KEY `key_permission_id`,
DROP FOREIGN KEY `fk_request_file__permission`;

-- ------------------------------
-- First Name
-- ------------------------------
CREATE TABLE `first_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Middle Name
-- ------------------------------
CREATE TABLE `middle_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Last Name
-- ------------------------------
CREATE TABLE `last_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Update Entity description
-- ------------------------------

UPDATE `entity_attribute_value_type` set `attribute_value_type` = 'last_name' where `id` = 1000;
UPDATE `entity_attribute_value_type` set `attribute_value_type` = 'first_name' where `id` = 1001;
UPDATE `entity_attribute_value_type` set `attribute_value_type` = 'middle_name' where `id` = 1002;

-- ------------------------------
-- Clear User Info
-- ------------------------------

DELETE FROM `user_info_string_culture`;
DELETE FROM `user_info_attribute`;

INSERT INTO `update` (`version`) VALUES ('20110207_554_0.1.3');