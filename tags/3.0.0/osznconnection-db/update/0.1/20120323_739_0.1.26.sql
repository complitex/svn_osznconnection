-- Support of some user preferences has been dropped: sorting related preferences, filter object, page number.

DELETE FROM `preference` WHERE `key` IN ('PAGE_INDEX','SORT_ORDER','SORT_PROPERTY');

INSERT INTO `update` (`version`) VALUE ('20120323_739_0.1.26');