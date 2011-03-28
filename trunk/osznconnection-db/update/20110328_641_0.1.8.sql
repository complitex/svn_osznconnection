-- Adds current calculation center flag attribute to all calculation centers.
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (908, 1, UPPER('Является текущим модулем начислений')), (908, 2, UPPER('Является текущим модулем начислений'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (904, 900, 0, 908, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (904, 904, UPPER('boolean'));

DELIMITER /
CREATE PROCEDURE `updateCalculationCentres`()
BEGIN
    DECLARE l_start_date DATETIME;
    DECLARE l_string_id BIGINT(20);
    DECLARE l_cc_id BIGINT(20);
    DECLARE l_current_cc BIGINT(20);
    DECLARE l_current_flag VARCHAR(5);
    DECLARE done INT;
    DECLARE cc_cursor CURSOR FOR SELECT DISTINCT `object_id`, `start_date` FROM `organization` WHERE `status` = 'ACTIVE' 
			AND `entity_type_id` = 901;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN cc_cursor;
    cc_loop: LOOP
        FETCH cc_cursor INTO l_cc_id, l_start_date;

        IF done = 1 THEN
            LEAVE cc_loop;
        END IF;
        
        SELECT `calculation_center_id` INTO l_current_cc FROM `calculation_center_preference` WHERE `id` = 1;
        IF l_current_cc = l_cc_id THEN 
		SET l_current_flag = 'true';
	ELSE 
		SET l_current_flag = 'false';
	END IF;
	
	SELECT `sequence_value` INTO l_string_id FROM `sequence` WHERE `sequence_name` = 'organization_string_culture';
		
	INSERT INTO `organization_string_culture`(`id`, `locale_id`, `value`) VALUES (l_string_id,1,UPPER(l_current_flag));
	INSERT INTO `organization_attribute`(`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`) 
	VALUES (l_cc_id, 1, 904, l_string_id, 904, l_start_date);
	UPDATE `sequence` SET `sequence_value` = (l_string_id+1) WHERE `sequence_name` = 'organization_string_culture';

    END LOOP cc_loop;
    CLOSE cc_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `updateCalculationCentres`();

DROP PROCEDURE `updateCalculationCentres`;

DROP TABLE `calculation_center_preference`;

INSERT INTO `update` (`version`) VALUE ('20110328_641_0.1.8');

