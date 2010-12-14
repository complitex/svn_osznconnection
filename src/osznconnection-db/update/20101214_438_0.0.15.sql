-- Script adds request_warning index.

ALTER TABLE `request_warning` ADD UNIQUE KEY `uk_request_warning` (`request_id`, `request_file_type`);

INSERT INTO `update` (`version`) VALUE ('20101214_438');

