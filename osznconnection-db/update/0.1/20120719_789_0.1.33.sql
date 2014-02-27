-- Changes a way of construction a file path to point where request files should be loaded from and saved to.
-- Moves organization attributes responsible for load/save directories for request files from user organizations to oszn organizations
-- and makes it relative.
-- Removes default configurations responsible for load/save directories.
-- Adds two new mandatory user organization's attributes - root directory for loading/saving request files and 
-- EDRPOU(ЕДРПОУ). Default attribute values - 'UNSPECIFIED'. You must change these values to correct ones.

SET GLOBAL log_bin_trust_function_creators = 1;

-- -------------------------------
-- Request files paths attributes
-- -------------------------------

-- Save payments/benefits directory. It is OSZN only attribute. --
UPDATE `string_culture` SET `value` = 'Директория исходящих ответов на запросы на субсидию' WHERE `id` = 917;

-- Load actual payments directory. It is OSZN only attribute. --
UPDATE `string_culture` SET `value` = 'Директория входящих запросов фактического начисления' WHERE `id` = 918;

-- Save actual payments directory. It is OSZN only attribute. --
UPDATE `string_culture` SET `value` = 'Директория исходящих ответов на запросы фактического начисления' WHERE `id` = 919;

-- References directory. It is OSZN only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (926, 1, UPPER('Директория справочников')), (926, 2, UPPER('Директория справочников'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (925, 900, 0, 926, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (925, 925, UPPER('string'));

-- EDRPOU(ЕДРПОУ) attribute. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (927, 1, UPPER('ЕДРПОУ')), (927, 2, UPPER('ЕДРПОУ'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (926, 900, 1, 927, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (926, 926, UPPER('string'));

-- Root directory for loading and saving request files. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (928, 1, UPPER('Корневой каталог для файлов запрсов')), (928, 2, UPPER('Корневой каталог для файлов запрсов'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (927, 900, 1, 928, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (927, 927, UPPER('string'));

-- Create temp table containing all user organizations ids.
CREATE TABLE `temp_user_organizations_0_1_33` (
	`user_organization_id` BIGINT(20) NOT NULL,
	PRIMARY KEY(`user_organization_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Fill temp user organizations table.
INSERT INTO `temp_user_organizations_0_1_33` (`user_organization_id`) 
	SELECT o.`object_id` FROM `organization` o WHERE EXISTS(
		SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 904 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 1);

-- Delete load/save directory attribute from user organizations.
DELETE FROM `organization_string_culture` WHERE `id` IN 
	(SELECT a.`value_id` FROM `organization_attribute` a WHERE a.`attribute_type_id` >= 915 AND a.`attribute_type_id` <= 920
		AND a.`object_id` IN (SELECT `user_organization_id` FROM `temp_user_organizations_0_1_33`));
DELETE FROM `organization_attribute` WHERE `attribute_type_id` >= 915 AND `attribute_type_id` <= 920
		AND `object_id` IN (SELECT `user_organization_id` FROM `temp_user_organizations_0_1_33`);

-- Add default value 'UNSPECIFIED' to EDRPOU and root-directory attributes for all user organizations.

DELIMITER /
CREATE FUNCTION `insertString_0.1.33`(p_locale_id BIGINT(20), p_value VARCHAR(1000)) RETURNS BIGINT(20)
BEGIN
    DECLARE l_strings_seq BIGINT(20);
    
    SELECT `sequence_value` INTO l_strings_seq FROM `sequence` WHERE `sequence_name` = 'organization_string_culture';
    INSERT INTO `organization_string_culture`(`id`, `locale_id`, `value`) 
		VALUES (l_strings_seq, p_locale_id, UPPER(p_value));
    UPDATE `sequence` SET `sequence_value` = (l_strings_seq+1) WHERE `sequence_name` = 'organization_string_culture';
    RETURN l_strings_seq;
END/
DELIMITER ;

DELIMITER /
CREATE PROCEDURE `insertAttribute_0.1.33`(p_object_id BIGINT(20), p_start_date TIMESTAMP, p_string_id BIGINT(20),
			p_attribute_type_id INT, p_value_type_id INT)
BEGIN
    INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`, `status`) 
    VALUES (1, p_object_id, p_attribute_type_id, p_string_id, p_value_type_id, p_start_date, 'ACTIVE');
END/
DELIMITER ;

DELIMITER /
CREATE PROCEDURE `set_default_values_0.1.33`()
BEGIN
    DECLARE l_o_id BIGINT(20);
    DECLARE l_start_date TIMESTAMP;
    DECLARE l_string_id BIGINT(20);
    DECLARE l_locale_id BIGINT(20);    
    DECLARE done INT;
    
    DECLARE o_cursor CURSOR FOR 
	SELECT o.`object_id`, MIN(o.`start_date`) FROM `organization` o 
	WHERE o.`object_id` IN (SELECT `user_organization_id` FROM `temp_user_organizations_0_1_33`)
	GROUP BY o.`object_id`;
	
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    SET done = 0;
    
     -- figure out system locale.
    SELECT `id` INTO l_locale_id FROM `locales` WHERE `system` = 1;
    
    OPEN o_cursor;
    o_loop: LOOP
	FETCH o_cursor INTO l_o_id, l_start_date;

	IF done = 1 THEN
	    LEAVE o_loop;
	END IF;
	
	-- EDRPOU attribute default value.
	SELECT `insertString_0.1.33`(l_locale_id, 'UNSPECIFIED') INTO `l_string_id`;
	CALL `insertAttribute_0.1.33`(l_o_id, l_start_date, l_string_id, 926, 926);
	
	-- root-directory attribute default value.
	SELECT `insertString_0.1.33`(l_locale_id, 'UNSPECIFIED') INTO `l_string_id`;
	CALL `insertAttribute_0.1.33`(l_o_id, l_start_date, l_string_id, 927, 927);
    END LOOP o_loop;
    CLOSE o_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `set_default_values_0.1.33`();
DROP PROCEDURE `set_default_values_0.1.33`;
DROP PROCEDURE `insertAttribute_0.1.33`;
DROP FUNCTION `insertString_0.1.33`;
DROP TABLE `temp_user_organizations_0_1_33`;

-- Delete default 
DELETE FROM `config` WHERE `name` = 'DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR';
DELETE FROM `config` WHERE `name` = 'DEFAULT_SAVE_PAYMENT_BENEFIT_FILES_DIR';

DELETE FROM `config` WHERE `name` = 'DEFAULT_LOAD_ACTUAL_PAYMENT_DIR';
DELETE FROM `config` WHERE `name` = 'DEFAULT_SAVE_ACTUAL_PAYMENT_DIR';
	
DELETE FROM `config` WHERE `name` = 'DEFAULT_LOAD_SUBSIDY_DIR';
DELETE FROM `config` WHERE `name` = 'DEFAULT_SAVE_SUBSIDY_DIR';

DELETE FROM `config` WHERE `name` = 'LOAD_TARIF_DIR';

INSERT INTO `update` (`version`) VALUE ('20120719_789_0.1.33');

SET GLOBAL log_bin_trust_function_creators = 0;