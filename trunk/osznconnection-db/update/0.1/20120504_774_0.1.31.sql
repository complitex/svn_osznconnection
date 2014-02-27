-- Fixes privilege name attribute (id = 73)
-- Fixes privilege's code attributes and "itself" organization code attribute for using system locale.

UPDATE `organization_string_culture` SET `locale_id` = (SELECT `id` FROM `locales` WHERE `system` = 1)
	WHERE `id` = 2;
	
-- fixes name for privilege with id=73
UPDATE `privilege_string_culture` SET `id` = 145 WHERE `id` = 154 AND `locale_id` = (SELECT `id` FROM `locales` WHERE `system` = 0);
	
UPDATE `privilege_string_culture` SET `locale_id` = (SELECT `id` FROM `locales` WHERE `system` = 1)
	WHERE `id` IN (SELECT cod.`value_id` FROM `privilege_attribute` cod WHERE cod.attribute_type_id = 1201);

INSERT INTO `update` (`version`) VALUE ('20120504_774_0.1.31');