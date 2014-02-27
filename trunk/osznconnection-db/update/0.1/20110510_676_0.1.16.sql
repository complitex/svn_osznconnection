-- Adds `pu_account_number` column.

DELETE FROM `person_account`;
ALTER TABLE `person_account` ADD COLUMN `pu_account_number` VARCHAR(100) NOT NULL,
	DROP KEY `uk_person_account`, ADD UNIQUE KEY
        `uk_person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street_type`, `street`, `building_num`,
        `building_corp`, `apartment`, `oszn_id`, `calc_center_id`, `pu_account_number`);

INSERT INTO `update` (`version`) VALUE ('20110510_676_0.1.16');