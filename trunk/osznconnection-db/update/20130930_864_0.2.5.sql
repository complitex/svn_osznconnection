--
-- Add city and street type to street correction
--

ALTER TABLE `street_correction`
  ADD COLUMN `city_object_id` BIGINT(20),
  ADD COLUMN `street_type_object_id` BIGINT(20),
  ADD KEY `key_city_object_id` (`city_object_id`),
  ADD KEY  `key_street_type_object_id` (`street_type_object_id`),
  ADD CONSTRAINT `fk_street_correction__city` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`),
  ADD CONSTRAINT `fk_street_correction__street_type` FOREIGN KEY (`street_type_object_id`) REFERENCES `street_type` (`object_id`);

UPDATE `street_correction` sc
  LEFT JOIN `street` s ON (s.`object_id` = sc.`object_id`)
  LEFT JOIN `street_attribute` sa ON (sa.`object_id` = sc.`object_id` and sa.`value_type_id` = 301)
SET sc.`city_object_id` = s.`parent_id`, sc.`street_type_object_id` = sa.`value_id`;

-- lock not null
ALTER TABLE `street_correction`
  MODIFY COLUMN `city_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта',
  MODIFY COLUMN `street_type_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа улицы';

--
-- Add street to building_correction
--

ALTER TABLE `building_correction`
    ADD COLUMN `street_object_id` BIGINT(20),
    ADD KEY `key_street_object_id` (`street_object_id`),
    ADD CONSTRAINT `fk_building_correction__street` FOREIGN KEY (`street_object_id`) REFERENCES `street` (`object_id`);

UPDATE `building_correction` bc
  LEFT JOIN `building` b ON (bc.`object_id` = b.`object_id`)
  LEFT JOIN `building_address` ba ON (ba.`object_id` = b.`parent_id`)
SET bc.`street_object_id` = ba.`parent_id`;

ALTER TABLE `building_correction` MODIFY `street_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта улицы';

--
-- Add city to district_correction
--

ALTER TABLE `district_correction`
ADD COLUMN `city_object_id` BIGINT(20),
ADD KEY `key_city_object_id` (`city_object_id`),
ADD CONSTRAINT `fk_district_correction__street` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`);

UPDATE `district_correction` dc
  LEFT JOIN `district` d ON (d.`object_id` = dc.`object_id`)
SET dc.`city_object_id` = d.`parent_id`;

ALTER TABLE `district_correction` MODIFY `city_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта';

-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20130930_864_0.2.5');