-- Street type and city type attribute labels were changed.

UPDATE `string_culture` SET `value` = UPPER('Краткое название') WHERE `id` = 1401;
UPDATE `string_culture` SET `value` = UPPER('Название') WHERE `id` = 1402;
UPDATE `string_culture` SET `value` = UPPER('Краткое название') WHERE `id` = 1301;
UPDATE `string_culture` SET `value` = UPPER('Название') WHERE `id` = 1302;

INSERT INTO `update` (`version`) VALUE ('20110513_677_0.1.17');