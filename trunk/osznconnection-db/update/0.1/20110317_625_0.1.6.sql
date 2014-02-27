-- Adds full name attributes to city_type and street_type.

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1402, 1, UPPER('Полное название')), (1402, 2, UPPER('Полное название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1401, 1400, 1, 1402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1401, 1401, UPPER('string_culture'));

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1302, 1, UPPER('Полное название')), (1302, 2, UPPER('Полное название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1301, 1300, 1, 1302, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1301, 1301, UPPER('string_culture'));

DELIMITER /
CREATE PROCEDURE `addStreetTypeFullNames`()
BEGIN
    DECLARE l_street_type_id BIGINT(20);
    DECLARE l_street_type_name VARCHAR(1000);
    DECLARE l_start_date DATETIME;
    DECLARE l_string_id BIGINT(20);
    DECLARE done INT;
    DECLARE street_type_cursor CURSOR FOR SELECT DISTINCT `object_id`, `start_date` FROM `street_type` WHERE `status` = 'ACTIVE';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN street_type_cursor;
    street_type_loop: LOOP
        FETCH street_type_cursor INTO l_street_type_id, l_start_date;

        IF done = 1 THEN
            LEAVE street_type_loop;
        END IF;
	
	SELECT `sequence_value` INTO l_string_id FROM `sequence` WHERE `sequence_name` = 'street_type_string_culture';
	SELECT `value` INTO l_street_type_name FROM `street_type_string_culture` str
			JOIN `street_type_attribute` a ON (a.`object_id` = l_street_type_id AND 
				a.`status` = 'ACTIVE' AND a.`attribute_type_id` = 1400 AND a.`value_id` = str.`id`)
			WHERE str.`locale_id` = 1;
		
	INSERT INTO `street_type_string_culture`(`id`, `locale_id`, `value`) VALUES (l_string_id,1, l_street_type_name);
	INSERT INTO `street_type_attribute`(`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`) VALUES
		(l_street_type_id, 1, 1401, l_string_id, 1401, l_start_date);
	UPDATE `sequence` SET `sequence_value` = (l_string_id+1) WHERE `sequence_name` = 'street_type_string_culture';

    END LOOP street_type_loop;
    CLOSE street_type_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `addStreetTypeFullNames`();

DROP PROCEDURE `addStreetTypeFullNames`;

DELIMITER /
CREATE PROCEDURE `addCityTypeFullNames`()
BEGIN
    DECLARE l_city_type_id BIGINT(20);
    DECLARE l_city_type_name VARCHAR(1000);
    DECLARE l_start_date DATETIME;
    DECLARE l_string_id BIGINT(20);
    DECLARE done INT;
    DECLARE city_type_cursor CURSOR FOR SELECT DISTINCT `object_id`, `start_date` FROM `city_type` WHERE `status` = 'ACTIVE';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN city_type_cursor;
    city_type_loop: LOOP
        FETCH city_type_cursor INTO l_city_type_id, l_start_date;

        IF done = 1 THEN
            LEAVE city_type_loop;
        END IF;
	
	SELECT `sequence_value` INTO l_string_id FROM `sequence` WHERE `sequence_name` = 'city_type_string_culture';
	SELECT `value` INTO l_city_type_name FROM `city_type_string_culture` str
			JOIN `city_type_attribute` a ON (a.`object_id` = l_city_type_id AND 
				a.`status` = 'ACTIVE' AND a.`attribute_type_id` = 1300 AND a.`value_id` = str.`id`)
			WHERE str.`locale_id` = 1;
		
	INSERT INTO `city_type_string_culture` (`id`, `locale_id`, `value`) VALUES (l_string_id, 1, l_city_type_name);
	INSERT INTO `city_type_attribute`(`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`) VALUES
		(l_city_type_id, 1, 1301, l_string_id, 1301, l_start_date);
	UPDATE `sequence` SET `sequence_value` = (l_string_id+1) WHERE `sequence_name` = 'city_type_string_culture';

    END LOOP city_type_loop;
    CLOSE city_type_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `addCityTypeFullNames`();

DROP PROCEDURE `addCityTypeFullNames`;

INSERT INTO `update` (`version`) VALUE ('20110317_625_0.1.6');

