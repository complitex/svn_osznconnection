/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import java.util.Date;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.service.LookupBean;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.Component;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.web.component.lookup.AbstractLookupPanel;

/**
 * Панель для поиска номера л/c по различным параметрам: по адресу, по номеру лиц. счета, по номеру в мегабанке.
 * @author Artem
 */
public class PaymentLookupPanel extends AbstractLookupPanel<Payment> {

    @EJB(name = "LookupBean")
    private LookupBean lookupBean;
    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;

    public PaymentLookupPanel(String id, Component... toUpdate) {
        super(id, toUpdate);
    }

    @Override
    protected void initInternalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, String apartment) {
        payment.setInternalCityId(cityId);
        payment.setInternalStreetId(streetId);
        payment.setInternalStreetTypeId(streetTypeId);
        payment.setInternalBuildingId(buildingId);
        payment.setField(PaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    @Override
    protected boolean isInternalAddressCorrect(Payment payment) {
        return payment.getInternalCityId() != null && payment.getInternalCityId() > 0
                && payment.getInternalStreetId() != null && payment.getInternalStreetId() > 0
                && payment.getInternalBuildingId() != null && payment.getInternalBuildingId() > 0;
    }

    @Override
    protected void updateAccountNumber(Payment payment, String accountNumber) {
        personAccountService.updateAccountNumber(payment, accountNumber);
    }

    @Override
    protected void resolveOutgoingAddress(Payment payment) {
        lookupBean.resolveOutgoingAddress(payment);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByAddress(Payment payment) throws DBException {
        return lookupBean.acquireAccountDetailsByAddress(payment, payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1));
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByOsznAccount(Payment payment, String account) throws DBException {
        payment.setField(PaymentDBF.OWN_NUM_SR, account);
        return lookupBean.acquireAccountDetailsByOsznAccount(payment);
    }

    @Override
    protected List<AccountDetail> acquireAccountDetailsByMegabankAccount(Payment payment, String account) throws DBException {
        return lookupBean.acquireAccountDetailsByMegabankAccount(payment, payment.getOutgoingDistrict(), account);
    }

    @Override
    protected void resolveOutgoingDistrict(Payment payment) {
        lookupBean.resolveOutgoingDistrict(payment);
    }

    @Override
    protected boolean lookupByOwnNumSr() {
        return true;
    }
}
