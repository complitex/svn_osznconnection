/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import org.complitex.osznconnection.file.entity.Payment;
import java.util.Date;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;

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
            throw new RuntimeException(e);
        } catch (UnknownAccountNumberTypeException e) {
            System.out.println("Unknown account number type exception.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void testByAddress(CalculationContext calculationCenterInfo, ServiceProviderAdapter adapter) throws DBException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAddress(calculationCenterInfo, payment,
                payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1)));
    }

    private void testByOsznAccount(CalculationContext calculationCenterInfo, ServiceProviderAdapter adapter) throws DBException, UnknownAccountNumberTypeException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAccount(calculationCenterInfo, payment, payment.getOutgoingDistrict(), "1234567"));
    }

    private void testByMegabankAccount(CalculationContext calculationCenterInfo, ServiceProviderAdapter adapter) throws DBException, UnknownAccountNumberTypeException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAccount(calculationCenterInfo, payment, payment.getOutgoingDistrict(), "9876543"));
    }

    private static Payment newPayment() {
        Payment p = new Payment() {

            @Override
            public <T> T getField(PaymentDBF paymentDBF) {
                if (paymentDBF == PaymentDBF.DAT1) {
                    return (T) new Date();
                } else if (paymentDBF == PaymentDBF.OWN_NUM_SR) {
                    return (T) "1234567";
                } else {
                    throw new IllegalStateException();
                }
            }
        };
        p.setId(1L);
        p.setOutgoingDistrict("ЦЕНТРАЛЬНЫЙ");
        p.setOutgoingStreet("ФРАНТИШЕКА КРАЛА");
        p.setOutgoingStreetType("УЛ");
        p.setOutgoingBuildingNumber("25А");
        p.setOutgoingBuildingCorp("");
        p.setOutgoingApartment("19");
        return p;
    }

    @Override
    protected void test(ServiceProviderAdapter adapter) throws Exception {
        testByAddress(new CalculationContext(2, "test", ImmutableSet.of(1L), 3), adapter);
    }
}
