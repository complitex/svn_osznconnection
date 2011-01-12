/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.actualpayment;

import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.ActualPaymentBean;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

/**
 *
 * @author Artem
 */
public class ActualPaymentLookupPanel extends AbstractLookupPanel<ActualPayment> {

    @EJB(name = "PaymentLookupBean")
    private LookupBean paymentLookupBean;
    @EJB(name = "ActualPaymentBean")
    private ActualPaymentBean actualPaymentBean;

    public ActualPaymentLookupPanel(String id, Component... toUpdate) {
        super(id, toUpdate);
    }

    @Override
    protected void initInternalAddress(ActualPayment actualPayment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, String apartment) {
        actualPayment.setInternalCityId(cityId);
        actualPayment.setInternalStreetId(streetId);
        actualPayment.setInternalStreetTypeId(streetTypeId);
        actualPayment.setInternalBuildingId(buildingId);
        actualPayment.setField(ActualPaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean validateInternalAddress(ActualPayment actualPayment) {
        boolean validated = actualPayment.getInternalCityId() != null && actualPayment.getInternalCityId() > 0
                && actualPayment.getInternalStreetId() != null && actualPayment.getInternalStreetId() > 0
                && actualPayment.getInternalBuildingId() != null && actualPayment.getInternalBuildingId() > 0;
        if (!validated) {
            error(getString("address_required"));
        }
        return validated;
    }

    @Override
    protected void resolveOutgoingAddress(ActualPayment actualPayment) {
        paymentLookupBean.resolveOutgoingAddress(actualPayment);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(ActualPayment actualPayment) throws DBException {
        return paymentLookupBean.acquireAccountDetailsByAddress(actualPayment, actualPayment.getOutgoingDistrict(), actualPayment.getOutgoingStreetType(),
                actualPayment.getOutgoingStreet(), actualPayment.getOutgoingBuildingNumber(), actualPayment.getOutgoingBuildingCorp(),
                actualPayment.getOutgoingApartment(), (Date) actualPayment.getField(ActualPaymentDBF.DAT_BEG));
    }

    @Override
    protected void updateAccountNumber(AjaxRequestTarget target, ActualPayment actualPayment, String accountNumber) {
        actualPayment.setAccountNumber(accountNumber);
        actualPayment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        actualPaymentBean.updateAccountNumber(actualPayment);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByOsznAccount(ActualPayment actualPayment, String account) throws DBException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByMegabankAccount(ActualPayment actualPayment, String account) throws DBException {
        return paymentLookupBean.acquireAccountDetailsByMegabankAccount(actualPayment, actualPayment.getOutgoingDistrict(), account);
    }

    @Override
    protected void setupOutgoingDistrict(ActualPayment actualPayment) {
        paymentLookupBean.setupOutgoingDistrict(actualPayment);
    }
}
