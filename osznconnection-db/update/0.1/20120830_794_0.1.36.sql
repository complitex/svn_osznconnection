-- Renames BLILD field to BUILD for dwelling characteristics and facility service types.

SET GLOBAL log_bin_trust_function_creators = 1;

ALTER TABLE `dwelling_characteristics` DROP KEY `key_BLILD`, CHANGE COLUMN `BLILD` `BUILD` VARCHAR(100) COMMENT 'Корпус',
	ADD KEY `key_BUILD` (`BUILD`);
	
ALTER TABLE `facility_service_type` DROP KEY `key_BLILD`, CHANGE COLUMN `BLILD` `BUILD` VARCHAR(100) COMMENT 'Корпус',
	ADD KEY `key_BUILD` (`BUILD`);
	
UPDATE `request_file_field_description`	SET `name` = 'BUILD' WHERE `name` = 'BLILD' AND 
	`request_file_description_id` IN (SELECT d.`id` FROM `request_file_description` d WHERE 
			d.`request_file_type` IN ('DWELLING_CHARACTERISTICS', 'FACILITY_SERVICE_TYPE'));

INSERT INTO `update` (`version`) VALUE ('20120830_794_0.1.36');

SET GLOBAL log_bin_trust_function_creators = 0;