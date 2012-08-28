-- Refactores tarif request files.

SET GLOBAL log_bin_trust_function_creators = 1;

-- Rename `tarif` table to `subsidy_tarif`.
RENAME TABLE `tarif` TO `subsidy_tarif`;

-- Change type of all tarif request files to 'SUBSIDY_TARIF'.
UPDATE `request_file` SET `type` = 'SUBSIDY_TARIF' WHERE `type` = 'TARIF';

-- Change name of tarif request file type in descriptions.
UPDATE `request_file_description` SET `request_file_type` = 'SUBSIDY_TARIF' WHERE `request_file_type` = 'TARIF';

-- Change tarif's file name mask config.
UPDATE `config` SET `name` = 'SUBSIDY_TARIF_FILENAME_MASK' WHERE `name` = 'TARIF_PAYMENT_FILENAME_MASK';

-- Changes RequestStatus TARIF_CODE2_1_NOT_FOUND to SUBSIDY_TARIF_CODE_NOT_FOUND.
UPDATE `status_description` SET `name` = 'Код тарифа на оплату жилья не найден в справочнике тарифов для запросов по субсидиям'
	WHERE `code` = 216;

-- Changes RequestWarningStatus TARIF_NOT_FOUND to SUBSIDY_TARIF_NOT_FOUND.
UPDATE `status_description` SET `name` = 'Тариф не найден в справочнике тарифов для запросов по субсидиям'
	WHERE `code` = 300;

-- Fix status of tarif request files as LOADED.
UPDATE `request_file` SET `status` = 110 WHERE `type` = 'SUBSIDY_TARIF';

INSERT INTO `update` (`version`) VALUE ('20120828_791_0.1.35');

SET GLOBAL log_bin_trust_function_creators = 0;