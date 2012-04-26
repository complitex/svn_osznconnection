-- Converts all oszn-to-system objects corrections to upper case.
  
-- Create temp table containing oszn organization ids.
CREATE TABLE `temp_oszns_0_1_29` (
   `oszn_id` BIGINT(20) NOT NULL,
    PRIMARY KEY(`oszn_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- Fill temp oszns table.
INSERT INTO `temp_oszns_0_1_29` (`oszn_id`) 
	SELECT o.`object_id` FROM `organization` o 
	WHERE EXISTS(SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 904 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
-- Update corrections for all oszns.
UPDATE `person_account` SET `city` = UPPER(`city`), `street_type` = UPPER(`street_type`), `street` = UPPER(`street`),
	`building_num` = UPPER(`building_num`), `building_corp` = UPPER(`building_corp`), `apartment` = UPPER(`apartment`),
	`first_name` = UPPER(`first_name`), `middle_name` = UPPER(`middle_name`), `last_name` = UPPER(`last_name`);

UPDATE `city_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `city_type_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `district_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `street_type_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `street_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `building_correction` SET `correction` = UPPER(`correction`), `correction_corp` = UPPER(`correction_corp`), 
	`organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);	

UPDATE `ownership_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

UPDATE `privilege_correction` SET `correction` = UPPER(`correction`), `organization_code` = UPPER(`organization_code`)
	WHERE `organization_id` IN (SELECT `oszn_id` FROM `temp_oszns_0_1_29`);

-- Drops temp table.
DROP TABLE `temp_oszns_0_1_29`;

INSERT INTO `update` (`version`) VALUE ('20120426_766_0.1.29');