-- Script changes organization data structure: calculation center now has mandatory reference to jdbc data source
-- that will be used to establish connection to that calculation center when it needs to process files.
-- All calculation center organizations will have 'jdbc/osznconnection_remote_resource' jdbc data source by default.

-- Reference to jdbc data source. It is calculation center only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (914, 1, UPPER('Ресурс доступа к МН')), (914, 2, UPPER('Ресурс доступа к МН'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (913, 900, 1, 914, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (913, 913, UPPER('string'));

DELIMITER /
CREATE FUNCTION `insertString_20120213_0.1.21`(p_locale_id BIGINT(20), p_value VARCHAR(1000)) RETURNS BIGINT(20)
BEGIN
    DECLARE organization_strings_seq BIGINT(20);  
    
    SELECT `sequence_value` INTO organization_strings_seq FROM `sequence` WHERE `sequence_name` = 'organization_string_culture';
    INSERT INTO `organization_string_culture`(`id`, `locale_id`, `value`) VALUES (organization_strings_seq, p_locale_id, UPPER(p_value));
    UPDATE `sequence` SET `sequence_value` = (organization_strings_seq+1) WHERE `sequence_name` = 'organization_string_culture';
    RETURN organization_strings_seq;
END/
DELIMITER ;

DELIMITER /
CREATE PROCEDURE `updateCalculationCenters_20120213_0.1.21`()
BEGIN
    DECLARE l_o_id BIGINT(20);
    DECLARE l_start_date DATETIME;
    DECLARE done INT;
    DECLARE l_locale_id BIGINT(20);
    DECLARE l_string_id BIGINT(20);
    
    -- calculation centres cursor
    DECLARE o_cursor CURSOR FOR 
	SELECT o.`object_id`, o.`start_date` FROM `organization` o 
	WHERE EXISTS(SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 3);
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
	
	-- all calculation center organizations will have 'jdbc/osznconnection_remote_resource' jdbc data source by default.
	SELECT `insertString_20120213_0.1.21`(l_locale_id, 'jdbc/osznconnection_remote_resource') INTO l_string_id;
	INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,913,l_string_id,913,l_start_date);
    END LOOP o_loop;
    CLOSE o_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `updateCalculationCenters_20120213_0.1.21`();

DROP PROCEDURE `updateCalculationCenters_20120213_0.1.21`;
DROP FUNCTION `insertString_20120213_0.1.21`;

INSERT INTO `update` (`version`) VALUE ('20120213_725_0.1.21');



