-- Type description

CREATE TABLE `type_description` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `code` INTEGER NOT NULL COMMENT 'Код описания типа',
  `name` VARCHAR(500) NOT NULL COMMENT 'Описание типа',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_description` (`code`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8 COMMENT 'Описание типов';

INSERT INTO `type_description`(`code`, `name`) VALUES
(1, 'Льгота запроса на субсидию'), (2, 'Начисление запроса на субсидию'), (3, 'Тариф запроса на субсидию'),
(4, 'Фактическое начисление'), (5, 'Субсидия'), (6, 'Характеристики жилья'), (7, 'Виды услуг'), (8, 'Форма-2 льгота'),
(9, 'Типы улиц запроса по льготам'), (10, 'Улицы запроса по льготам'), (11, 'Тарифы запроса по льготам');

-- Change request file type to number

ALTER TABLE `request_file` ADD COLUMN type0 INTEGER NOT NULL;

UPDATE `request_file` set type0 = 1 where type = 'BENEFIT';
UPDATE `request_file` set type0 = 2 where type = 'PAYMENT';
UPDATE `request_file` set type0 = 3 where type = 'SUBSIDY_TARIF';
UPDATE `request_file` set type0 = 4 where type = 'ACTUAL_PAYMENT';
UPDATE `request_file` set type0 = 5 where type = 'SUBSIDY';
UPDATE `request_file` set type0 = 6 where type = 'DWELLING_CHARACTERISTICS';
UPDATE `request_file` set type0 = 7 where type = 'FACILITY_SERVICE_TYPE';
UPDATE `request_file` set type0 = 8 where type = 'FACILITY_FORM2';
UPDATE `request_file` set type0 = 9 where type = 'FACILITY_STREET_TYPE';
UPDATE `request_file` set type0 = 10 where type = 'FACILITY_STREET';
UPDATE `request_file` set type0 = 11 where type = 'FACILITY_TARIF';

ALTER TABLE `request_file` DROP COLUMN type;

ALTER TABLE `request_file` CHANGE COLUMN type0 type INTEGER NOT NULL;

ALTER TABLE `request_file` ADD KEY `key_type` (`type`), ADD CONSTRAINT `fk_request_file__type_description`
  FOREIGN KEY (`type`) REFERENCES `type_description` (`code`);

-- Change Request Warning

ALTER TABLE `request_warning` ADD COLUMN type0 INTEGER NOT NULL;

UPDATE `request_warning` set type0 = 1 where request_file_type = 'BENEFIT';
UPDATE `request_warning` set type0 = 2 where request_file_type = 'PAYMENT';
UPDATE `request_warning` set type0 = 3 where request_file_type = 'SUBSIDY_TARIF';
UPDATE `request_warning` set type0 = 4 where request_file_type = 'ACTUAL_PAYMENT';
UPDATE `request_warning` set type0 = 5 where request_file_type = 'SUBSIDY';
UPDATE `request_warning` set type0 = 6 where request_file_type = 'DWELLING_CHARACTERISTICS';
UPDATE `request_warning` set type0 = 7 where request_file_type = 'FACILITY_SERVICE_TYPE';
UPDATE `request_warning` set type0 = 8 where request_file_type = 'FACILITY_FORM2';
UPDATE `request_warning` set type0 = 9 where request_file_type = 'FACILITY_STREET_TYPE';
UPDATE `request_warning` set type0 = 10 where request_file_type = 'FACILITY_STREET';
UPDATE `request_warning` set type0 = 11 where request_file_type = 'FACILITY_TARIF';

ALTER TABLE `request_warning` DROP COLUMN request_file_type;

ALTER TABLE `request_warning` CHANGE COLUMN type0 request_file_type INTEGER NOT NULL;

ALTER TABLE `request_warning` ADD KEY `key_type` (`request_file_type`), ADD CONSTRAINT `fk_request_warning__type_description`
  FOREIGN KEY (`request_file_type`) REFERENCES `type_description` (`code`);

-- Update DB version

INSERT INTO `update` (`version`) VALUE ('20130618_844_0.2.1');