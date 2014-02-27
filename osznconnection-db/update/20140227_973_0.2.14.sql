-- --------------------------------
-- Current database version
-- --------------------------------
INSERT INTO `update` (`version`) VALUE ('20140227_973_0.2.14');

-- --------------------------------
-- Add fields to subsidy_master_data
-- --------------------------------

ALTER TABLE subsidy_master_data
  ADD COLUMN  `VC` TINYINT(1) DEFAULT 0 COMMENT 'Всегда 0',
  ADD COLUMN `PLE` TINYINT(1) DEFAULT 0 COMMENT 'Всегда 0';

