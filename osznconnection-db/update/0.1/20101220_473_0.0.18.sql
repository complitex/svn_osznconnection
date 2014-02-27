-- Script separates request statuses to differentiate not found information and unresolved. Also staled status 
-- APARTMENT_UNRESOLVED_LOCALLY was discarded.

DELETE FROM `status_description` WHERE `code` IN (203, 210, 211);

UPDATE `status_description` SET `name` = 'Населенный пункт не найден в соответствиях ц.н.' WHERE `code` = 205;
UPDATE `status_description` SET `name` = 'Район не найден в соответствиях ц.н.' WHERE `code` = 206;
UPDATE `status_description` SET `name` = 'Тип улицы не найден в соответствиях ц.н.' WHERE `code` = 207;
UPDATE `status_description` SET `name` = 'Улица не найдена в соответствиях ц.н.' WHERE `code` = 208;
UPDATE `status_description` SET `name` = 'Дом не найден в соответствиях ц.н.' WHERE `code` = 209;
INSERT INTO `status_description`(`code`, `name`) VALUES (221, 'Населенный пункт не найден в ц.н.'), (222, 'Район не найден в ц.н.'), 
(223, 'Тип улицы не найден в ц.н.'), (224, 'Улица не найдена в ц.н.'), (225, 'Дом не найден в ц.н.'), 
(226, 'Корпус дома не найден в ц.н.'), (227, 'Квартира не найдена в ц.н.');

UPDATE `payment` SET `status` = 226 WHERE `status` = 210;
UPDATE `benefit` SET `status` = 226 WHERE `status` = 210;
UPDATE `payment` SET `status` = 227 WHERE `status` = 211;
UPDATE `benefit` SET `status` = 227 WHERE `status` = 211;

INSERT INTO `update` (`version`) VALUE ('20101220_473');
