-- Script deletes out of date entity_type_correction, apartment_correction tables and corrects unique keys for correction tables.
-- Also correction_corp column was made NOT NULL with empty corp for null.
-- New statuses have been added to reflect situation when more one correction or internal object could be found.

DROP TABLE `entity_type_correction`;
DROP TABLE `apartment_correction`;

ALTER TABLE `city_correction` DROP KEY `uk_city_correction`, 
ADD UNIQUE KEY `uk_city_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`);

ALTER TABLE `city_type_correction` ADD UNIQUE KEY `uk_city_type_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`), DROP KEY `key_parent_id`;

ALTER TABLE `street_type_correction` ADD UNIQUE KEY `uk_street_type_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`), DROP KEY `key_parent_id`;

ALTER TABLE `ownership_correction` DROP KEY `uk_ownership_correction`, 
ADD UNIQUE KEY `uk_ownership_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`, `organization_code`);

ALTER TABLE `privilege_correction` DROP KEY `uk_privilege_correction`, 
ADD UNIQUE KEY `uk_privilege_correction` (`object_id`, `correction`, `organization_id`, `internal_organization_id`, `organization_code`);

UPDATE `building_correction` SET `correction_corp` = '' WHERE `correction_corp` IS NULL;
ALTER TABLE `building_correction` MODIFY COLUMN `correction_corp` VARCHAR(20) NOT NULL DEFAULT '';

INSERT INTO `status_description`(`code`, `name`) VALUES (234,'Найдено более одного населенного пункта в адресной базе'), 
(235,'Найдено более одной улицы в адресной базе'), (236,'Найдено более одного дома в адресной базе'), 
(210,'Найдено более одного соответствия для населенного пункта'), (211,'Найдено более одного соответствия для улицы'), 
(228,'Найдено более одного соответствия для дома'), (229, 'Более одного населенного пункта найдено в соответствиях МН'), 
(230,'Более одного района найдено в соответствиях МН'), (231,'Более одного типа улицы найдено в соответствиях МН'), 
(232,'Более одной улицы найдено в соответствиях МН'), (233,'Более одного дома найдено в соответствиях МН');

INSERT INTO `update` (`version`) VALUE ('20101228_487');

