-- Adds MORE_ONE_ACCOUNTS_LOCALLY status.

INSERT INTO `status_description`(`code`, `name`) VALUES (242, 'Более одного л/с в таблице счетов абонентов');

INSERT INTO `update` (`version`) VALUE ('20120502_771_0.1.30');