-- Script corrects calculation center's related i18n messages.

UPDATE `status_description` SET `name` = 'Неизвестный населенный пункт' WHERE `code` = 200;
UPDATE `status_description` SET `name` = 'Неизвестная улица' WHERE `code` = 201;
UPDATE `status_description` SET `name` = 'Неизвестный номер дома' WHERE `code` = 202;
UPDATE `status_description` SET `name` = 'Населенный пункт не найден в соответствиях МН' WHERE `code` = 205;
UPDATE `status_description` SET `name` = 'Район не найден в соответствиях МН' WHERE `code` = 206;
UPDATE `status_description` SET `name` = 'Тип улицы не найден в соответствиях МН' WHERE `code` = 207;
UPDATE `status_description` SET `name` = 'Улица не найдена в соответствиях МН' WHERE `code` = 208;
UPDATE `status_description` SET `name` = 'Дом не найден в соответствиях МН' WHERE `code` = 209;
UPDATE `status_description` SET `name` = 'Населенный пункт не найден в МН' WHERE `code` = 221;
UPDATE `status_description` SET `name` = 'Район не найден в МН' WHERE `code` = 222;
UPDATE `status_description` SET `name` = 'Тип улицы не найден в МН' WHERE `code` = 223;
UPDATE `status_description` SET `name` = 'Улица не найдена в МН' WHERE `code` = 224;
UPDATE `status_description` SET `name` = 'Дом не найден в МН' WHERE `code` = 225;
UPDATE `status_description` SET `name` = 'Корпус дома не найден в МН' WHERE `code` = 226;
UPDATE `status_description` SET `name` = 'Квартира не найдена в МН' WHERE `code` = 227;
UPDATE `status_description` SET `name` = 'Объект формы собственности не найден в таблице коррекций для МН' WHERE `code` = 301;
UPDATE `status_description` SET `name` = 'Нечисловой код формы собственности в коррекции для ОСЗН' WHERE `code` = 303;
UPDATE `status_description` SET `name` = 'Объект льготы не найден в таблице коррекций для МН' WHERE `code` = 304;
UPDATE `status_description` SET `name` = 'Нечисловой код льготы в коррекции для ОСЗН' WHERE `code` = 306;
UPDATE `status_description` SET `name` = 'Нечисловой порядок льготы' WHERE `code` = 307;

UPDATE `string_culture` SET `value` = UPPER('Модуль начислений') WHERE `id` = 905 AND `locale_id` = 1;

INSERT INTO `update` (`version`) VALUE ('20101221_475');
