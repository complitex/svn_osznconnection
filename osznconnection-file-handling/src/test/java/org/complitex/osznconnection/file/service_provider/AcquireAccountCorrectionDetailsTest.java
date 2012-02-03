/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.Sets;
import org.complitex.osznconnection.file.entity.Payment;
import java.util.Date;
import java.util.Set;
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
            new AcquireAccountCorrectionDetailsTest().executeTest(Sets.newHashSet(1L));
        } catch (DBException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        } catch (UnknownAccountNumberTypeException e) {
            System.out.println("Unknown account number type exception.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void testByAddress(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws DBException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAddress(serviceProviderTypeIds, payment,
                payment.getOutgoingDistrict(), payment.getOutgoingStreetType(),
                payment.getOutgoingStreet(), payment.getOutgoingBuildingNumber(), payment.getOutgoingBuildingCorp(),
                payment.getOutgoingApartment(), (Date) payment.getField(PaymentDBF.DAT1)));
    }

    private void testByOsznAccount(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws DBException, UnknownAccountNumberTypeException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAccount(serviceProviderTypeIds, payment, payment.getOutgoingDistrict(), "1234567"));
    }

    private void testByMegabankAccount(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws DBException, UnknownAccountNumberTypeException {
        Payment payment = newPayment();
        System.out.println(adapter.acquireAccountDetailsByAccount(serviceProviderTypeIds, payment, payment.getOutgoingDistrict(), "9876543"));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        p.setId(1L);
        p.setOutgoingDistrict("ЦЕНТРАЛЬНЫЙ");
        p.setOutgoingStreet("ФРАНТИШЕКА КРАЛА");
        p.setOutgoingStreetType("УЛ");
        p.setOutgoingBuildingNumber("25А");
        p.setOutgoingBuildingCorp("");
        p.setOutgoingApartment("19");
        p.setField(PaymentDBF.DAT1, new Date());
        p.setField(PaymentDBF.OWN_NUM_SR, "1234567");
        return p;
    }

    @Override
    protected void test(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws Exception {
        testByAddress(serviceProviderTypeIds, adapter);
    }
}
