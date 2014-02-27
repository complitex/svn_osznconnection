-- Script adds street type correction support.

ALTER TABLE `street_correction` DROP KEY `uk_street_correction`, ADD COLUMN `street_type_correction_id` BIGINT(20) NULL AFTER `correction`,
ADD UNIQUE KEY `uk_street_correction` (`parent_id`, `object_id`, `correction`, `organization_id`, `internal_organization_id`, `street_type_correction_id`),
ADD KEY `key_street_type_correction_id` (`street_type_correction_id`), 
ADD CONSTRAINT `fk_street_correction__street_type_correctionn` FOREIGN KEY (`street_type_correction_id`) REFERENCES `street_type_correction` (`id`);

INSERT INTO `update` (`version`) VALUE ('20101227_486');

