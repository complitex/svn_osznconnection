-- Adds new attributes to organization entity that indicate load/save directories for all request files 
-- that should be searched at loading and saving phases of file processing. 
-- Replaces common configurations for request files directories onto default configurations that will be used only if 
-- user organization's load/save attribute's directories were not specified.
-- Adds LOAD_TARIF_DIR config that indicates from where to load tarif files.
-- Removes LOAD_INPUT_FILE_STORAGE_DIR and SAVE_OUTPUT_FILE_STORAGE_DIR configs.

SET GLOBAL log_bin_trust_function_creators = 1;

-- Load payments/benefits directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (916, 1, UPPER('Директория входящих запросов на субсидию')), (916, 2, UPPER('Директория входящих запросов на субсидию'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (915, 900, 0, 916, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (915, 915, UPPER('string'));

-- Save payments/benefits directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (917, 1, UPPER('Директория исходящих запросов на субсидию')), (917, 2, UPPER('Директория исходящих запросов на субсидию'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (916, 900, 0, 917, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (916, 916, UPPER('string'));

-- Load actual payments directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (918, 1, UPPER('Директория входящих запросов фактических начислений')), (918, 2, UPPER('Директория входящих запросов фактических начислений'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (917, 900, 0, 918, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (917, 917, UPPER('string'));

-- Save actual payments directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (919, 1, UPPER('Директория исходящих запросов фактических начислений')), (919, 2, UPPER('Директория исходящих запросов фактических начислений'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (918, 900, 0, 919, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (918, 918, UPPER('string'));

-- Load subsidies directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (920, 1, UPPER('Директория входящих файлов субсидий')), (920, 2, UPPER('Директория входящих файлов субсидий'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (919, 900, 0, 920, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (919, 919, UPPER('string'));

-- Save subsidies directory. It is user organization only attribute. --
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (921, 1, UPPER('Директория исходящих файлов субсидий')), (921, 2, UPPER('Директория исходящих файлов субсидий'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (920, 900, 0, 921, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (920, 920, UPPER('string'));

DELIMITER /
CREATE PROCEDURE `insert_tarif_load_config_dir_0.1.32`()
BEGIN
    DECLARE l_tarif_load_dir VARCHAR(1000);

    SELECT `value` INTO l_tarif_load_dir FROM `config` WHERE `name` = 'LOAD_INPUT_REQUEST_FILE_STORAGE_DIR';
    INSERT INTO `config`(`name`, `value`) VALUES ('LOAD_TARIF_DIR', l_tarif_load_dir);
END/
DELIMITER ;

CALL `insert_tarif_load_config_dir_0.1.32`();
DROP PROCEDURE `insert_tarif_load_config_dir_0.1.32`;

UPDATE `config` SET `name` = 'DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR' WHERE `name` = 'LOAD_INPUT_REQUEST_FILE_STORAGE_DIR';
UPDATE `config` SET `name` = 'DEFAULT_SAVE_PAYMENT_BENEFIT_FILES_DIR' WHERE `name` = 'SAVE_OUTPUT_REQUEST_FILE_STORAGE_DIR';

UPDATE `config` SET `name` = 'DEFAULT_LOAD_ACTUAL_PAYMENT_DIR' WHERE `name` = 'LOAD_INPUT_ACTUAL_PAYMENT_FILE_STORAGE_DIR';
UPDATE `config` SET `name` = 'DEFAULT_SAVE_ACTUAL_PAYMENT_DIR' WHERE `name` = 'SAVE_OUTPUT_ACTUAL_PAYMENT_FILE_STORAGE_DIR';
	
UPDATE `config` SET `name` = 'DEFAULT_LOAD_SUBSIDY_DIR'	WHERE `name` = 'LOAD_INPUT_SUBSIDY_FILE_STORAGE_DIR';
UPDATE `config` SET `name` = 'DEFAULT_SAVE_SUBSIDY_DIR' WHERE `name` = 'SAVE_OUTPUT_SUBSIDY_FILE_STORAGE_DIR';

DELETE FROM `config` WHERE `name` = 'LOAD_INPUT_FILE_STORAGE_DIR' OR `name` = 'SAVE_OUTPUT_FILE_STORAGE_DIR';

INSERT INTO `update` (`version`) VALUE ('20120523_778_0.1.32');

SET GLOBAL log_bin_trust_function_creators = 0;


