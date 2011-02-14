-- --------------------------------------------------------------------------------------------------
-- 'dev' keyword denote sql update script is in development process. Do not execute on real database.
-- --------------------------------------------------------------------------------------------------

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
  KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Middle Name
-- ------------------------------
CREATE TABLE `middle_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Last Name
-- ------------------------------
CREATE TABLE `last_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- uncomment and update on release
-- INSERT INTO `update` (`version`) VALUES ('20110207_575_0.1.3');