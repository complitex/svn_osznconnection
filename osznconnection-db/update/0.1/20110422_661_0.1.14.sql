-- Deletes dublicate street corrections

CREATE TABLE `dublicate_street_correction`(
	`id` BIGINT(20) NOT NULL
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO dublicate_street_correction SELECT s1.`id` FROM `street_correction` s1 
		JOIN `street_correction` s2 ON (s1.`parent_id` = s2.`parent_id` AND 
		TRIM(TO_CYRILLIC(s1.`correction`)) = TRIM(TO_CYRILLIC(s2.`correction`)) 
		AND s1.`street_type_correction_id` = s2.`street_type_correction_id` AND s1.`organization_id` = s2.`organization_id`
		AND s1.`id` < s2.`id`);
		
DELETE FROM `building_correction` WHERE `parent_id` IN (SELECT d.`id` FROM `dublicate_street_correction` d);
DELETE FROM `street_correction` WHERE `id` IN (SELECT d.`id` FROM `dublicate_street_correction` d);

DROP TABLE `dublicate_street_correction`;

INSERT INTO `update` (`version`) VALUE ('20110422_661_0.1.14');