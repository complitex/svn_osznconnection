-- --------------------------------
-- Users
-- --------------------------------
INSERT INTO USER (`id`, `login`, `password`) VALUE (1, 'admin', '21232f297a57a5a743894a0e4a801fc3');
INSERT INTO usergroup (`id`, `login`, `group_name`) VALUE (1, 'admin', 'ADMINISTRATORS');
INSERT INTO USER (`id`, `login`, `password`)  VALUE (2, 'ANONYMOUS', 'ANONYMOUS');

-- --------------------------------
-- Locale
-- --------------------------------

INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (1, 'ru', 1);
INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (2, 'uk', 0);

-- --------------------------------
-- Sequence
-- --------------------------------

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES
('string_culture',1),
('apartment',1), ('apartment_string_culture',1),
('building',1), ('building_string_culture',1),
('building_address',1), ('building_address_string_culture',1),
('country',1), ('country_string_culture',1),
('district',1), ('district_string_culture',1),
('city',1), ('city_string_culture',1),
('city_type',1), ('city_type_string_culture',1),
('region',1), ('region_string_culture',1),
('room',1), ('room_string_culture',1),
('street',1), ('street_string_culture',1),
('street_type',1), ('street_type_string_culture',1),
('organization',1), ('organization_string_culture',1),
('user_info', 1), ('user_info_string_culture', 1),
('ownership',1), ('ownership_string_culture',1),
('privilege',1), ('privilege_string_culture',1);

--Permission
INSERT INTO `permission` (`permission_id`, `table`, `entity`, `object_id`) VALUE (0, 'ALL', 'ALL', 0);
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('permission', 1);

-- --------------------------------
-- Apartment
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (100, 1, 'Квартира'), (100, 2, 'Квартира');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (100, 'apartment', 100, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (101, 1, UPPER('Наименование квартиры')), (101, 2, UPPER('Найменування квартири'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (100, 100, 1, 101, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (100, 100, UPPER('string_culture'));

-- --------------------------------
-- Room
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (200, 1, 'Комната'), (200, 2, 'Кімната');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (200, 'room', 200, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (201, 1, UPPER('Наименование комнаты')), (201, 2, UPPER('Найменування кімнати'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (200, 200, 1, 201, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (200, 200, UPPER('string_culture'));

-- --------------------------------
-- Street
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (300, 1, 'Улица'), (300, 2, 'Вулиця');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (300, 'street', 300, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (301, 1, UPPER('Наименование улицы')), (301, 2, UPPER('Найменування вулиці'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (300, 300, 1, 301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (300, 300, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (302, 1, UPPER('Тип улицы')),(302, 2, UPPER('Тип улицы'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (301, 300, 1, 302, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (301, 301, 'street_type');

-- --------------------------------
-- Street Type
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1400, 1, 'Тип улицы'), (1400, 2, 'Тип улицы');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1400, 'street_type', 1400, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1401, 1, UPPER('Название')), (1401, 2, UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1400, 1400, 1, 1401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1400, 1400, UPPER('string_culture'));

-- --------------------------------
-- City
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (400, 1, 'Населенный пункт'), (400, 2, 'Населений пункт');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (400, 'city', 400, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (401, 1, UPPER('Наименование населенного пункта')), (401, 2, UPPER('Найменування населеного пункту'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (400, 400, 1, 401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (400, 400, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (402, 1, UPPER('Тип населенного пункта')), (402, 2, UPPER('Тип населенного пункта'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (401, 400, 1, 402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (401, 401, 'city_type');

-- --------------------------------
-- City Type
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1300, 1, 'Тип нас. пункта'), (1300, 2, 'Тип населенного пункта');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1300, 'city_type', 1300, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1301, 1, UPPER('Название')), (1301, 2, UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1300, 1300, 1, 1301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1300, 1300, UPPER('string_culture'));

-- --------------------------------
-- Building
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (500, 1, 'Дом'), (500, 2, 'Будинок');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (500, 'building', 500, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (501, 1, UPPER('Район')), (501, 2, UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (500, 500, 0, 501, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (500, 500, 'district');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (502, 1, UPPER('Альтернативный адрес')), (502, 2, UPPER('Альтернативный адрес'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (501, 500, 0, 502, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (501, 501, 'building_address');


-- --------------------------------
-- Building Address
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1500, 1, 'Адрес здания'), (1500, 2, 'Адрес здания');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1500, 'building_address', 1500, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1501, 1, UPPER('Номер дома')), (1501, 2, UPPER('Номер будинку'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1500, 1500, 1, 1501, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1502, 1, UPPER('Корпус')), (1502, 2, UPPER('Корпус'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1501, 1500, 0, 1502, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1503, 1, UPPER('Строение')), (1503, 2, UPPER('Будова'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1502, 1500, 0, 1503, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1500, 1500, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1501, 1501, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1502, 1502, UPPER('string_culture'));

-- --------------------------------
-- District
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (600, 1, 'Район'), (600, 2, 'Район');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (600, 'district', 600, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (601, 1, UPPER('Наименование района')), (601, 2, UPPER('Найменування району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (600, 600, 1, 601, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (600, 600, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (602, 1, UPPER('Код района')), (602, 2, UPPER('Код району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (601, 600, 1, 602, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (601, 601, UPPER('string'));

-- --------------------------------
-- Region
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (700, 1, 'Регион'), (700, 2, 'Регіон');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (700, 'region', 700, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (701, 1, UPPER('Наименование региона')), (701, 2, UPPER('Найменування регіону'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (700, 700, 1, 701, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (700, 700, UPPER('string_culture'));

-- --------------------------------
-- Country
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (800, 1, 'Страна'), (800, 2, 'Країна');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (800, 'country', 800, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (801, 1, UPPER('Наименование страны')), (801, 2, UPPER('Найменування країни'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (800, 800, 1, 801, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (800, 800, UPPER('string_culture'));

-- --------------------------------
-- Organization
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (900, 1, 'Организация'), (900, 2, 'Організація');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (900, 'organization', 900, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (901, 1, UPPER('Наименование организации')), (901, 2, UPPER('Найменування організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (900, 900, 1, 901, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (902, 1, UPPER('Уникальный код организации')), (902, 2, UPPER('Унікальний код організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (901, 900, 1, 902, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (903, 1, UPPER('Район')), (903, 2, UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (902, 900, 0, 903, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (907, 1, UPPER('Принадлежит')), (907, 2, UPPER('Принадлежит'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (903, 900, 0, 907, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (900, 900, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (901, 901, UPPER('string'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (902, 902, 'district');
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (903, 903, 'organization');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES
(904, 1, UPPER('ОСЗН')), (904, 2, UPPER('ОСЗН')),
(905, 1, UPPER('Модуль начислений')), (905, 2, UPPER('Центр нарахувань')),
(906, 1, UPPER('Организации пользователей')), (906, 2, UPPER('Организации пользователей'));
INSERT INTO `entity_type`(`id`, `entity_id`, `entity_type_name_id`) VALUES (900, 900, 904), (901, 900, 905), (902, 900, 906);

-- --------------------------------
-- User
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1000, 1, 'Пользователь'), (1000, 2, 'Користувач');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1000, 'user_info', 1000, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1001, 1, UPPER('Фамилия')), (1001, 2, UPPER('Прізвище'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1000, 1000, 1, 1001, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1000, 1000, 'last_name');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1002, 1, UPPER('Имя')), (1002, 2, UPPER('Ім\'я'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1001, 1000, 1, 1002, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1001, 1001, 'first_name');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1003, 1, UPPER('Отчество')), (1003, 2, UPPER('По батькові'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1002, 1000, 1, 1003, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1002, 1002, 'middle_name');

-- --------------------------------
-- Ownership
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1100, 1, 'Форма собственности'), (1100, 2, 'Форма власності');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1100, 'ownership', 1100, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1101, 1, UPPER('Название')), (1101, 2, UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1100, 1100, 1, 1101, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1100, 1100, UPPER('string_culture'));

-- Forms of ownerships
INSERT INTO `ownership`(`object_id`) VALUES (1),(2),(3),(4),(5),(6);
INSERT INTO `ownership_string_culture`(`id`, `locale_id`, `value`) VALUES (1, 1, UPPER('мiсцевих Рад')), (1, 2,UPPER('мiсцевих Рад')),
(2, 1, UPPER('кооперативна')), (2, 2, UPPER('кооперативна')), (3, 1, UPPER('вiдомча')), (3, 2, UPPER('вiдомча')),
(4, 1, UPPER('громадська')), (4, 2, UPPER('громадська')), (5, 1, UPPER('приватна')), (5, 2, UPPER('приватна')),
(6, 1, UPPER('приватизована')), (6, 2, UPPER('приватизована'));
INSERT INTO `ownership_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,1,1100,1,1100),(1,2,1100,2,1100),(1,3,1100,3,1100),(1,4,1100,4,1100),(1,5,1100,5,1100),(1,6,1100,6,1100);

-- --------------------------------
-- Privilege
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1200, 1, 'Льгота'), (1200, 2, 'Привілей');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1200, 'privilege', 1200, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1201, 1, UPPER('Название')), (1201, 2, UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1200, 1200, 1, 1201, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1200, 1200, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1202, 1, UPPER('Код')), (1202, 2, UPPER('Код'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1201, 1200, 1, 1202, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1201, 1201, UPPER('string'));

-- Privileges
INSERT INTO `privilege`(`object_id`) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),
(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),(101),(102),(103),(104);
INSERT INTO `privilege_string_culture`(`id`, `locale_id`, `value`) VALUES
(1,1,UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')), (1,2,UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')), (2,1,UPPER('1')),
(3,1,UPPER('УЧАСТНИК ВОЙНЫ')), (3,2,UPPER('УЧАСТНИК ВОЙНЫ')), (4,1,UPPER('2')),
(5,1,UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')), (5,2,UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')), (6,1,UPPER('3')),
(7,1,UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')), (7,2,UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')), (8,1,UPPER('11')),
(9,1,UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')), (9,2,UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')), (10,1,UPPER('12')),
(11,1,UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')), (11,2,UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')), (12,1,UPPER('13')),
(13,1,UPPER('РЕБЕНОК ВОЙНЫ')), (13,2,UPPER('РЕБЕНОК ВОЙНЫ')), (14,1,UPPER('15')),
(15,1,UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')), (15,2,UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')), (16,1,UPPER('20')),
(17,1,UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (17,2,UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (18,1,UPPER('22')),
(19,1,UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (19,2,UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (20,1,UPPER('23')),
(21,1,UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (21,2,UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (22,1,UPPER('26')),
(23,1,UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (23,2,UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (24,1,UPPER('30')),
(25,1,UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (25,2,UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (26,1,UPPER('32')),
(27,1,UPPER('ВЕТЕРАН ТРУДА')), (27,2,UPPER('ВЕТЕРАН ТРУДА')), (28,1,UPPER('33')),
(29,1,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')), (29,2,UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')), (30,1,UPPER('34')),
(31,1,UPPER('МНОГОДЕТНЫЕ СЕМЬИ')), (31,2,UPPER('МНОГОДЕТНЫЕ СЕМЬИ')), (32,1,UPPER('35')),
(33,1,UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')), (33,2,UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')), (34,1,UPPER('36')),
(35,1,UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')), (35,2,UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')), (36,1,UPPER('37')),
(37,1,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')), (37,2,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')), (38,1,UPPER('39')),
(39,1,UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')), (39,2,UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')), (40,1,UPPER('40')),
(41,1,UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')), (41,2,UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')), (42,1,UPPER('41')),
(43,1,UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')), (43,2,UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')), (44,1,UPPER('42')),
(45,1,UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')), (45,2,UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')), (46,1,UPPER('43')),
(47,1,UPPER('СУДЬЯ В ОТСТАВКЕ')), (47,2,UPPER('СУДЬЯ В ОТСТАВКЕ')), (48,1,UPPER('47')),
(49,1,UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')), (49,2,UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')), (50,1,UPPER('49')),
(51,1,UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (51,2,UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (52,1,UPPER('50')),
(53,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (53,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (54,1,UPPER('51')),
(55,1,UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (55,2,UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (56,1,UPPER('52')),
(57,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (57,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (58,1,UPPER('53')),
(59,1,UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')), (59,2,UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')), (60,1,UPPER('58')),
(61,1,UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')), (61,2,UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')), (62,1,UPPER('61')),
(63,1,UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')), (63,2,UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')), (64,1,UPPER('62')),
(65,1,UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')), (65,2,UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')), (66,1,UPPER('63')),
(67,1,UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')), (67,2,UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')), (68,1,UPPER('64')),
(69,1,UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')), (69,2,UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')), (70,1,UPPER('65')),
(71,1,UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (71,2,UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (72,1,UPPER('66')),
(73,1,UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (73,2,UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (74,1,UPPER('67')),
(75,1,UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')), (75,2,UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')), (76,1,UPPER('68')),
(77,1,UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')), (77,2,UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')), (78,1,UPPER('69')),
(79,1,UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')), (79,2,UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')), (80,1,UPPER('70')),
(81,1,UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (81,2,UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (82,1,UPPER('71')),
(83,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (83,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (84,1,UPPER('72')),
(85,1,UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (85,2,UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (86,1,UPPER('73')),
(87,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (87,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (88,1,UPPER('74')),
(89,1,UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')), (89,2,UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')), (90,1,UPPER('75')),
(91,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')), (91,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')), (92,1,UPPER('76')),
(93,1,UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (93,2,UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (94,1,UPPER('77')),
(95,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')), (95,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')), (96,1,UPPER('78')),
(97,1,UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')), (97,2,UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')), (98,1,UPPER('80')),
(99,1,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')), (99,2,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')), (100,1,UPPER('81')),
(101,1,UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')), (101,2,UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')), (102,1,UPPER('83')),
(103,1,UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')), (103,2,UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')), (104,1,UPPER('84')),
(105,1,UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')), (105,2,UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')), (106,1,UPPER('85')),
(107,1,UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')), (107,2,UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')), (108,1,UPPER('86')),
(109,1,UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')), (109,2,UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')), (110,1,UPPER('87')),
(111,1,UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')), (111,2,UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')), (112,1,UPPER('88')),
(113,1,UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (113,2,UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (114,1,UPPER('90')),
(115,1,UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (115,2,UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (116,1,UPPER('91')),
(117,1,UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')), (117,2,UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')), (118,1,UPPER('95')),
(119,1,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')), (119,2,UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')), (120,1,UPPER('96')),
(121,1,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (121,2,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')),(122,1,UPPER('98')),
(123,1,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (123,2,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (124,1,UPPER('99')),
(125,1,UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')), (125,2,UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')), (126,1,UPPER('100')),
(127,1,UPPER('РЕБЕНОК-ИНВАЛИД')), (127,2,UPPER('РЕБЕНОК-ИНВАЛИД')), (128,1,UPPER('110')),
(129,1,UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (129,2,UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (130,1,UPPER('111')),
(131,1,UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (131,2,UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (132,1,UPPER('112')),
(133,1,UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (133,2,UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (134,1,UPPER('113')),
(135,1,UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (135,2,UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (136,1,UPPER('114')),
(137,1,UPPER('ИНВАЛИД 3 ГРУППЫ')), (137,2,UPPER('ИНВАЛИД 3 ГРУППЫ')), (138,1,UPPER('115')),
(139,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')), (139,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')), (140,1,UPPER('120')),
(141,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')), (141,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')), (142,1,UPPER('121')),
(143,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')), (143,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')), (144,1,UPPER('122')),
(145,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')), (154,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')), (146,1,UPPER('123')),
(147,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')), (147,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')), (148,1,UPPER('124')),
(149,1,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')), (149,2,UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')), (150,1,UPPER('125')),
(151,1,UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')), (151,2,UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')), (152,1,UPPER('126')),
(153,1,UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')), (153,2,UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')), (154,1,UPPER('127')),
(155,1,UPPER('ГОРНЯКИ - ИНВАЛИДЫ')), (155,2,UPPER('ГОРНЯКИ - ИНВАЛИДЫ')), (156,1,UPPER('128')),
(157,1,UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')), (157,2,UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')), (158,1,UPPER('129')),
(159,1,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (159,2,UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (160,1,UPPER('130')),
(161,1,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (161,2,UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (162,1,UPPER('131')),
(163,1,UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (163,2,UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (164,1,UPPER('132')),
(165,1,UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')), (165,2,UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')), (166,1,UPPER('135')),
(167,1,UPPER('МАТЕРИ-ГЕРОИНИ')), (167,2,UPPER('МАТЕРИ-ГЕРОИНИ')), (168,1,UPPER('200')),
(169,1,UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')), (169,2,UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')), (170,1,UPPER('201')),
(171,1,UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')), (171,2,UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')), (172,1,UPPER('202')),
(173,1,UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (173,2,UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (174,1,UPPER('203')),
(175,1,UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (175,2,UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (176,1,UPPER('204')),
(177,1,UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')), (177,2,UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')), (178,1,UPPER('205')),
(179,1,UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')), (179,2,UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')), (180,1,UPPER('206')),
(181,1,UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')), (181,2,UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')), (182,1,UPPER('207')),
(183,1,UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')), (183,2,UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')), (184,1,UPPER('208')),
(185,1,UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')), (185,2,UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')), (186,1,UPPER('209')),
(187,1,UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')), (187,2,UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')), (188,1,UPPER('210')),
(189,1,UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')), (189,2,UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')), (190,1,UPPER('211')),
(191,1,UPPER('ПРИЕМНЫЕ СЕМЬИ')), (191,2,UPPER('ПРИЕМНЫЕ СЕМЬИ')), (192,1,UPPER('212')),
(193,1,UPPER('ДВОРНИКИ')), (193,2,UPPER('ДВОРНИКИ')), (194,1,UPPER('300')),
(195,1,UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')), (195,2,UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')), (196,1,UPPER('301')),
(197,1,UPPER('ПРИЕМНЫЕ СЕМЬИ')), (197,2,UPPER('ПРИЕМНЫЕ СЕМЬИ')), (198,1,UPPER('303')),
(199,1,UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')), (199,2,UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')), (200,1,UPPER('304')),
(201,1,UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')), (201,2,UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')), (202,1,UPPER('305')),
(203,1,UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')), (203,2,UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')), (204,1,UPPER('306')),
(205,1,UPPER('АФГАНИСТАН')), (205,2,UPPER('АФГАНИСТАН')), (206,1,UPPER('633')),
(207,1,UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ')), (207,2,UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ')), (208,1,UPPER('45'));
INSERT INTO `privilege_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,1,1200,1,1200),(1,1,1201,2,1201),
(1,2,1200,3,1200),(1,2,1201,4,1201),
(1,3,1200,5,1200),(1,3,1201,6,1201),
(1,4,1200,7,1200),(1,4,1201,8,1201),
(1,5,1200,9,1200),(1,5,1201,10,1201),
(1,6,1200,11,1200),(1,6,1201,12,1201),
(1,7,1200,13,1200),(1,7,1201,14,1201),
(1,8,1200,15,1200),(1,8,1201,16,1201),
(1,9,1200,17,1200),(1,9,1201,18,1201),
(1,10,1200,19,1200),(1,10,1201,20,1201),
(1,11,1200,21,1200),(1,11,1201,22,1201),
(1,12,1200,23,1200),(1,12,1201,24,1201),
(1,13,1200,25,1200),(1,13,1201,26,1201),
(1,14,1200,27,1200),(1,14,1201,28,1201),
(1,15,1200,29,1200),(1,15,1201,30,1201),
(1,16,1200,31,1200),(1,16,1201,32,1201),
(1,17,1200,33,1200),(1,17,1201,34,1201),
(1,18,1200,35,1200),(1,18,1201,36,1201),
(1,19,1200,37,1200),(1,19,1201,38,1201),
(1,20,1200,39,1200),(1,20,1201,40,1201),
(1,21,1200,41,1200),(1,21,1201,42,1201),
(1,22,1200,43,1200),(1,22,1201,44,1201),
(1,23,1200,45,1200),(1,23,1201,46,1201),
(1,24,1200,47,1200),(1,24,1201,48,1201),
(1,25,1200,49,1200),(1,25,1201,50,1201),
(1,26,1200,51,1200),(1,26,1201,52,1201),
(1,27,1200,53,1200),(1,27,1201,54,1201),
(1,28,1200,55,1200),(1,28,1201,56,1201),
(1,29,1200,57,1200),(1,29,1201,58,1201),
(1,30,1200,59,1200),(1,30,1201,60,1201),
(1,31,1200,61,1200),(1,31,1201,62,1201),
(1,32,1200,63,1200),(1,32,1201,64,1201),
(1,33,1200,65,1200),(1,33,1201,66,1201),
(1,34,1200,67,1200),(1,34,1201,68,1201),
(1,35,1200,69,1200),(1,35,1201,70,1201),
(1,36,1200,71,1200),(1,36,1201,72,1201),
(1,37,1200,73,1200),(1,37,1201,74,1201),
(1,38,1200,75,1200),(1,38,1201,76,1201),
(1,39,1200,77,1200),(1,39,1201,78,1201),
(1,40,1200,79,1200),(1,40,1201,80,1201),
(1,41,1200,81,1200),(1,41,1201,82,1201),
(1,42,1200,83,1200),(1,42,1201,84,1201),
(1,43,1200,85,1200),(1,43,1201,86,1201),
(1,44,1200,87,1200),(1,44,1201,88,1201),
(1,45,1200,89,1200),(1,45,1201,90,1201),
(1,46,1200,91,1200),(1,46,1201,92,1201),
(1,47,1200,93,1200),(1,47,1201,94,1201),
(1,48,1200,95,1200),(1,48,1201,96,1201),
(1,49,1200,97,1200),(1,49,1201,98,1201),
(1,50,1200,99,1200),(1,50,1201,100,1201),
(1,51,1200,101,1200),(1,51,1201,102,1201),
(1,52,1200,103,1200),(1,52,1201,104,1201),
(1,53,1200,105,1200),(1,53,1201,106,1201),
(1,54,1200,107,1200),(1,54,1201,108,1201),
(1,55,1200,109,1200),(1,55,1201,110,1201),
(1,56,1200,111,1200),(1,56,1201,112,1201),
(1,57,1200,113,1200),(1,57,1201,114,1201),
(1,58,1200,115,1200),(1,58,1201,116,1201),
(1,59,1200,117,1200),(1,59,1201,118,1201),
(1,60,1200,119,1200),(1,60,1201,120,1201),
(1,61,1200,121,1200),(1,61,1201,122,1201),
(1,62,1200,123,1200),(1,62,1201,124,1201),
(1,63,1200,125,1200),(1,63,1201,126,1201),
(1,64,1200,127,1200),(1,64,1201,128,1201),
(1,65,1200,129,1200),(1,65,1201,130,1201),
(1,66,1200,131,1200),(1,66,1201,132,1201),
(1,67,1200,133,1200),(1,67,1201,134,1201),
(1,68,1200,135,1200),(1,68,1201,136,1201),
(1,69,1200,137,1200),(1,69,1201,138,1201),
(1,70,1200,139,1200),(1,70,1201,140,1201),
(1,71,1200,141,1200),(1,71,1201,142,1201),
(1,72,1200,143,1200),(1,72,1201,144,1201),
(1,73,1200,145,1200),(1,73,1201,146,1201),
(1,74,1200,147,1200),(1,74,1201,148,1201),
(1,75,1200,149,1200),(1,75,1201,150,1201),
(1,76,1200,151,1200),(1,76,1201,152,1201),
(1,77,1200,153,1200),(1,77,1201,154,1201),
(1,78,1200,155,1200),(1,78,1201,156,1201),
(1,79,1200,157,1200),(1,79,1201,158,1201),
(1,80,1200,159,1200),(1,80,1201,160,1201),
(1,81,1200,161,1200),(1,81,1201,162,1201),
(1,82,1200,163,1200),(1,82,1201,164,1201),
(1,83,1200,165,1200),(1,83,1201,166,1201),
(1,84,1200,167,1200),(1,84,1201,168,1201),
(1,85,1200,169,1200),(1,85,1201,170,1201),
(1,86,1200,171,1200),(1,86,1201,172,1201),
(1,87,1200,173,1200),(1,87,1201,174,1201),
(1,88,1200,175,1200),(1,88,1201,176,1201),
(1,89,1200,177,1200),(1,89,1201,178,1201),
(1,90,1200,179,1200),(1,90,1201,180,1201),
(1,91,1200,181,1200),(1,91,1201,182,1201),
(1,92,1200,183,1200),(1,92,1201,184,1201),
(1,93,1200,185,1200),(1,93,1201,186,1201),
(1,94,1200,187,1200),(1,94,1201,188,1201),
(1,95,1200,189,1200),(1,95,1201,190,1201),
(1,96,1200,191,1200),(1,96,1201,192,1201),
(1,97,1200,193,1200),(1,97,1201,194,1201),
(1,98,1200,195,1200),(1,98,1201,196,1201),
(1,99,1200,197,1200),(1,99,1201,198,1201),
(1,100,1200,199,1200),(1,100,1201,200,1201),
(1,101,1200,201,1200),(1,101,1201,202,1201),
(1,102,1200,203,1200),(1,102,1201,204,1201),
(1,103,1200,205,1200),(1,103,1201,206,1201),
(1,104,1200,207,1200),(1,104,1201,208,1201);

-- Status descriptions
INSERT INTO `status_description`(`code`, `name`) VALUES
(110,'Загружено'), (111,'Ошибка загрузки'), (112,'Загружается'),
(120,'Связано'), (121,'Ошибка связывания'), (122,'Связывается'),
(130,'Обработано'), (131,'Ошибка обработки'), (132,'Обрабатывается'),
(140,'Выгружено'), (141,'Ошибка выгрузки'), (142,'Выгружается'),
(240,'Загружена'),
(200,'Неизвестный населенный пункт'), (237, 'Неизвестный тип улицы'), (201,'Неизвестная улица'), (202,'Неизвестный номер дома'),
(234,'Найдено более одного населенного пункта в адресной базе'), (238, 'Найдено более одного типа улицы в адресной базе'), (235,'Найдено более одной улицы в адресной базе'),
(236,'Найдено более одного дома в адресной базе'), (210,'Найдено более одного соответствия для населенного пункта'), (239, 'Найдено более одного соответствия для типа улицы'),
(211,'Найдено более одного соответствия для улицы'), (228,'Найдено более одного соответствия для дома'),
(204,'Адрес откорректирован'), (205,'Населенный пункт не найден в соответствиях МН'), (206,'Район не найден в соответствиях МН'),
(207,'Тип улицы не найден в соответствиях МН'), (208,'Улица не найдена в соответствиях МН'), (209,'Дом не найден в соответствиях МН'),
(229, 'Более одного населенного пункта найдено в соответствиях МН'), (230,'Более одного района найдено в соответствиях МН'),
(231,'Более одного типа улицы найдено в соответствиях МН'), (232,'Более одной улицы найдено в соответствиях МН'),
(233,'Более одного дома найдено в соответствиях МН'),
(212,'Номер личного счета не разрешён'), (213,'Больше одного личного счета'), (214,'Номер личного счета разрешен'),
(215,'Запись обработана'), (216,'Код тарифа на оплату жилья не найден в справочнике тарифов'), (217,'Не сопоставлен носитель льготы'),
(218,'Льгота не найдена в справочнике соответствий'), (219,'Неверный формат данных на этапе обработки'), (203,'Неверный формат данных на этапе связывания'),
(220, 'Нет запроса оплаты'), (221, 'Населенный пункт не найден в МН'), (222, 'Район не найден в МН'), (223, 'Тип улицы не найден в МН'),
(224, 'Улица не найдена в МН'), (225, 'Дом не найден в МН'), (226, 'Корпус дома не найден в МН'), (227, 'Квартира не найдена в МН'),
(300, 'Тариф не найден'), (301, 'Объект формы собственности не найден в справочнике соответствий для МН'),
(302, 'Код формы собственности не найден в справочнике соответствий для ОСЗН'), (303, 'Нечисловой код формы собственности в справочнике соответствий для ОСЗН'),
(304, 'Объект льготы не найден в справочнике соответствий для МН'), (305, 'Код льготы не найден в справочнике соответствий для ОСЗН'),
(306, 'Нечисловой код льготы в справочнике соответствий для ОСЗН'), (307, 'Нечисловой порядок льготы');

-- Itself organization
INSERT INTO `organization`(`object_id`) VALUES (0);
INSERT INTO `organization_string_culture`(`id`, `locale_id`, `value`) VALUES
(1, 1, UPPER('Модуль №1')), (1,2,UPPER('Модуль №1')), (2, 1, UPPER('0'));
INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,0,900,1,900), (1,0,901,2,901);