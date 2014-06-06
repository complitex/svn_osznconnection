package org.complitex.osznconnection.organization_type.strategy;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

import javax.ejb.Stateless;
import java.util.Collection;

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
    public static final long OSZN_TYPE = 2;
    public static final long CALCULATION_CENTER_TYPE = 3;

    @Override
    protected Collection<Long> getReservedInstanceIds() {
        return ImmutableList.of(USER_ORGANIZATION_TYPE, OSZN_TYPE, CALCULATION_CENTER_TYPE);
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters pageParameters = super.getEditPageParams(objectId, parentId, parentEntity);
        pageParameters.set(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters pageParameters = super.getHistoryPageParams(objectId);
        pageParameters.set(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters pageParameters = super.getListPageParams();
        pageParameters.set(STRATEGY, STRATEGY_NAME);
        return pageParameters;
    }
}
