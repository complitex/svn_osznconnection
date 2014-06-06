-- add address object id to person account

ALTER TABLE `person_account` DROP FOREIGN KEY `fk_person_account__osnz_organization`;

ALTER TABLE `person_account`
  DROP KEY `uk_person_account`,
  DROP KEY `key_oszn_id`;

ALTER TABLE `person_account` CHANGE COLUMN `oszn_id` `organization_id` BIGINT(20) NOT NULL
  COMMENT 'Идентификатор отдела соц. защиты населения';

ALTER TABLE `person_account`
  ADD COLUMN `city_object_id` BIGINT(20) COMMENT 'Идентификатор населенного пункта',
  ADD COLUMN `street_object_id` BIGINT(20) COMMENT 'Идентификатор улицы',
  ADD COLUMN `street_type_object_id` BIGINT(20) COMMENT 'Идентификатор типа улицы',
  ADD COLUMN `building_object_id` BIGINT(20) COMMENT 'Идентификатор дома',
  ADD COLUMN `apartment_object_id` BIGINT (20) COMMENT 'Идентификатор квартиры';

ALTER TABLE `person_account`
  ADD KEY `key_city_object_id` (`city_object_id`),
  ADD KEY `key_street_object_id` (`street_object_id`),
  ADD KEY `key_street_type_object_id` (`street_type_object_id`),
  ADD KEY `key_building_object_id` (`building_object_id`),
  ADD KEY `key_apartment_object_id` (`apartment_object_id`),

  ADD KEY `key_organization_id` (`organization_id`);

ALTER TABLE `person_account`
  ADD CONSTRAINT `fk_person_account__city` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`),
  ADD CONSTRAINT `fk_person_account__street` FOREIGN KEY (`street_object_id`) REFERENCES `street` (`object_id`),
  ADD CONSTRAINT `fk_person_account__street_type` FOREIGN KEY (`street_type_object_id`) REFERENCES `street_type` (`object_id`),
  ADD CONSTRAINT `fk_person_account__building` FOREIGN KEY (`building_object_id`) REFERENCES `building` (`object_id`),
  ADD CONSTRAINT `fk_person_account__apartment` FOREIGN KEY (`apartment_object_id`) REFERENCES `apartment` (`object_id`),

  ADD CONSTRAINT `fk_person_account__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`);

ALTER TABLE `person_account` ADD UNIQUE KEY `unique_person_account` (`first_name`, `middle_name`, `last_name`,
  `city_object_id`, `street_object_id`, `building_object_id`, `apartment_object_id`,`pu_account_number`,
  `organization_id`, `user_organization_id`, `calc_center_id`);

-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140602_1002_0.3.1');