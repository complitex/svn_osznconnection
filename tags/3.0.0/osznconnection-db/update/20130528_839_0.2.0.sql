-- Merge request file month and year column to begin_date and end_date

ALTER TABLE `request_file` ADD COLUMN `begin_date` DATE NOT NULL COMMENT 'Дата начала';
ALTER TABLE `request_file` ADD COLUMN `end_date` DATE NULL COMMENT 'Дата окончания';

ALTER TABLE `request_file` ADD KEY `key_begin_date` (`begin_date`), ADD KEY `key_end_date` (`end_date`) ;

UPDATE `request_file` SET `begin_date` = DATE(CONCAT(`year`, '-', `month` , '-01'));

ALTER TABLE `request_file` DROP KEY `request_file_unique_id`;
ALTER TABLE `request_file` DROP COLUMN `year`, DROP COLUMN `month`;

ALTER TABLE `request_file` ADD UNIQUE KEY `request_file_unique_id` (`name`, `organization_id`, `user_organization_id`, `registry`, `begin_date`, `end_date`);

INSERT INTO `update` (`version`) VALUE ('20130528_839_0.2.0');