-- --------------------------------------------------------------------------------------------------
-- 'dev' keyword denote sql update script is in development process. Do not execute on real database.
-- --------------------------------------------------------------------------------------------------

-- New request status 'LOADED' has been introduced.
INSERT INTO `status_description`(`code`, `name`) VALUES (237, 'Загружена');
ALTER TABLE `payment` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 237 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';
ALTER TABLE `benefit` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 237 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';
ALTER TABLE `actual_payment` MODIFY COLUMN `status` INTEGER NOT NULL DEFAULT 237 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus';

INSERT INTO `update` (`version`) VALUE ('20110207_553_0.1.2');