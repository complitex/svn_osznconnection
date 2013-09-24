-- Remove street_type_correction from street_correction

ALTER TABLE `street_correction` DROP FOREIGN KEY `fk_street_correction__street_type_correction`,
    DROP COLUMN `street_type_correction_id`;


-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20130924_856_0.2.4');