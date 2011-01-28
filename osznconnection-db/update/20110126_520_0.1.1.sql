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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `permission` (`permission_id`, `table`, `entity`, `object_id`) value (0, 'ALL', 'ALL', 0);

ALTER TABLE `user`
  ADD COLUMN `organization_object_id` BIGINT(20),
  ADD KEY `key_organization_object_id` (`organization_object_id`),
  ADD CONSTRAINT `fk_user__organization` FOREIGN KEY (`organization_object_id`) REFERENCES `organization` (`object_id`);

INSERT INTO `update` (`version`) VALUE ('20110126_520_0.1.1');