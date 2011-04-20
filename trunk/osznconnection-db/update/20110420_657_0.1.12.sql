-- Removes all records except with null street type, drops `street_code` column, 
-- makes `own_num_sr` and `street_type` columns not nullable by default set to '', returns `own_num_sr` to unique key.

DELETE FROM `person_account` WHERE `street_type` IS NULL;
UPDATE `person_account` SET `own_num_sr` = '' WHERE `own_num_sr` IS NULL;
ALTER TABLE `person_account` DROP COLUMN `street_code`, 
	MODIFY COLUMN `street_type` VARCHAR(50) NOT NULL DEFAULT '',
	MODIFY COLUMN `own_num_sr` VARCHAR(50) NOT NULL DEFAULT '', 
	DROP KEY `uk_person_account`, ADD 
	UNIQUE KEY `uk_person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street_type`, `street`, `building_num`,
        `building_corp`, `apartment`, `own_num_sr`, `oszn_id`, `calc_center_id`);

INSERT INTO `update` (`version`) VALUE ('20110420_657_0.1.12');