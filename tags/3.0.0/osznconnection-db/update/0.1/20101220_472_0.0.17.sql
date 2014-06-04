-- Script adds PAYMENT_NOT_EXISTS request status for benefits for which associated payments don't exist.

INSERT INTO `status_description`(`code`, `name`) VALUES (220, 'Нет запроса оплаты');

INSERT INTO `update` (`version`) VALUE ('20101220_472');
