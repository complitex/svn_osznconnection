/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
@Stateless
public class CalculationCenterBean {

    @EJB
    private SessionBean sessionBean;
    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;

    public CalculationCenterInfo getInfo() {
        DomainObject mainUserOrganization = sessionBean.getMainUserOrganization();
        if (mainUserOrganization == null || mainUserOrganization.getId() == null) {
            throw new RuntimeException("User hasn't associated organization.");
        }

        CalculationCenterInfo calculationCenterInfo = new CalculationCenterInfo();
        final long calculationCenterOrganizationId = organizationStrategy.getCalculationCenterId(mainUserOrganization);
        calculationCenterInfo.setOrganizationId(calculationCenterOrganizationId);
        calculationCenterInfo.setServiceProviderTypeIds(organizationStrategy.getServiceProviderTypeIds(calculationCenterOrganizationId));
        return calculationCenterInfo;
    }
}
