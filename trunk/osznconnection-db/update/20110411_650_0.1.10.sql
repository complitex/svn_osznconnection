-- Fixes unique key in person_account table.
ALTER TABLE `person_account` DROP KEY `uk_person_account`,
    ADD UNIQUE KEY `uk_person_account` (`first_name`,`middle_name`,`last_name`,`city`,`street`,`building_num`,`building_corp`,`apartment`,`oszn_id`,`calc_center_id`);

INSERT INTO `update` (`version`) VALUE ('20110411_650_0.1.10');

