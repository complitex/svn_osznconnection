update `log` set `module` = 'org.complitex.template' where `module` = 'org.complitex.osznconnection.commons';
update `log` set `module` = 'org.complitex.dictionary' where `module` = 'org.complitex.dictionaryfw';
update `log` set `module` = 'org.complitex.admin' where `module` = 'org.complitex.osznconnection.admin';

update `log` set `controller` = 'org.complitex.admin.web.UserEdit'
  where `controller` = 'org.complitex.osznconnection.admin.web.UserEdit';
update `log` set `controller` = 'org.complitex.template.web.security.SecurityWebListener'
  where `controller` = 'org.complitex.osznconnection.commons.web.security.SecurityWebListener';
update `log` set `controller` = 'org.complitex.dictionary.strategy.web.DomainObjectEditPanel'
  where `controller` = 'org.complitex.dictionaryfw.strategy.web.DomainObjectEditPanel';

update `log` set `model` = 'org.complitex.dictionary.entity.User'
  where `model` = 'org.complitex.dictionaryfw.entity.User';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#building'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#building';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#city'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#city';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#country'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#country';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#person'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#person';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#region'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#region';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#room'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#room';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#apartment'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#apartment';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#street'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#street';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#organization'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#organization';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#ownership'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#ownership';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#privilege'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#privilege';
update `log` set `model` = 'org.complitex.dictionary.entity.DomainObject#district'
  where `model` = 'org.complitex.dictionaryfw.entity.DomainObject#district';

INSERT INTO `update` (`version`) VALUE ('20101216_452_0.0.16');
