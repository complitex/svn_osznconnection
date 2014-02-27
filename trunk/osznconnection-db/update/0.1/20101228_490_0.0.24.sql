-- Script deletes out of date tmp_* tables.

DROP TABLE `tmp_streets`;
DROP TABLE `tmp_street_types`;
DROP TABLE `tmp_houses`;
DROP TABLE `tmp_districts`;

INSERT INTO `update` (`version`) VALUE ('20101228_490');

