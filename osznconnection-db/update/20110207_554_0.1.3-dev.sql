-- --------------------------------------------------------------------------------------------------
-- 'dev' keyword denote sql update script is in development process. Do not execute on real database.
-- --------------------------------------------------------------------------------------------------

ALTER TABLE `request_file_group` DROP COLUMN `permission_id`,
DROP KEY `key_permission_id`,
DROP FOREIGN KEY `fk_request_file_group__permission`;

ALTER TABLE `request_file` DROP COLUMN `permission_id`,
DROP KEY `key_permission_id`,
DROP FOREIGN KEY `fk_request_file__permission`;