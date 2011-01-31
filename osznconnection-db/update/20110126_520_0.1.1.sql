-- ------------------------------
-- Permission
-- ------------------------------

CREATE TABLE `permission` (
    `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `permission_id` BIGINT(20) NOT NULL,
    `table` VARCHAR(64) NOT NULL,
    `entity` VARCHAR(64) NOT NULL,
    `object_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_unique` (`permission_id`, `entity`, `object_id`),
    KEY `key_permission_id` (`permission_id`),
    KEY `key_table` (`table`),
    KEY `key_entity` (`entity`),
    KEY `key_object_id` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `permission` (`permission_id`, `table`, `entity`, `object_id`) VALUE (0, 'ALL', 'ALL', 0);

ALTER TABLE `user`
  ADD COLUMN `organization_object_id` BIGINT(20),
  ADD KEY `key_organization_object_id` (`organization_object_id`),
  ADD CONSTRAINT `fk_user__organization` FOREIGN KEY (`organization_object_id`) REFERENCES `organization` (`object_id`);
  
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

INSERT INTO `update` (`version`) VALUE ('20110126_520_0.1.1');