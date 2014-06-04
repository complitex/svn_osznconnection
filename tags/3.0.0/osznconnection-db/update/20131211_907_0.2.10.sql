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

-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20131211_907_0.2.10');