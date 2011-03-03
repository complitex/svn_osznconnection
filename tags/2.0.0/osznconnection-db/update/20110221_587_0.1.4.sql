-- Fixes 'LOADED' status code. Adds missing street type related status descriptions. 
-- Sets up street type references for calculation module's street corrections.
-- Sets district's correction parent.
-- Deletes out of date 'MORE_ONE_REMOTE_STREET_TYPE_CORRECTION' request status.

ALTER TABLE `payment` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';
ALTER TABLE `benefit` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';
ALTER TABLE `actual_payment` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 240 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';
ALTER TABLE `status_description` ADD UNIQUE KEY `uk_status_description` (`code`);

UPDATE `payment` SET `status` = 240 WHERE `status` = 237;
UPDATE `benefit` SET `status` = 240 WHERE `status` = 237;
UPDATE `actual_payment` SET `status` = 240 WHERE `status` = 237;

INSERT INTO `status_description`(`code`, `name`) VALUES (237, 'Неизвестный тип улицы'), 
(238, 'Найдено более одного типа улицы в адресной базе'),(239, 'Найдено более одного соответствия для типа улицы'),
(240, 'Загружена');

DELIMITER /
CREATE PROCEDURE `updateStreetCorrections`()
BEGIN
    DECLARE l_street_correction_id BIGINT(20);
    DECLARE l_street_id BIGINT(20);
    DECLARE l_street_type_id BIGINT(20);
    DECLARE l_street_type_correction_id BIGINT(20);
    DECLARE done INT;
    DECLARE street_correction_cursor CURSOR FOR SELECT `id`, `object_id` FROM `street_correction` WHERE `organization_id` = 1;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN street_correction_cursor;
    street_correction_loop: LOOP
        FETCH street_correction_cursor INTO l_street_correction_id, l_street_id;
        
        IF done = 1 THEN
            LEAVE street_correction_loop;
        END IF;

	SELECT `value_id` INTO l_street_type_id FROM `street_attribute` WHERE `object_id` = l_street_id AND `attribute_type_id` = 301 
										AND `status` = 'ACTIVE';
	
	SELECT `id` INTO l_street_type_correction_id FROM `street_type_correction` WHERE `object_id` = l_street_type_id AND 
									`organization_id` = 1;
				
	UPDATE `street_correction` SET `street_type_correction_id` = l_street_type_correction_id WHERE `id` = l_street_correction_id;       
    END LOOP street_correction_loop;
    CLOSE street_correction_cursor;
    SET done = 0;
END/
DELIMITER ;

DELETE FROM `building_correction` WHERE `organization_id` != 1;
DELETE FROM `street_correction` WHERE `organization_id` != 1;

CALL `updateStreetCorrections`();

DROP PROCEDURE `updateStreetCorrections`;

ALTER TABLE `street_correction` DROP KEY `uk_street_correction`, 
	ADD UNIQUE KEY `uk_street_correction` (`parent_id`, `correction`, `organization_id`, `internal_organization_id`,
            `street_type_correction_id`, `object_id`, `organization_code`);
            
ALTER TABLE `person_account` DROP KEY `uq_person_account`, ADD UNIQUE KEY `uk_person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street_type`, `street`, `street_code`, `building_num`,
        `building_corp`, `apartment`, `own_num_sr`, `oszn_id`, `calc_center_id`);
        
UPDATE `district_correction` SET `parent_id` = (SELECT c.`id` FROM `city_correction` c WHERE c.`organization_id` = 1) 
		WHERE `organization_id` = 1;
		
DELETE FROM `status_description` WHERE `code` = 231;

INSERT INTO `update` (`version`) VALUE ('20110221_0.1.4');

