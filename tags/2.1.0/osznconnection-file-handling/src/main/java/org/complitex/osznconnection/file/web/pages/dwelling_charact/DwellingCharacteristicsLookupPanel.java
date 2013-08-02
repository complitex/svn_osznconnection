/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.dwelling_charact;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.DwellingCharacteristics;
import org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

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
        dwellingCharacteristics.setInternalCityId(cityId);
        dwellingCharacteristics.setInternalStreetId(streetId);
        dwellingCharacteristics.setInternalStreetTypeId(streetTypeId);
        dwellingCharacteristics.setInternalBuildingId(buildingId);
        dwellingCharacteristics.setField(DwellingCharacteristicsDBF.APT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(DwellingCharacteristics dwellingCharacteristics) {
        return dwellingCharacteristics.getInternalCityId() != null && dwellingCharacteristics.getInternalCityId() > 0
                && dwellingCharacteristics.getInternalStreetId() != null && dwellingCharacteristics.getInternalStreetId() > 0
                && dwellingCharacteristics.getInternalBuildingId() != null && dwellingCharacteristics.getInternalBuildingId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(dwellingCharacteristics, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId)
            throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(dwellingCharacteristics, userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(DwellingCharacteristics dwellingCharacteristics, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(dwellingCharacteristics, accountNumber, userOrganizationId);
    }

    @Override
    protected String resolveOutgoingDistrict(DwellingCharacteristics dwellingCharacteristics, long userOrganizationId) {
        return lookupBean.resolveOutgoingDistrict(dwellingCharacteristics, userOrganizationId);
    }
}
