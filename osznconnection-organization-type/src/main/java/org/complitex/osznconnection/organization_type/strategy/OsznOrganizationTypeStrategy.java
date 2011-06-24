/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization_type.strategy;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.ejb.Stateless;
import org.apache.wicket.PageParameters;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

/**
 *
 * @author Artem
 */
@Stateless
public class OsznOrganizationTypeStrategy extends OrganizationTypeStrategy {

    private static final String STRATEGY_NAME = OsznOrganizationTypeStrategy.class.getSimpleName();
    /**
     * Organization type ids
     */
    public static final long OSZN = 2;
    public static final long CALCULATION_CENTER = 3;

    @Override
    protected Collection<Long> getReservedInstanceIds() {
        return ImmutableList.of(USER_ORGANIZATION_TYPE, OSZN, CALCULATION_CENTER);
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters pageParameters = super.getEditPageParams(objectId, parentId, parentEntity);
        pageParameters.put(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters pageParameters = super.getHistoryPageParams(objectId);
        pageParameters.put(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters pageParameters = super.getListPageParams();
        pageParameters.put(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }
}
