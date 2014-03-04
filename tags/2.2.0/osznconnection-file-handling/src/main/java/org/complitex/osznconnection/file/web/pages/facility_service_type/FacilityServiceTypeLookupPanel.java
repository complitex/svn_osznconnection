/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility_service_type;

import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.FacilityServiceType;
import org.complitex.osznconnection.file.entity.FacilityServiceTypeDBF;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class FacilityServiceTypeLookupPanel extends AbstractLookupPanel<FacilityServiceType> {

    @EJB
    private LookupBean lookupBean;

    @EJB
    private PersonAccountService personAccountService;

    public FacilityServiceTypeLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(FacilityServiceType facilityServiceType, Long cityId, Long streetId,
            Long streetTypeId, Long buildingId, String apartment) {
        facilityServiceType.setInternalCityId(cityId);
        facilityServiceType.setInternalStreetId(streetId);
        facilityServiceType.setInternalStreetTypeId(streetTypeId);
        facilityServiceType.setInternalBuildingId(buildingId);
        facilityServiceType.setField(FacilityServiceTypeDBF.APT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(FacilityServiceType facilityServiceType) {
        return facilityServiceType.getInternalCityId() != null && facilityServiceType.getInternalCityId() > 0
                && facilityServiceType.getInternalStreetId() != null && facilityServiceType.getInternalStreetId() > 0
                && facilityServiceType.getInternalBuildingId() != null && facilityServiceType.getInternalBuildingId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(FacilityServiceType facilityServiceType, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(facilityServiceType, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(FacilityServiceType facilityServiceType,
            long userOrganizationId) throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(facilityServiceType, userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(FacilityServiceType facilityServiceType, String accountNumber,
            long userOrganizationId) {
        personAccountService.updateAccountNumber(facilityServiceType, accountNumber, userOrganizationId);
    }
}
