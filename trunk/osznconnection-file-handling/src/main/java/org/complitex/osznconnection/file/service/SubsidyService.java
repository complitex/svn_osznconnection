package org.complitex.osznconnection.file.service;

import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.correction.service.OrganizationCorrectionBean;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 0:16
 */
@Stateless
public class SubsidyService {
    @EJB
    private OsznOrganizationStrategy organizationStrategy;

    @EJB
    private OrganizationCorrectionBean organizationCorrectionBean;

    @EJB
    private RequestFileBean requestFileBean;

    public SubsidySum getSubsidySum(AbstractRequest request){
        OsznOrganization organization = organizationStrategy.findById(request.getUserOrganizationId(), true);

        BigDecimal nSum = new BigDecimal(0);
        BigDecimal sbSum = new BigDecimal(0);
        BigDecimal smSum = new BigDecimal(0);

        for (ServiceAssociation sa : organization.getServiceAssociationList()) {
            nSum = nSum.add((BigDecimal) request.getField("P" + sa.getServiceProviderTypeId()));
            sbSum = sbSum.add((BigDecimal) request.getField("SB" + sa.getServiceProviderTypeId()));
            smSum = smSum.add((BigDecimal) request.getField("SM" + sa.getServiceProviderTypeId()));
        }

        return new SubsidySum(nSum, sbSum, smSum);
    }

    public boolean validate(AbstractRequest request){
        SubsidySum subsidySum = getSubsidySum(request);

        Long numm = (Long)request.getField("NUMM");
        BigDecimal summa = (BigDecimal) request.getField("SUMMA");
        BigDecimal subs = (BigDecimal) request.getField("SUBS");
        BigDecimal nmPay = (BigDecimal) request.getField("NM_PAY");

        return nmPay.compareTo(subsidySum.getNSum()) == 0
                && summa.compareTo(subsidySum.getSmSum()) == 0
                && subs.compareTo(subsidySum.getSbSum()) == 0
                && (numm <= 0 || summa.compareTo(subs.multiply(new BigDecimal(numm))) == 0);
    }

    public Long getServicingOrganizationId(RequestFile subsidyRequestFile){
        String fileName = subsidyRequestFile.getName();
        String code = fileName.substring(0, fileName.length()-8);

        List<OrganizationCorrection> list = organizationCorrectionBean.getOrganizationCorrections(
                FilterWrapper.of(new OrganizationCorrection(null, null, code, subsidyRequestFile.getOrganizationId(),
                        subsidyRequestFile.getUserOrganizationId(), null)));

        return !list.isEmpty() ?  list.get(0).getObjectId() : organizationStrategy.getObjectIdByCode(code);
    }

    public String getServicingOrganizationCode(Long requestFileId){
        RequestFile requestFile = requestFileBean.findById(requestFileId);

        String fileName = requestFile.getName();
        String code = fileName.substring(0, fileName.length()-8);

        List<OrganizationCorrection> list = organizationCorrectionBean.getOrganizationCorrections(
                FilterWrapper.of(new OrganizationCorrection(null, null, code, requestFile.getOrganizationId(),
                        requestFile.getUserOrganizationId(), null)));

        return !list.isEmpty() ? organizationStrategy.getUniqueCode(list.get(0).getObjectId()) : code;
    }
}