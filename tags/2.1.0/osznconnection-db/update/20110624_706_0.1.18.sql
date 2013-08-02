-- Changes organization data structure.

-- --------------------------------
-- Organization type
-- --------------------------------
DROP TABLE IF EXISTS `organization_type`;

CREATE TABLE `organization_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_organization_type__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_organization_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_organization_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_type_attribute`;

CREATE TABLE `organization_type_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_organization_type_attribute__organization` FOREIGN KEY (`object_id`) REFERENCES `organization_type`(`object_id`),
  CONSTRAINT `fk_organization_type_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_organization_type_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_type_string_culture`;

CREATE TABLE `organization_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_organization_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- Common orgainization type: user organization type. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (2300, 1, 'Тип организации'), (2300, 2, 'Тип организации');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (2300, 'organization_type', 2300, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (2301, 1, UPPER('Тип организации')), (2301, 2, UPPER('Тип организации'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (2300, 2300, 1, 2301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (2300, 2300, UPPER('string_culture'));
INSERT INTO `organization_type`(`object_id`) VALUES (1);
INSERT INTO `organization_type_string_culture`(`id`, `locale_id`, `value`) VALUES (1, 1, UPPER('Организации пользователей')), (1, 2,UPPER('Организации пользователей'));
INSERT INTO `organization_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,1,2300,1,2300);

-- Oszn specific organization types: oszn, calculation center types. --
INSERT INTO `organization_type`(`object_id`) VALUES (2),(3);
INSERT INTO `organization_type_string_culture`(`id`, `locale_id`, `value`) VALUES (2, 1, UPPER('ОСЗН')), (2, 2,UPPER('ОСЗН')),
(3, 1, UPPER('Модуль начислений')), (3, 2,UPPER('Центр нарахувань'));
INSERT INTO `organization_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,2,2300,2,2300),(1,3,2300,3,2300);

UPDATE sequence SET sequence_value = (SELECT IFNULL(MAX(`id`), 0) FROM `organization_type_string_culture`)+1 WHERE sequence_name = 'organization_type_string_culture';
UPDATE sequence SET sequence_value = (SELECT IFNULL(MAX(`object_id`), 0) FROM `organization_type`)+1 WHERE sequence_name = 'organization_type';

UPDATE `string_culture` SET `value` = UPPER('Тип организации') WHERE `id` = 906;
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (905, 900, 0, 906, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (905, 905, 'organization_type');

DELIMITER /
CREATE PROCEDURE `updateOrganizations`()
BEGIN
    DECLARE l_start_date DATETIME;
    DECLARE l_entity_type_id BIGINT(20);
    DECLARE l_o_id BIGINT(20);
    DECLARE done INT;
    DECLARE o_cursor CURSOR FOR SELECT DISTINCT `object_id`, `entity_type_id`, `start_date` FROM `organization` WHERE `status` = 'ACTIVE';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN o_cursor;
    o_loop: LOOP
        FETCH o_cursor INTO l_o_id, l_entity_type_id, l_start_date;

        IF done = 1 THEN
            LEAVE o_loop;
        END IF;
        
        CASE l_entity_type_id
		WHEN 900 THEN 
			INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,905,2,905,l_start_date);
		WHEN 901 THEN 
			INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,905,3,905,l_start_date);
		WHEN 902 THEN
			INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, 
			`start_date`) VALUES (1,l_o_id,905,1,905,l_start_date);
		ELSE BEGIN END;
	END CASE;
    END LOOP o_loop;
    CLOSE o_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `updateOrganizations`();

DROP PROCEDURE `updateOrganizations`;

UPDATE `organization` SET `entity_type_id` = NULL;
DELETE FROM `entity_type` WHERE `id` IN (900,901,902);
DELETE FROM `string_culture` WHERE `id` IN (904,905);
/* 907 replace by 904 */
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (904, 1, UPPER('Принадлежит')), (904, 2, UPPER('Принадлежит'));
UPDATE `entity_attribute_type` SET `attribute_type_name_id` = 904 WHERE `id` = 903;
/* 906 replace by 905 */
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (905, 1, UPPER('Тип организации')), (905, 2, UPPER('Тип организации'));
UPDATE `entity_attribute_type` SET `attribute_type_name_id` = 905 WHERE `id` = 905;
/* 908 replace by 906 */
UPDATE `string_culture` SET `value` = UPPER('Является текущим модулем начислений') WHERE `id` = 906;
UPDATE `entity_attribute_type` SET `attribute_type_name_id` = 906 WHERE `id` = 904;
DELETE FROM `string_culture` WHERE `id` >=907 AND `id` < 1000;

INSERT INTO `update` (`version`) VALUE ('20110624_706_0.1.18');

