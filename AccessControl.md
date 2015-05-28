# Управление доступом #

Основные теоретические основы можно подчеркнуть тут http://en.wikipedia.org/wiki/Category:Access_control

На текущий момент в слое Wicket и EJB уже используется управление доступом на основе ролей Java EE. Задача расширить эту модель для фильтрации конкретных записей в таблицах.

Одним из вариантов может быть реализация подхода ключей от дверей в доме. Для каждой записи в таблице назначается ключ безопасности по которому определяется можно ли прочитать или изменить запись. И всем пользователям раздаются соответствующие ключи с помощью которых делаются выборки из этих таблиц. Создаются функции, которые по идентификатору пользователя и таблице возвращают список ключей которые есть у пользователя.

Пример.
```
Таблица Permission
-----------------------------------------------------------
pk_id  permission_id  table    entity     object_id   type
-----------------------------------------------------------
pk_id1 permission_id1 street   user       user1       write
pk_id2 permission_id1 street   group      group1      write
pk_id3 permission_id2 street   department department1 read
pk_id4 permission_id3 building department department1 write

Таблица Street
--------------------------------
pk_id  object_id  permission_id
--------------------------------
pk_id1 object_id1 permission_id1  
pk_id2 object_id2 permission_id1 
pk_id3 object_id3 permission_id1 
pk_id4 object_id4 permission_id2 

Таблица Building
-------------------------------------------
pk_id  object_id  parent_id  permission_id
-------------------------------------------
pk_id1 object_id1 parent_id1 permission_id3  
pk_id2 object_id2 parent_id2 permission_id3 
pk_id3 object_id3 parent_id3 permission_id3 
pk_id4 object_id4 parent_id4 permission_id3 

Запросы: 

select * from `street` s 
  left join `building` b on (b.`parent_id` = s.`object_id`) 
where s.`permission_id` in getPermissions(user_id, 'street')
  and b.`permission_id` in getPermissions(user_id, 'building')
```

Таблица `Permission` и колонки `permission_id` редактируется администратором через пользовательский интерфейс. Колонка `table` нужна на случай быстрого поиска. Функции `getPermissions` реализуются на java или mysql.