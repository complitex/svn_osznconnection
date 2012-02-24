/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
@Stateless
public class CalculationCenterBean {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    private CalculationContext getCalculationContext(DomainObject userOrganization) {
        final long calculationCenterOrganizationId = organizationStrategy.getCalculationCenterId(userOrganization);
        CalculationContext calculationCenterInfo = new CalculationContext(calculationCenterOrganizationId,
                organizationStrategy.getDataSource(calculationCenterOrganizationId),
                organizationStrategy.getServiceProviderTypeIds(calculationCenterOrganizationId), userOrganization.getId());
        return calculationCenterInfo;
    }

    public CalculationContext getContext(long userOrganizationId) {
        DomainObject userOrganization = organizationStrategy.findById(userOrganizationId, true);
        if (userOrganization == null || userOrganization.getId() == null || userOrganization.getId() <= 0) {
            throw new RuntimeException("User organization was not found.");
        }
        return getCalculationContext(userOrganization);
    }
}
