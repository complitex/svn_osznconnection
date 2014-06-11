ALTER TABLE `person_account`
  ADD COLUMN `city` VARCHAR(100) COMMENT 'Населенный пункт',
  ADD COLUMN `street_type` VARCHAR(50) COMMENT 'Тип улицы',
  ADD COLUMN `street` VARCHAR(100) COMMENT 'Улица',
  ADD COLUMN `building_number` VARCHAR(20) COMMENT 'Номер дома',
  ADD COLUMN `building_corp` VARCHAR(20) COMMENT 'Корпус';

-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140611_1008_0.3.3');