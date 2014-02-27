-- Discards `own_num_sr` column.

DELETE FROM `person_account` WHERE `street_type` = ''; 
ALTER TABLE `person_account` DROP COLUMN `own_num_sr`;

INSERT INTO `update` (`version`) VALUE ('20110421_660_0.1.13');