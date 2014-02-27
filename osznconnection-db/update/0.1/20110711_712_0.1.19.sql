-- Removes entity types.

ALTER TABLE `apartment` DROP FOREIGN KEY `fk_apartment__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `room` DROP FOREIGN KEY `fk_room__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `street` DROP FOREIGN KEY `fk_street__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `street_type` DROP FOREIGN KEY `fk_street_type__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `city` DROP FOREIGN KEY `fk_city__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `city_type` DROP FOREIGN KEY `fk_city_type__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `building_address` DROP FOREIGN KEY `fk_building_address__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `building` DROP FOREIGN KEY `fk_building__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `district` DROP FOREIGN KEY `fk_district__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `region` DROP FOREIGN KEY `fk_region__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `country` DROP FOREIGN KEY `fk_country__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `organization_type` DROP FOREIGN KEY `fk_organization_type__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `organization` DROP FOREIGN KEY `fk_organization__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `user_info` DROP FOREIGN KEY `fk_user_info__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `ownership` DROP FOREIGN KEY `fk_ownership__entity_type`, DROP COLUMN `entity_type_id`;
ALTER TABLE `privilege` DROP FOREIGN KEY `fk_privilege__entity_type`, DROP COLUMN `entity_type_id`;

DROP TABLE `entity_type`;

INSERT INTO `update` (`version`) VALUE ('20110711_712_0.1.19');

