package org.complitex.osznconnection.file.web.pages.dwelling_charact;

import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.DwellingCharacteristics;
import org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF;
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
public class DwellingCharacteristicsLookupPanel extends AbstractLookupPanel<DwellingCharacteristics> {

    @EJB
    private LookupBean lookupBean;

    @EJB
    private PersonAccountService personAccountService;

    public DwellingCharacteristicsLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(DwellingCharacteristics dwellingCharacteristics, Long cityId, Long streetId, Long streetTypeId,
                                       Long buildingId, String apartment) {
        dwellingCharacteristics.setCityObjectId(cityId);
        dwellingCharacteristics.setStreetObjectId(streetId);
        dwellingCharacteristics.setStreetTypeObjectId(streetTypeId);
        dwellingCharacteristics.setBuildingObjectId(buildingId);
        dwellingCharacteristics.setField(DwellingCharacteristicsDBF.APT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(DwellingCharacteristics dwellingCharacteristics) {
        return dwellingCharacteristics.getCityObjectId() != null && dwellingCharacteristics.getCityObjectId() > 0
                && dwellingCharacteristics.getStreetObjectId() != null && dwellingCharacteristics.getStreetObjectId() > 0
                && dwellingCharacteristics.getBuildingObjectId() != null && dwellingCharacteristics.getBuildingObjectId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(dwellingCharacteristics, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(DwellingCharacteristics dwellingCharacteristics,
                                                                 long userOrganizationId)
            throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(dwellingCharacteristics, dwellingCharacteristics.getOutgoingDistrict(),
                dwellingCharacteristics.getOutgoingStreetType(), dwellingCharacteristics.getOutgoingStreet(),
                dwellingCharacteristics.getOutgoingBuildingNumber(), dwellingCharacteristics.getOutgoingBuildingCorp(),
                dwellingCharacteristics.getOutgoingApartment(), dwellingCharacteristics.getDate(), userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(DwellingCharacteristics dwellingCharacteristics, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(dwellingCharacteristics, accountNumber, userOrganizationId);
    }
}
