-- Script adds request_warning index.

ALTER TABLE `request_warning` ADD KEY `key_request_warning__request` (`request_id`), ADD KEY `key_request_warning__request_file` (`request_file_type`);

INSERT INTO `update` (`version`) VALUE ('20101214_438');

