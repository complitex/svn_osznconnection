INSERT INTO `service_provider_type`(`object_id`) VALUES (2),(3),(4),(5),(6),(8);
INSERT INTO `service_provider_type_string_culture`(`id`, `locale_id`, `value`) VALUES
  (2, 1, UPPER('отопление / уголь')), (2, 2, UPPER('опалення / вугілля')),
  (3, 1, UPPER('горячая вода / дрова')), (3, 2, UPPER('гаряча вода/дрова')),
  (4, 1, UPPER('холодная вода')), (4, 2, UPPER('холодна вода')),
  (5, 1, UPPER('газ')), (5, 2, UPPER('газ')),
  (6, 1, UPPER('электроэнергия')), (6, 2, UPPER('електроенергія')),
  (8, 1, UPPER('водоотведение')), (8, 2, UPPER('вивіз нечистот'));

INSERT INTO `service_provider_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`)
VALUES (1,2,1600,2,1600),(1,3,1600,3,1600),(1,4,1600,4,1600),(1,5,1600,5,1600),(1,6,1600,6,1600), (1,8,1600,8,1600);

-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20131204_885_0.2.9');