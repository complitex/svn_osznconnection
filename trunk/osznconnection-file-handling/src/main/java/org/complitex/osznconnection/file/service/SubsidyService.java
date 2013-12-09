package org.complitex.osznconnection.file.service;

import org.complitex.osznconnection.file.entity.*;
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
            nSum = nSum.add((BigDecimal) subsidy.getField("P" + sa.getServiceProviderTypeId()));
            sbSum = sbSum.add((BigDecimal) subsidy.getField("SB" + sa.getServiceProviderTypeId()));
            smSum = smSum.add((BigDecimal) subsidy.getField("SM" + sa.getServiceProviderTypeId()));
        }

        return new SubsidySum(nSum, sbSum, smSum);
    }

    public boolean validate(AbstractRequest request){
        OsznOrganization organization = organizationStrategy.findById(request.getUserOrganizationId(), true);

        BigDecimal nSum = new BigDecimal(0);
        BigDecimal sbSum = new BigDecimal(0);
        BigDecimal smSum = new BigDecimal(0);

        /*
           Если поле NUMM != 0, то нужно выполнять проверку SUMMA == SUBS * NUMM и SUMMA должен быть
           вточности равен сумме полей SBN, где N - коды услуг, разрешенных для организации пользователей.
           Если поле NUMM == 0, то нужно выполнять проверку SUMMA должен быть вточности равен сумме полей SMN,
           где N - коды услуг, разрешенных для организации пользователей.
         */

        for (ServiceAssociation sa : organization.getServiceAssociationList()) {
            nSum = nSum.add((BigDecimal) request.getField("P" + sa.getServiceProviderTypeId()));
            sbSum = sbSum.add((BigDecimal) request.getField("SB" + sa.getServiceProviderTypeId()));
            smSum = smSum.add((BigDecimal) request.getField("SM" + sa.getServiceProviderTypeId()));
        }

        Long numm = (Long)request.getField("NUMM");
        BigDecimal summa = (BigDecimal) request.getField("SUMMA");
        BigDecimal subs = (BigDecimal) request.getField("SUBS");
        BigDecimal nmPay = (BigDecimal) request.getField("NM_PAY");

        return nmPay.compareTo(nSum) == 0
                && (numm == 0 || summa.compareTo(subs.multiply(new BigDecimal(numm))) == 0 || summa.compareTo(sbSum) == 0)
                && (numm != 0 || summa.compareTo(smSum) == 0);
    }
}
