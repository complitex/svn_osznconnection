--
-- Change privilege and ownership correction table
--

-- Privilege

ALTER TABLE `privilege_correction`
DROP COLUMN `parent_id`,
DROP FOREIGN KEY `fk_privilege_correction__internal_organization`,
DROP KEY `uk_privilege_correction`,
DROP KEY `key_internal_organization_id`;

ALTER TABLE `privilege_correction`
CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `privilege_correction`
ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
ADD COLUMN `status` INTEGER;

ALTER TABLE `privilege_correction`
ADD KEY `key_begin_date` (`begin_date`),
ADD KEY `key_end_date` (`end_date`),
ADD KEY `key_external_id` (`external_id`),
ADD KEY `key_status` (`status`);

-- Ownership

ALTER TABLE `ownership_correction`
DROP COLUMN `parent_id`,
DROP FOREIGN KEY `fk_ownership_correction__internal_organization`,
DROP KEY `uk_ownership_correction`,
DROP KEY `key_internal_organization_id`;

ALTER TABLE `ownership_correction`
CHANGE COLUMN `organization_code` `external_id` VARCHAR(20),
CHANGE COLUMN `internal_organization_id` `module_id` BIGINT(20) NOT NULL;

ALTER TABLE `ownership_correction`
ADD COLUMN `begin_date` DATE NOT NULL DEFAULT '1970-01-01',
ADD COLUMN `end_date` DATE NOT NULL DEFAULT '2054-12-31',
ADD COLUMN `status` INTEGER;

ALTER TABLE `ownership_correction`
ADD KEY `key_begin_date` (`begin_date`),
ADD KEY `key_end_date` (`end_date`),
ADD KEY `key_external_id` (`external_id`),
ADD KEY `key_status` (`status`);

--
-- Remove parent correction
--

ALTER TABLE `city_correction` DROP COLUMN `parent_id`;
ALTER TABLE `city_type_correction` DROP COLUMN `parent_id`;
ALTER TABLE `district_correction` DROP FOREIGN KEY `fk_district_correction__city_correction`, DROP COLUMN `parent_id`;
ALTER TABLE `street_type_correction` DROP COLUMN `parent_id`;
ALTER TABLE `street_correction` DROP FOREIGN KEY `fk_street_correction__city_correction`, DROP COLUMN `parent_id`,
  DROP FOREIGN KEY `fk_street_correction__street_type_correction`, DROP COLUMN `street_type_correction_id`;
ALTER TABLE `building_correction` DROP FOREIGN KEY `fk_building_correction__street_correction`,DROP COLUMN `parent_id`;

--
-- Remove duplicate correction
--

-- City

delete c from `city_correction` c
  left join `city_attribute` a on a.`object_id` = c.`object_id`
  left join `city_string_culture` sc on sc.`id` = a.`value_id`
where sc.`locale_id` = (select l.`id` from `locales` l where l.`system` = true) and c.`correction` = sc.`value`
  and a.attribute_type_id = 400;

-- Street Type

delete c from `street_type_correction` c
  left join `street_type_attribute` a on a.`object_id` = c.`object_id`
  left join `street_type_string_culture` sc on sc.`id` = a.`value_id`
where sc.`locale_id` = (select l.`id` from `locales` l where l.`system` = true) and c.`correction` = sc.`value`
      and a.attribute_type_id = 1400;

-- Street

delete c from `street_correction` c
  left join `street_attribute` a on a.`object_id` = c.`object_id`
  left join `street_string_culture` sc on sc.`id` = a.`value_id`
where sc.`locale_id` = (select l.`id` from `locales` l where l.`system` = true) and c.`correction` = sc.`value`
      and a.attribute_type_id = 300;

-- Building Address

delete c from `building_correction` c
  left join `building` b on b.`object_id` = c.`object_id`
  left join `building_address` ba on ba.`object_id` = b.`parent_id`
  left join `building_address_attribute` a on a.`object_id` = ba.`object_id`
  left join `building_address_string_culture` sc_n on (sc_n.`id` = a.`value_id` and a.`attribute_type_id` = 1500)
  left join `building_address_string_culture` sc_c on (sc_c.`id` = a.`value_id` and a.`attribute_type_id` = 1501)
where sc_n.`locale_id` = (select l.`id` from `locales` l where l.`system` = true) and (sc_c.`locale_id` = sc_n.`locale_id` or sc_c.`id` is null)
  and c.`correction` = sc_n.`value` and (c.`correction_corp` = sc_c.`value` or (c.`correction_corp` = '' and sc_c.`value` is null));

--
-- Remove street and street type correction
--

alter table `dwelling_characteristics` drop column `street_correction_id`,
    drop column `street`, drop column `street_type`;

alter table `facility_service_type` drop column `street_correction_id`,
drop column `street`, drop column `street_type`;

-- Update DB version

INSERT INTO `update` (`version`) VALUE ('20130704_850_0.2.3');