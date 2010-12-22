-- Script divides invalid format status on two - PROCESSING_INVALID_FORMAT and BINDING_INVALID_FORMAT for related tasks.

UPDATE `status_description` SET `name` = 'Неверный формат данных на этапе обработки' WHERE `code` = 219;
INSERT INTO `status_description`(`name`, `code`) VALUES ('Неверный формат данных на этапе связывания', 203);

INSERT INTO `update` (`version`) VALUE ('20101222_480');
