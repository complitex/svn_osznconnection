-- Adds support for actual payments.
ALTER TABLE `person_account` ADD KEY `key_oszn_id` (`oszn_id`), ADD KEY `key_calc_center_id` (`calc_center_id`),
        ADD CONSTRAINT `fk_person_account__osnz_organization` FOREIGN KEY (`oszn_id`) REFERENCES `organization` (`object_id`),
        ADD CONSTRAINT `fk_person_account__calc_center_organization` FOREIGN KEY (`calc_center_id`) REFERENCES `organization` (`object_id`);

INSERT INTO `update` (`version`) VALUE ('20110321_629_0.1.7');

