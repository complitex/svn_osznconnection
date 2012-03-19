-- Changes organizations data structure: mandatory reference to service provider type from calculation center 
-- was dropped, user organization now has mandatory set of associations where each association links 
-- service provider type and calculation center, that is associations reference to service ptovider type 
-- and calculation center.

SET GLOBAL log_bin_trust_function_creators = 1;

DELIMITER /
CREATE PROCEDURE `update_0.1.23`()
BEGIN

    DECLARE l_o_id BIGINT(20);
    DECLARE l_start_date DATETIME;
    DECLARE l_calc_center_id BIGINT(20);
    DECLARE l_spt_id BIGINT(20);
    DECLARE l_service_association_id BIGINT(20);
    DECLARE l_attribute_count INT;
    DECLARE l_done INT DEFAULT 0;
    
    -- user organizations cursor
    DECLARE o_cursor CURSOR FOR 
	SELECT o.`object_id`, o.`start_date` FROM `organization` o 
	WHERE EXISTS(SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 1);
    
    -- calculation center's attributes cursor for attributes that store references to service provider types			
    DECLARE s_cursor CURSOR FOR 
	SELECT DISTINCT a.`value_id` FROM `organization_attribute` a WHERE a.`object_id` = l_calc_center_id AND 
	a.`status` = 'ACTIVE' AND a.`attribute_type_id` = 912;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET l_done = 1;
	
-- ------------------------------
-- Service association
-- ------------------------------
DROP TABLE IF EXISTS `service_association`;

CREATE TABLE `service_association` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `service_provider_type_id` BIGINT(20) NOT NULL COMMENT 'ID объекта типа поставщика услуги',
  `calculation_center_id` BIGINT(20) NOT NULL COMMENT 'ID модуля начислений',
  PRIMARY KEY  (`pk_id`),
  KEY `key_service_provider_type_id` (`service_provider_type_id`),
  KEY `key_calculation_center_id` (`calculation_center_id`),
  CONSTRAINT `fk_service_association__service_provider_type` FOREIGN KEY (`service_provider_type_id`) REFERENCES `service_provider_type` (`object_id`),
  CONSTRAINT `fk_service_association__calculation_center` FOREIGN KEY (`calculation_center_id`) REFERENCES `organization` (`object_id`)
) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Пары ассоциаций: тип услуги - модуль начислений';

-- Reference to `service_association` helper table. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (915, 1, UPPER('Ассоцияции тип услуги - модуль начислений')), (915, 2, UPPER('Ассоцияции тип услуги - модуль начислений'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (914, 900, 1, 915, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (914, 914, 'service_association');

OPEN o_cursor;
    o_loop: LOOP
	FETCH o_cursor INTO l_o_id, l_start_date;

	IF l_done = 1 THEN
	    LEAVE o_loop;
	END IF;
	
	SELECT `value_id` INTO l_calc_center_id FROM `organization_attribute` WHERE `object_id` = l_o_id 
		AND `status` = 'ACTIVE'	AND `attribute_type_id` = 911;
		
	OPEN s_cursor;
	SET l_attribute_count = 1;
	    s_loop: LOOP      
		FETCH s_cursor INTO l_spt_id;

		IF l_done = 1 THEN
		  LEAVE s_loop;
		END IF;
		
		INSERT INTO `service_association` (`service_provider_type_id`, `calculation_center_id`)
			VALUES (l_spt_id, l_calc_center_id);
		SELECT LAST_INSERT_ID() INTO l_service_association_id;
		
		INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (l_attribute_count,l_o_id,914,l_service_association_id,914,l_start_date);
			
		SET l_attribute_count = l_attribute_count + 1;
	    END LOOP;
	CLOSE s_cursor;
	SET l_done = 0;	
    END LOOP o_loop;
CLOSE o_cursor;

DELETE FROM `organization_attribute` WHERE `attribute_type_id` IN (911, 912);

DELETE FROM `entity_attribute_value_type` WHERE `id` IN (911, 912);
DELETE FROM `entity_attribute_type` WHERE `id` IN (911, 912);
DELETE FROM `string_culture` WHERE `id` IN (912, 913);

INSERT INTO `update` (`version`) VALUE ('20120319_733_0.1.23');
    
END/
DELIMITER ;

CALL `update_0.1.23`();
DROP PROCEDURE `update_0.1.23`;

SET GLOBAL log_bin_trust_function_creators = 0;