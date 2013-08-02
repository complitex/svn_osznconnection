-- 1. Adds service provider type entity.
-- 2. Changes organization data structure: calculation center has mandatory reference to 
--    set of service provider types (attribute type id = 912),
--    user organization has mandatory reference to calculation center organization (attribute type id = 911).
--    Old calculation center's property 'current calculation center' being dropped.

DELIMITER /
CREATE PROCEDURE `updateUserOrganizations`()
BEGIN
    DECLARE l_count BIGINT(20);
    DECLARE l_o_current_id BIGINT(20);
    DECLARE l_start_date DATETIME;
    DECLARE l_o_id BIGINT(20);
    DECLARE done INT;
    
    -- user organizations cursor
    DECLARE o_cursor CURSOR FOR 
	SELECT o.`object_id`, o.`start_date` FROM `organization` o 
	WHERE EXISTS(SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 1);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
	
    -- retrieve current calculation center id. It should be only one.
    SELECT o_current.`object_id` INTO l_o_current_id FROM `organization_attribute` o_current WHERE o_current.`status` = 'ACTIVE' 
		AND o_current.`attribute_type_id` = 904 AND 
		UPPER('true') = (SELECT UPPER(TRIM(val.`value`)) FROM `organization_string_culture` val WHERE 
			o_current.`value_id` = val.`id` AND val.`locale_id` = (SELECT `id` FROM `locales` l WHERE l.`system` = 1));

    OPEN o_cursor;
    o_loop: LOOP
	FETCH o_cursor INTO l_o_id, l_start_date;

	IF done = 1 THEN
	    LEAVE o_loop;
	END IF;
	
	-- all user organizations will reference 'current' calculation center by default.
	INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,911,l_o_current_id,911,l_start_date);
    END LOOP o_loop;
    CLOSE o_cursor;
    SET done = 0;
END/
DELIMITER ;

DELIMITER /
CREATE PROCEDURE `updateCalculationCenterOrganizations`()
BEGIN
    DECLARE l_o_id BIGINT(20);
    DECLARE l_start_date DATETIME;
    DECLARE done INT;
    
    -- calculation centres cursor
    DECLARE o_cursor CURSOR FOR 
	SELECT o.`object_id`, o.`start_date` FROM `organization` o 
	WHERE EXISTS(SELECT 1 FROM `organization_attribute` o_type WHERE 
		o.`object_id` = o_type.`object_id` AND o_type.`attribute_type_id` = 905 AND o_type.`status` = 'ACTIVE' 
		AND o_type.`value_id` = 3);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    SET done = 0;
    
    OPEN o_cursor;
    o_loop: LOOP
	FETCH o_cursor INTO l_o_id, l_start_date;

	IF done = 1 THEN
	    LEAVE o_loop;
	END IF;
	
	-- all calculation center organizations will have only apartment fee service provider by default (id = 1).
	INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,912,1,912,l_start_date);
    END LOOP o_loop;
    CLOSE o_cursor;
    SET done = 0;
END/
DELIMITER ;

DELIMITER /
CREATE PROCEDURE `update_20120203`()
BEGIN
    DECLARE l_count BIGINT(20);
    DECLARE l_error_message VARCHAR(100);
    
    -- at first check whether there is exactly one 'current' calculation center. if no then abort execution.
    SELECT COUNT(*) INTO l_count FROM `organization_attribute` o_current WHERE o_current.`status` = 'ACTIVE' 
		AND o_current.`attribute_type_id` = 904 AND 
		UPPER('true') = (SELECT UPPER(TRIM(val.`value`)) FROM `organization_string_culture` val WHERE 
			o_current.`value_id` = val.`id` AND val.`locale_id` = (SELECT `id` FROM `locales` l WHERE l.`system` = 1));
    
    IF l_count != 1 THEN
	IF l_count = 0 THEN 
	    SET l_error_message = 'There is no one current calculation center.';
	ELSE
	    SET l_error_message = 'There is more one current calculation center.';
	END IF;
	SELECT l_error_message FROM DUAL;
    ELSE 
	-- update body
	
	-- ------------------------------
	-- Service provider types
	-- ------------------------------
	DROP TABLE IF EXISTS `service_provider_type`;

	CREATE TABLE `service_provider_type` (
	  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
	  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
	  `parent_id` BIGINT(20) COMMENT 'Не используется',
	  `parent_entity_id` BIGINT(20) COMMENT 'Не используется',
	  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия параметров объекта',
	  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия параметров объекта',
	  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус объекта: ACTIVE, INACTIVE или ARCHIVE',
	  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
	  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
	  PRIMARY KEY  (`pk_id`),
	  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
	  UNIQUE KEY `unique_external_id` (`external_id`),
	  KEY `key_object_id` (`object_id`),
	  KEY `key_parent_id` (`parent_id`),
	  KEY `key_parent_entity_id` (`parent_entity_id`),
	  KEY `key_start_date` (`start_date`),
	  KEY `key_end_date` (`end_date`),
	  KEY `key_status` (`status`),
	  KEY `key_permission_id` (`permission_id`),
	  CONSTRAINT `fk_service_provider_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
	  CONSTRAINT `fk_service_provider_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
	) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Типы поставщиков услуг';

	DROP TABLE IF EXISTS `service_provider_type_attribute`;

	CREATE TABLE `service_provider_type_attribute` (
	  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
	  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
	  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
	  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута. Возможные значения: 1600 - НАЗВАНИЕ',
	  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
	  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа. Возможные значения: 1600 - STRING_CULTURE',
	  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия значений атрибута',
	  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата завершения периода действия значений атрибута',
	  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус атрибута: ACTIVE, INACTIVE или ARCHIVE',
	  PRIMARY KEY  (`pk_id`),
	  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
	  KEY `key_object_id` (`object_id`),
	  KEY `key_attribute_type_id` (`attribute_type_id`),
	  KEY `key_value_id` (`value_id`),
	  KEY `key_value_type_id` (`value_type_id`),
	  KEY `key_start_date` (`start_date`),
	  KEY `key_end_date` (`end_date`),
	  KEY `key_status` (`status`),
	  CONSTRAINT `fk_service_provider_type_attribute__privilege` FOREIGN KEY (`object_id`) REFERENCES `privilege`(`object_id`),
	  CONSTRAINT `fk_service_provider_type_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
	    REFERENCES `entity_attribute_type` (`id`),
	  CONSTRAINT `fk_service_provider_type_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
	    REFERENCES `entity_attribute_value_type` (`id`)
	) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Атрибуты объекта типа поставщика услуг';

	DROP TABLE IF EXISTS `service_provider_type_string_culture`;

	CREATE TABLE `service_provider_type_string_culture` (
	  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
	  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор значения',
	  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
	  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
	  PRIMARY KEY (`pk_id`),
	  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
	  KEY `key_locale` (`locale_id`),
	  KEY `key_value` (`value`),
	  CONSTRAINT `fk_service_provider_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
	) ENGINE=INNODB DEFAULT  CHARSET=utf8 COMMENT 'Локализация атрибутов типа поставщика услуг';

	INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1600, 1, 'Тип услуги'), (1600, 2, 'Тип услуги');
	INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1600, 'service_provider_type', 1600, '');
	INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1601, 1, UPPER('Название')), (1601, 2, UPPER('Назва'));
	INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1600, 1600, 1, 1601, 1);
	INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1600, 1600, UPPER('string_culture'));

	-- Predefined service provider types
	INSERT INTO `service_provider_type`(`object_id`) VALUES (1),(7); 
	INSERT INTO `service_provider_type_string_culture`(`id`, `locale_id`, `value`) VALUES 
	(1, 1, UPPER('квартплата')), (1, 2,UPPER('оплата житла')),
	(7, 1, UPPER('вывоз мусора')), (7, 2, UPPER('вивезення сміття'));
	INSERT INTO `service_provider_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
	(1,1,1600,1,1600),(1,7,1600,7,1600);

	-- Reference to calculation center. It is user organization only attribute. --
	INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (912, 1, UPPER('Модуль начислений')), (912, 2, UPPER('Модуль начислений'));
	INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (911, 900, 0, 912, 1);
	INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (911, 911, 'organization');

	-- Reference to the set of service provider types. It is calculation center only attribute. --
	INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (913, 1, UPPER('Типы услуг')), (913, 2, UPPER('Типы услуг'));
	INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (912, 900, 0, 913, 1);
	INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (912, 912, 'service_provider_type');

	CALL `updateUserOrganizations`();
	CALL `updateCalculationCenterOrganizations`();
	
	-- Delete 'current calculation center' property
	DELETE FROM `organization_string_culture` WHERE `id` IN 
		(SELECT `value_id` FROM `organization_attribute` WHERE `attribute_type_id` = 904);
	DELETE FROM `organization_attribute` WHERE `attribute_type_id` = 904;
	DELETE FROM `entity_attribute_value_type` WHERE `id` = 904;
	DELETE FROM `entity_attribute_type` WHERE `id` = 904;
	DELETE FROM `string_culture` WHERE `id` = 906;

	INSERT INTO `update` (`version`) VALUE ('20120203_720_0.1.20');
    END IF;
END/
DELIMITER ;

CALL `update_20120203`();
DROP PROCEDURE `update_20120203`;

DROP PROCEDURE `updateUserOrganizations`;
DROP PROCEDURE `updateCalculationCenterOrganizations`;



