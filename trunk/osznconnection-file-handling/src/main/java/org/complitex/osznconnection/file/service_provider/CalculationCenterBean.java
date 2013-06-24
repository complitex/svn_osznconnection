/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Artem
 */
@Stateless
public class CalculationCenterBean {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    /**
     * Figures out calculation context for calculation center with minimal object id.
     * @param userOrganizationId userOrganizationId
     * @return 
     */
    public CalculationContext getContextWithAnyCalculationCenter(long userOrganizationId) {
        final DomainObject userOrganization = checkUserOrganization(userOrganizationId);

        final ServiceAssociationList serviceAssociationList = organizationStrategy.getServiceAssociations(userOrganization);
        final SetMultimap<Long, Long> groups = serviceAssociationList.groupByCalculationCenter();

        final long calculationCenterId = Collections.min(groups.keySet());
        final Set<Long> serviceProviderTypeIds = ImmutableSet.copyOf(groups.get(calculationCenterId));

        final CalculationContext calculationContext = new CalculationContext(calculationCenterId,
                organizationStrategy.getDataSource(calculationCenterId),
                serviceProviderTypeIds, userOrganizationId);
        return calculationContext;
    }

    private DomainObject checkUserOrganization(long userOrganizationId) {
        DomainObject userOrganization = organizationStrategy.findById(userOrganizationId, true);
        if (userOrganization == null || userOrganization.getId() == null || userOrganization.getId() <= 0) {
            throw new IllegalArgumentException("User organization was not found.");
        }
        return userOrganization;
    }

    public Collection<CalculationContext> getContexts(long userOrganizationId) {
        final DomainObject userOrganization = checkUserOrganization(userOrganizationId);

        final ServiceAssociationList serviceAssociationList = organizationStrategy.getServiceAssociations(userOrganization);
        final SetMultimap<Long, Long> groups = serviceAssociationList.groupByCalculationCenter();
        final Collection<CalculationContext> contexts = Lists.newArrayList();

        for (long calculationCenterId : groups.keySet()) {
            final Set<Long> serviceProviderTypeIds = ImmutableSet.copyOf(groups.get(calculationCenterId));
            final String dataSource = organizationStrategy.getDataSource(calculationCenterId);
            contexts.add(new CalculationContext(calculationCenterId, dataSource, serviceProviderTypeIds, userOrganizationId));
        }

        return ImmutableList.copyOf(contexts);
    }
}
