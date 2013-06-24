-- City

ALTER TABLE `city_correction`
  DROP FOREIGN KEY `fk_city_correction__internal_organization`,
  DROP KEY `uk_city_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `city_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `city_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `city_correction`
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- City Type

ALTER TABLE `city_type_correction`
  DROP FOREIGN KEY `fk_city_type_correction__internal_organization`,
  DROP KEY `uk_city_type_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `city_type_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `city_type_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `city_type_correction`
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- District

ALTER TABLE `district_correction`
  DROP FOREIGN KEY `fk_district_correction__internal_organization`,
  DROP KEY `uk_district_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `district_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `district_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `district_correction`
  ADD KEY `key_parent_id` (`parent_id`),
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- Street Type

ALTER TABLE `street_type_correction`
  DROP FOREIGN KEY `fk_street_type_correction__internal_organization`,
  DROP KEY `uk_street_type_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `street_type_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `street_type_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `street_type_correction`
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- Street

ALTER TABLE `street_correction`
  DROP FOREIGN KEY `fk_street_correction__internal_organization`,
  DROP KEY `uk_street_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `street_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `street_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `street_correction`
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- Building

ALTER TABLE `building_correction`
  DROP FOREIGN KEY `fk_building_correction__internal_organization`,
  DROP KEY `uk_building_correction`,
  DROP KEY `key_internal_organization_id`;

ALTER TABLE `building_correction`
  CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
  CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `building_correction`
  ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
  ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
  ADD COLUMN `status` INTEGER;

ALTER TABLE `building_correction`
  ADD KEY `key_begin_date` (`begin_date`),
  ADD KEY `key_end_date` (`end_date`),
  ADD KEY `key_external_id` (`external_id`),
  ADD KEY `key_status` (`status`);

-- Change external_id to String

ALTER TABLE `apartment` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `street` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `street_type` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `city` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `city_type` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `building_address` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `building` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `district` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `region` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `country` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `organization_type` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);
ALTER TABLE `organization` CHANGE COLUMN `external_id` `external_id` VARCHAR(20);

-- Change skipped status to 100

UPDATE `request_file` SET `status` = 100 where `status` = 10;


-- Update DB version

INSERT INTO `update` (`version`) VALUE ('20130619_845_0.2.2');