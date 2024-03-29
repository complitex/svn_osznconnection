ALTER TABLE `user_info` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `privilege` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `ownership` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `country` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `organization` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `region` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `district` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `building` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `building_address` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `city_type` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `city` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `street_type` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `street` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `room` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);
ALTER TABLE `apartment` ADD COLUMN `external_id` BIGINT(20), ADD UNIQUE KEY `unique_external_id` (`external_id`);

INSERT INTO `update` (`version`) VALUE ('20110222_592_0.1.5');