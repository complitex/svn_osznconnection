package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.SubsidyService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

public class SubsidyLookupPanel extends AbstractLookupPanel<Subsidy> {

    @EJB
    private LookupBean lookupBean;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private SubsidyService subsidyService;

    public SubsidyLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(Subsidy subsidy, Long cityId, Long streetId, Long streetTypeId,
            Long buildingId, String apartment) {
        subsidy.setCityObjectId(cityId);
        subsidy.setStreetObjectId(streetId);
        subsidy.setStreetTypeObjectId(streetTypeId);
        subsidy.setBuildingObjectId(buildingId);
        subsidy.setField(SubsidyDBF.FLAT + "_CYR", apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(Subsidy subsidy) {
        return subsidy.getCityObjectId() != null && subsidy.getCityObjectId() > 0
                && subsidy.getStreetObjectId() != null && subsidy.getStreetObjectId() > 0
                && subsidy.getBuildingObjectId() != null && subsidy.getBuildingObjectId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(Subsidy subsidy, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(subsidy, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(Subsidy subsidy, long userOrganizationId) throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(subsidy, subsidy.getOutgoingDistrict(), subsidy.getOutgoingStreetType(),
                subsidy.getOutgoingStreet(), subsidy.getOutgoingBuildingNumber(), subsidy.getOutgoingBuildingCorp(),
                subsidy.getOutgoingApartment(), (Date) subsidy.getField(SubsidyDBF.DAT1), userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(Subsidy subsidy, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(subsidy, accountNumber, userOrganizationId);
    }

    @Override
    protected String getTitle(Subsidy subsidy) {
        return subsidy.getFio() + ", " + subsidy.getAddress(getLocale());
    }

    @Override
    protected String getServicingOrganizationCode(Subsidy request) {
        return subsidyService.getServicingOrganizationCode(request.getRequestFileId());
    }
}
