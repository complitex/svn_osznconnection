CREATE TABLE `update` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` VARCHAR(64) NOT NULL,
    `date` TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `update` (`version`) value ('20101020_300');

ALTER TABLE `city_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;

ALTER TABLE `district_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `district_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `district_correction` ADD CONSTRAINT `fk_district_correction__city_correction` FOREIGN KEY (`parent_id`) REFERENCES `city_correction` (`id`);

ALTER TABLE `street_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `street_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `street_correction` ADD CONSTRAINT `fk_street_correction__district_correction` FOREIGN KEY (`parent_id`) REFERENCES `district_correction` (`id`);

ALTER TABLE `building_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `building_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `building_correction` ADD CONSTRAINT `fk_building_correction__street_correction` FOREIGN KEY (`parent_id`) REFERENCES `street_correction` (`id`);

ALTER TABLE `ownership_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `privilege_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;