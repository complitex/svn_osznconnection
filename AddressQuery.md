# Пример запроса поиска адреса #

```
select
  *
from
  /*Страна*/
  `country`
    left join `country_attribute` on (`country_attribute`.`object_id` = `country`.`object_id`)
    left join `country_string_culture` on (`country_string_culture`.`id` = `country_attribute`.`value_id`)
  /*Регион*/
  left join `region` on (`region`.`parent_id` = `country`.`object_id`)
    left join `region_attribute` on (`region_attribute`.`object_id` = `region`.`object_id`)
    left join `region_string_culture` on (`region_string_culture`.`id` = `region_attribute`.`value_id`)
  /*Населенный пункт*/
  left join `city` on (`city`.`parent_id` = `region`.`object_id`)
    left join `city_attribute` on (`city_attribute`.`object_id` = `city`.`object_id`)
    left join `city_string_culture` on (`city_string_culture`.`id` = `city_attribute`.`value_id`)
  /*Тип населенного пункта*/
  left join `entity_type` city_type on (city_type.`id` = `city`.`entity_type_id`)
    left join `string_culture` city_type_sc on (city_type_sc.`id` = city_type.`entity_type_name_id`)
  /*Район*/
  left join `district` on (`district`.`parent_id` = `city`.`object_id`)
    left join `district_attribute` on (`district_attribute`.`object_id` = `district`.`object_id`)
    left join `district_string_culture` on (`district_string_culture`.`id` = `district_attribute`.`value_id`)
  /*Улица*/
  left join `street` on (`street`.`parent_id` = `city`.`object_id`)
    left join `street_attribute` on (`street_attribute`.`object_id` = `street`.`object_id`)
    left join `street_string_culture` on (`street_string_culture`.`id` = `street_attribute`.`value_id`)
  /*Тип улица*/
  left join `entity_type` street_type on (street_type.`id` = `street`.`entity_type_id`)
    left join `string_culture` street_type_sc on (street_type_sc.`id` = street_type.`entity_type_name_id`)
  /*Дом*/
  left join `building` on (`building`.`parent_id` = `city`.`object_id`)
    /*Номер дома*/
    left join `building_attribute` b_num on (b_num.`object_id` = `building`.`object_id` and b_num.`attribute_type_id` = 500)
    left join `building_string_culture` b_num_sc on (b_num_sc.`id` = b_num.`value_id`)
    /*Корпус*/
    left join `building_attribute` b_corp on (b_corp.`object_id` = `building`.`object_id` and b_corp.`attribute_type_id` = 501)
    left join `building_string_culture` b_corp_sc on (b_corp_sc.`id` = b_corp.`value_id`)
    /*Строение*/
    left join `building_attribute` b_str on (b_str.`object_id` = `building`.`object_id` and b_str.`attribute_type_id` = 502)
    left join `building_string_culture` b_str_sc on (b_str_sc.`id` = b_str.`value_id`)
    /*Улица*/
    left join `building_attribute` b_street on (b_street.`object_id` = `building`.`object_id` and b_street.`attribute_type_id` = 503)
    /*Район*/
    left join `building_attribute` b_district on (b_district.`object_id` = `building`.`object_id` and b_district.`attribute_type_id` = 504)

where
  `country_string_culture`.`value` like 'УКРАИНА'
  and `region_string_culture`.`value` like 'ХАРЬКОВСКАЯ ОБЛАСТЬ'
  and street_type_sc.`value` like 'ПРОСП'
  and `city_string_culture`.`value` like 'ХАРЬКОВ'
  and `district_string_culture`.`value` like 'ОРДЖОНИКИДЗЕВСКИЙ'
  and `street_string_culture`.`value` like 'ЛЕНИНА'
  and b_num_sc.`value` = '68А'
  and b_street.`value_id` = `street`.`object_id`
```