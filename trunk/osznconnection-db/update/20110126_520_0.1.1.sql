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
    KEY `key_permission_id` (`permission_id`),
    KEY `key_table` (`table`),
    KEY `key_entity` (`entity`),
    KEY `key_object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `update` (`version`) VALUE ('20110126_520_0.1.1');