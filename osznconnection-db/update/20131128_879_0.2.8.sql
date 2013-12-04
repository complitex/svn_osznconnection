CREATE TABLE `organization_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта организация',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор организации',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Код организации',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_organization__correction__organization_object` FOREIGN KEY (`object_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_organization__correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_organization__correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция организации';

-- Update DB version
INSERT INTO `update` (`version`) VALUE ('20131128_879_0.2.8');