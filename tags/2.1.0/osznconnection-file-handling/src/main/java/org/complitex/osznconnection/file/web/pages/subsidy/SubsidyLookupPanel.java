/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.subsidy;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

/**
 *
 * @author Artem
 */
public class SubsidyLookupPanel extends AbstractLookupPanel<Subsidy> {

    @EJB
    private LookupBean lookupBean;
    @EJB
    private PersonAccountService personAccountService;

    public SubsidyLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(Subsidy subsidy, Long cityId, Long streetId, Long streetTypeId,
            Long buildingId, String apartment) {
        subsidy.setInternalCityId(cityId);
        subsidy.setInternalStreetId(streetId);
        subsidy.setInternalStreetTypeId(streetTypeId);
        subsidy.setInternalBuildingId(buildingId);
        subsidy.setField(SubsidyDBF.FLAT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(Subsidy subsidy) {
        return subsidy.getInternalCityId() != null && subsidy.getInternalCityId() > 0
                && subsidy.getInternalStreetId() != null && subsidy.getInternalStreetId() > 0
                && subsidy.getInternalBuildingId() != null && subsidy.getInternalBuildingId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(Subsidy subsidy, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(subsidy, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(Subsidy subsidy, long userOrganizationId) throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(subsidy, userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(Subsidy subsidy, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(subsidy, accountNumber, userOrganizationId);
    }

    @Override
    protected String resolveOutgoingDistrict(Subsidy subsidy, long userOrganizationId) {
        return lookupBean.resolveOutgoingDistrict(subsidy, userOrganizationId);
    }
}
