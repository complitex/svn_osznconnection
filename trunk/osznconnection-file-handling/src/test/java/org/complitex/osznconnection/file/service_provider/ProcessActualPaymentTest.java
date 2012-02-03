/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Set;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 *
 * @author Artem
 */
public class ProcessActualPaymentTest extends AbstractTest {

    public static void main(String[] args) throws Exception {
        new ProcessActualPaymentTest().executeTest(Sets.newHashSet(1L));
    }

    @Override
    protected void test(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws Exception {
        ActualPayment p = new ActualPayment();
        p.setAccountNumber("1000000000");
        try {
            adapter.processActualPayment(serviceProviderTypeIds, p, new Date());
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + p.getStatus()
                + ", P1 : " + p.getField(ActualPaymentDBF.P1) + ", N1 : " + p.getField(ActualPaymentDBF.N1)
                + ", P2 : " + p.getField(ActualPaymentDBF.P2) + ", N2 : " + p.getField(ActualPaymentDBF.N2));
    }
}
