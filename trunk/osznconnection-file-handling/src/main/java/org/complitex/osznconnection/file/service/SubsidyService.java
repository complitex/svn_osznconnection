package org.complitex.osznconnection.file.service;

import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidySum;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.math.BigDecimal;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 0:16
 */
@Stateless
public class SubsidyService {
    @EJB
    private OsznOrganizationStrategy organizationStrategy;

    public SubsidySum getSubsidySum(Subsidy subsidy){
        OsznOrganization organization = organizationStrategy.findById(subsidy.getUserOrganizationId(), true);

        BigDecimal nSum = new BigDecimal(0);
        BigDecimal sbSum = new BigDecimal(0);
        BigDecimal smSum = new BigDecimal(0);

        for (ServiceAssociation sa : organization.getServiceAssociationList()) {
            nSum = nSum.add((BigDecimal) subsidy.getField("P" + sa.getServiceProviderTypeId())).setScale(2);
            sbSum = sbSum.add((BigDecimal) subsidy.getField("SB" + sa.getServiceProviderTypeId())).setScale(2);
            smSum = smSum.add((BigDecimal) subsidy.getField("SM" + sa.getServiceProviderTypeId())).setScale(2);
        }

        return new SubsidySum(nSum, sbSum, smSum);
    }


}
