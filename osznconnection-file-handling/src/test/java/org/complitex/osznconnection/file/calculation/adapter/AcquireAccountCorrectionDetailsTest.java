/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.complitex.osznconnection.file.entity.Payment;

import java.util.Date;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.PaymentDBF;

/**
 *
 * @author Artem
 */
public class AcquireAccountCorrectionDetailsTest extends AbstractTest {

    public static void main(String[] args) {
        try {
            new AcquireAccountCorrectionDetailsTest().executeTest();
        } catch (DBException e) {
            System.out.println("DB error.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void testByAddress(ICalculationCenterAdapter adapter) throws DBException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAddress(payment, payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1)));
    }

    private void testByOsznAccount(ICalculationCenterAdapter adapter) throws DBException {
        System.out.println(adapter.acquireAccountDetailsByOsznAccount(newPayment()));
    }

    private void testByMegabankAccount(ICalculationCenterAdapter adapter) throws DBException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByMegabankAccount(payment, payment.getOutgoingDistrict(), "9876543"));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        p.setId(1L);
        p.setOutgoingDistrict("ЦЕНТРАЛЬНЫЙ");
        p.setOutgoingStreet("ФРАНТИШЕКА КРАЛА");
        p.setOutgoingStreetType("УЛ");
        p.setOutgoingBuildingNumber("25А");
        p.setOutgoingBuildingCorp("");
        p.setOutgoingApartment("40");
        p.setField(PaymentDBF.DAT1, new Date());
        p.setField(PaymentDBF.OWN_NUM_SR, "1234567");
        return p;
    }

    @Override
    protected void test(ICalculationCenterAdapter adapter) throws Exception {
        testByOsznAccount(adapter);
    }
}
