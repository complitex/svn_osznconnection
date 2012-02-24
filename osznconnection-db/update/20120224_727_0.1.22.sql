-- 1. Adds visibility of request files and corrections by user organizations.

DELIMITER /
CREATE PROCEDURE `update_0.1.22`()
BEGIN
    DECLARE l_count BIGINT(20);
    DECLARE l_error_message VARCHAR(100);
    DECLARE l_user_org_id BIGINT(20);
    
    -- at first check whether there is exactly one user organization. if no then abort execution.
    SELECT COUNT(*) INTO l_count FROM `organization_attribute` o WHERE o.`status` = 'ACTIVE' 
		AND o.`attribute_type_id` = 905 AND o.`value_id` = 1;
    
    IF l_count != 1 THEN
	IF l_count = 0 THEN 
	    SET l_error_message = 'There is no one user organization.';
	ELSE
	    SET l_error_message = 'There is more one user organization.';
	END IF;
	SELECT l_error_message FROM DUAL;
    ELSE 
	-- update body
	
	-- find user organization id.
	SELECT o.`object_id` INTO l_user_org_id FROM `organization_attribute` o WHERE o.`status` = 'ACTIVE' 
		AND o.`attribute_type_id` = 905 AND o.`value_id` = 1;
	
	-- change `request_file` structure.
	ALTER TABLE `request_file` ADD COLUMN `user_organization_id` 
		BIGINT(20) NOT NULL COMMENT 'Идентификатор организации пользователя, который загрузил текущий файл' AFTER `status`;
	ALTER TABLE `request_file` ADD KEY `key_user_organization_id` (`user_organization_id`);
	ALTER TABLE `request_file` ADD CONSTRAINT `fk_request_file__user_organization`
		FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`);
		
	-- change `city_correction` structure.
	ALTER TABLE `city_correction` DROP KEY `uk_city_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_city_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_city_correction` (`object_id`, `correction`, `organization_id`, `user_organization_id`, `internal_organization_id`);
	
	-- change `city_type_correction` structure.
	ALTER TABLE `city_type_correction` DROP KEY `uk_city_type_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_city_type_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_city_type_correction` (`object_id`, `correction`, `organization_id`, `user_organization_id`, `internal_organization_id`);

	-- change `district_correction` structure.
	ALTER TABLE `district_correction` DROP KEY `uk_district_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_district_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_district_correction` (`parent_id`, `object_id`, `correction`, `organization_id`, `user_organization_id`, `internal_organization_id`);

	-- change `street_correction` structure.
	ALTER TABLE `street_correction` DROP KEY `uk_street_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_street_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_street_correction` (`parent_id`, `correction`, `organization_id`, `user_organization_id`, `internal_organization_id`,
            `street_type_correction_id`, `object_id`, `organization_code`);
	
	-- change `street_type_correction` structure.
	ALTER TABLE `street_type_correction` DROP KEY `uk_street_type_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_street_type_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_street_type_correction` (`object_id`, `correction`, `organization_id`, `user_organization_id`, `internal_organization_id`);
	
	-- change `building_correction` structure.
	ALTER TABLE `building_correction` DROP KEY `uk_building_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_building_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_building_correction` (`parent_id`, `object_id`, `correction`, `correction_corp`, `organization_id`, 
                `user_organization_id`, `internal_organization_id`);
	
	-- change `ownership_correction` structure.
	ALTER TABLE `ownership_correction` DROP KEY `uk_ownership_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_ownership_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_ownership_correction` (`object_id`, `correction`, `organization_id`, `user_organization_id`, 
                `internal_organization_id`, `organization_code`);
	
	-- change `privilege_correction` structure.
	ALTER TABLE `privilege_correction` DROP KEY `uk_privilege_correction`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `internal_organization_id`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_privilege_correction__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_privilege_correction` (`object_id`, `correction`, `organization_id`, `user_organization_id`, 
                    `internal_organization_id`, `organization_code`);
	
	-- change `person_account` structure.
	ALTER TABLE `person_account` DROP KEY `uk_person_account`, ADD COLUMN `user_organization_id` BIGINT(20) AFTER `pu_account_number`,
	ADD KEY `key_user_organization_id` (`user_organization_id`), ADD CONSTRAINT `fk_person_account__user_organization`
	FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
	ADD UNIQUE KEY `uk_person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street_type`, `street`, `building_num`,
        `building_corp`, `apartment`, `oszn_id`, `calc_center_id`, `pu_account_number`, `user_organization_id`);
	
	-- set user organization id for all request files.
	UPDATE `request_file` SET `user_organization_id` = l_user_org_id;
	
	-- set user organization id for all person accounts and oszn corrections (oszn type id = 2).
	-- person accounts
	UPDATE `person_account` SET `user_organization_id` = l_user_org_id;
	
	-- city corrections
	UPDATE `city_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- city_type corrections
	UPDATE `city_type_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
	
	-- district corrections
	UPDATE `district_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- street corrections
	UPDATE `street_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- street_type corrections
	UPDATE `street_type_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- building corrections
	UPDATE `building_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- ownership corrections
	UPDATE `ownership_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);
		
	-- privilege corrections
	UPDATE `privilege_correction` SET `user_organization_id` = l_user_org_id 
	WHERE `organization_id` IN (SELECT o_type.`object_id` FROM `organization_attribute` o_type WHERE 
		o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 2);

	INSERT INTO `update` (`version`) VALUE ('20120224_727_0.1.22');
    END IF;
END/
DELIMITER ;

CALL `update_0.1.22`();
DROP PROCEDURE `update_0.1.22`;