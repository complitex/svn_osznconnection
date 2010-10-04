-- sequence update

update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `room`)+1 where sequence_name = 'room';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `room_string_culture`)+1 where sequence_name = 'room_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `apartment`)+1 where sequence_name = 'apartment';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `apartment_string_culture`)+1 where sequence_name = 'apartment_string_culture';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `building_string_culture`)+1 where sequence_name = 'building_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `building`)+1 where sequence_name = 'building';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `street_string_culture`)+1 where sequence_name = 'street_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `street`)+1 where sequence_name = 'street';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `district_string_culture`)+1 where sequence_name = 'district_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `district`)+1 where sequence_name = 'district';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `city_string_culture`)+1 where sequence_name = 'city_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `city`)+1 where sequence_name = 'city';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `region_string_culture`)+1 where sequence_name = 'region_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `region`)+1 where sequence_name = 'region';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `country_string_culture`)+1 where sequence_name = 'country_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `country`)+1 where sequence_name = 'country';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `organization_string_culture`)+1 where sequence_name = 'organization_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `organization`)+1 where sequence_name = 'organization';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `ownership_string_culture`)+1 where sequence_name = 'ownership_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `ownership`)+1 where sequence_name = 'ownership';
update sequence set sequence_value = (select IFNULL(max(`id`), 0) from `privilege_string_culture`)+1 where sequence_name = 'privilege_string_culture';
update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `privilege`)+1 where sequence_name = 'privilege';
