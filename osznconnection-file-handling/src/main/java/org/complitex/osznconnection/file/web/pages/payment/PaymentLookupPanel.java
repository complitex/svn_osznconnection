/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.apache.wicket.Component;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.service.LookupBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель для поиска номера л/c по различным параметрам: по адресу, по номеру лиц. счета, по номеру в мегабанке.
 * @author Artem
 */
public class PaymentLookupPanel extends AbstractLookupPanel<Payment> {

    @EJB
    private LookupBean lookupBean;

    @EJB
    private PersonAccountService personAccountService;



    public PaymentLookupPanel(String id, long userOrganizationId, Component... toUpdate) {
        super(id, userOrganizationId, toUpdate);
    }

    @Override
    protected void initInternalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, String apartment) {
        payment.setCityObjectId(cityId);
        payment.setStreetObjectId(streetId);
        payment.setStreetTypeObjectId(streetTypeId);
        payment.setBuildingObjectId(buildingId);
        payment.setField(PaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(Payment payment) {
        return payment.getCityObjectId() != null && payment.getCityObjectId() > 0
                && payment.getStreetObjectId() != null && payment.getStreetObjectId() > 0
                && payment.getBuildingObjectId() != null && payment.getBuildingObjectId() > 0;
    }

    @Override
    protected void updateAccountNumber(Payment payment, String accountNumber, long userOrganizationId) {
        personAccountService.updateAccountNumber(payment, accountNumber, userOrganizationId);
    }

    @Override
    protected void resolveOutgoingAddress(Payment payment, long userOrganizationId) {
        lookupBean.resolveOutgoingAddress(payment, userOrganizationId);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(Payment payment, long userOrganizationId) throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(payment, userOrganizationId);
    }
}
