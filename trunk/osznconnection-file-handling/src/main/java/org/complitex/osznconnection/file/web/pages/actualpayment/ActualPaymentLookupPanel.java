/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.actualpayment;

import org.apache.wicket.Component;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class ActualPaymentLookupPanel extends AbstractLookupPanel<ActualPayment> {

    @EJB
    private LookupBean lookupBean;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private ActualPaymentBean actualPaymentBean;

    public ActualPaymentLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(ActualPayment actualPayment, Long cityId, Long streetId, Long streetTypeId,
            Long buildingId, String apartment) {
        actualPayment.setCityObjectId(cityId);
        actualPayment.setStreetObjectId(streetId);
        actualPayment.setStreetTypeObjectId(streetTypeId);
        actualPayment.setBuildingObjectId(buildingId);
        actualPayment.setField(ActualPaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(ActualPayment actualPayment) {
        return actualPayment.getCityObjectId() != null && actualPayment.getCityObjectId() > 0
                && actualPayment.getStreetObjectId() != null && actualPayment.getStreetObjectId() > 0
                && actualPayment.getBuildingObjectId() != null && actualPayment.getBuildingObjectId() > 0;
    }

    @Override
    protected void resolveOutgoingAddress(ActualPayment actualPayment, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(actualPayment, userOrganizationId);
    }

    @Override
    protected Cursor<AccountDetail> getAccountDetails(ActualPayment actualPayment, long userOrganizationId)
            throws DBException {
        RequestFile actualPaymentFile = requestFileBean.findById(actualPayment.getRequestFileId());

        return lookupBean.getAccountDetails(actualPayment.getOutgoingDistrict(),
                actualPayment.getOutgoingStreetType(), actualPayment.getOutgoingStreet(),
                actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), actualPaymentBean.getFirstDay(actualPayment, actualPaymentFile),
                userOrganizationId);
    }

    @Override
    protected void updateAccountNumber(ActualPayment actualPayment, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(actualPayment, accountNumber, userOrganizationId);
    }
}
