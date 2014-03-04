-- For all standard entities script deletes empty SORT_PROPERTY and SORT_ORDER preferences.
-- This is preparation step in order to apply new strategy's feature: default sort column. 
-- If list page has no preference for sort column then default sort column will be used (see complitex common modules commit 938).

SET GLOBAL log_bin_trust_function_creators = 1;

DELIMITER /
CREATE PROCEDURE `update_0.1.24`()
BEGIN

    DECLARE l_id BIGINT(20);
    DECLARE l_user_id BIGINT(20);
    DECLARE l_page VARCHAR(1000);
    DECLARE l_count INT;
    DECLARE l_done INT DEFAULT 0;
    
    -- preferences cursor: all standard entities and building.
    DECLARE p_cursor CURSOR FOR 
	SELECT p.`id`, p.`user_id`, p.`page` FROM `preference` p WHERE p.`key` = UPPER('sort_property') 
		AND TRIM(p.`value`) = ''
		AND p.`page` IN (
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#country',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#region',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#city',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#city_type',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#district',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#street',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#street_type',
			'org.complitex.address.strategy.building.web.list.BuildingList',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#organization',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#organization_type',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#ownership',
			'org.complitex.dictionary.strategy.web.DomainObjectListPanel#privilege');
	
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET l_done = 1;

	OPEN p_cursor;
	    p_loop: LOOP
		FETCH p_cursor INTO l_id, l_user_id, l_page;

		IF l_done = 1 THEN
		    LEAVE p_loop;
		END IF;	
			
		SET l_count = 0;
		-- count false sort_order preferences associated with current sort_property preference.
		SELECT COUNT(1) INTO l_count FROM `preference` WHERE `page` = l_page AND `key` = UPPER('sort_order')
			AND TRIM(`value`) = 'false' AND `user_id` = l_user_id;
		
		-- if false sort_order preference is found (it can be only one record) then delete it and associated sort_property preference.
		IF l_count = 1 THEN 
			DELETE FROM `preference` WHERE `id` = l_id;
			DELETE FROM `preference` WHERE `page` = l_page AND `key` = UPPER('sort_order') 
				AND TRIM(`value`) = 'false' AND `user_id` = l_user_id;
		END IF;
	    END LOOP p_loop;
	CLOSE p_cursor;

	INSERT INTO `update` (`version`) VALUE ('20120321_737_0.1.24');
END/
DELIMITER ;

CALL `update_0.1.24`();
DROP PROCEDURE `update_0.1.24`;

SET GLOBAL log_bin_trust_function_creators = 0;