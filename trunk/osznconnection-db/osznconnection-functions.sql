DROP FUNCTION IF EXISTS `TO_CYRILLIC`/

CREATE FUNCTION `TO_CYRILLIC`(str VARCHAR(1000)) RETURNS VARCHAR(1000) CHARSET utf8 COLLATE utf8_unicode_ci
NO SQL
  BEGIN IF str IS NULL THEN RETURN NULL; END IF;
    RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
      REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE( str, 'a', 'а'), 'A', 'А'), 'T', 'Т'),
      'x', 'х'), 'X', 'Х'), 'k', 'к'), 'K', 'К'), 'M', 'М'), 'e', 'е'), 'E', 'Е'), 'o', 'о'), 'O', 'О'), 'p', 'р'), 'P', 'Р'),
      'c', 'с'), 'C', 'С'), 'B', 'В'), 'i', 'і'), 'I', 'І'), 'Ї', 'Є'), 'Ў', 'І'), '∙', 'ї'), '°', 'Ї');
  END/