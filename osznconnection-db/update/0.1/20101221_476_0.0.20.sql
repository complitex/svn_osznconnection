-- Script corrects some status descriptions.

UPDATE `status_description` SET `name` = 'Объект формы собственности не найден в справочнике соответствий для МН' WHERE `code` = 301;
UPDATE `status_description` SET `name` = 'Код формы собственности не найден в справочнике соответствий для ОСЗН' WHERE `code` = 302;
UPDATE `status_description` SET `name` = 'Нечисловой код формы собственности в справочнике соответствий для ОСЗН' WHERE `code` = 303;
UPDATE `status_description` SET `name` = 'Объект льготы не найден в справочнике соответствий для МН' WHERE `code` = 304;
UPDATE `status_description` SET `name` = 'Код льготы не найден в справочнике соответствий для ОСЗН' WHERE `code` = 305;
UPDATE `status_description` SET `name` = 'Нечисловой код льготы в справочнике соответствий для ОСЗН' WHERE `code` = 306;
UPDATE `status_description` SET `name` = 'Льгота не найдена в справочнике соответствий' WHERE `code` = 218;

INSERT INTO `update` (`version`) VALUE ('20101221_476');
