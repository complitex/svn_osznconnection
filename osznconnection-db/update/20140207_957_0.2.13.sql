-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140207_957_0.2.13');

-- ------------------------------
-- Export subsidy directory. It is OSZN only attribute.
-- ------------------------------
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (930, 1, UPPER('Корневой каталог для экспорта файлов'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (930, 900, 0, 930, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (930, 930, UPPER('string'));