-- Script changes 'organization type' attribute type id from 905 to 904.

INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (904, 900, 0, 905, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (904, 904, 'organization_type');
UPDATE `organization_attribute` SET `attribute_type_id` = 904, `value_type_id` = 904 WHERE `attribute_type_id` = 905;
DELETE FROM `entity_attribute_value_type` WHERE `id` = 905;
DELETE FROM `entity_attribute_type` WHERE `id` = 905;

INSERT INTO `update` (`version`) VALUE ('20120322_738_0.1.25');



