-- New request status 'ACCOUNT_NUMBER_MISMATCH' was introduced.
INSERT INTO `status_description`(`code`, `name`) VALUES (241, 'Несоответствие номера л/с'),
(308, 'Номер л/с ЖЭКа в МН не соответствует шаблону: <номер ЖЭКа>.<номер л/с ЖЭКа>.');

INSERT INTO `update` (`version`) VALUES ('20110510_673_0.1.15');