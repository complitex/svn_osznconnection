-- ------------------------------
-- Permission
-- ------------------------------

CREATE TABLE `permission` (
    `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `permission_id` BIGINT(20) NOT NULL,
    `table` VARCHAR(64) NOT NULL,
    `entity` VARCHAR(64) NOT NULL,
    `object_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`pk_id`),
    UNIQUE KEY `key_unique` (`permission_id`, `entity`, `object_id`),
    KEY `key_permission_id` (`permission_id`),
    KEY `key_table` (`table`),
    KEY `key_entity` (`entity`),
    KEY `key_object_id` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `permission` (`permission_id`, `table`, `entity`, `object_id`) VALUE (0, 'ALL', 'ALL', 0);
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('permission', 1);

CREATE TABLE `user_organization` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(20) NOT NULL,
    `organization_object_id` BIGINT(20) NOT NULL,
    `main` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_unique` (`user_id`, `organization_object_id`),
    CONSTRAINT `fk_user_organization__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_organization__organization` FOREIGN KEY (`organization_object_id`)
      REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (907, 1, UPPER('Принадлежит')), (907, 2, UPPER('Принадлежит'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (903, 900, 0, 907, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (903, 903, 'organization');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (906, 1, UPPER('Организации пользователей')), (906, 2, UPPER('Организации пользователей'));
INSERT INTO `entity_type`(`id`, `entity_id`, `entity_type_name_id`) VALUES (902, 900, 906);
  
ALTER TABLE `apartment` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_apartment__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `room` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_room__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `street` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_street__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);
  
ALTER TABLE `street_type` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_street_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `city` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_city__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `city_type` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_city_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `building_address` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_building_address__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `building` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_building__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `district` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_district__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `region` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_region__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `country` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_country__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `organization` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_organization__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `ownership` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_ownership__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `privilege` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_privilege__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `user_info` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`), 
ADD CONSTRAINT `fk_user_info__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `request_file` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`),
ADD CONSTRAINT `fk_request_file__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

ALTER TABLE `request_file_group` ADD COLUMN `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
ADD KEY `key_permission_id` (`permission_id`),
ADD CONSTRAINT `fk_request_file_group__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`);

INSERT INTO `update` (`version`) VALUE ('20110126_520_0.1.1');