ALTER TABLE `person_account`
    DROP COLUMN `city`,
    DROP COLUMN `street_type`,
    DROP COLUMN `street`,
    DROP COLUMN `building_num`,
    DROP COLUMN `building_corp`;

ALTER TABLE `person_account`
  MODIFY COLUMN `apartment` VARCHAR(20) COMMENT 'Номер квартиры';

-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140609_1006_0.3.2');