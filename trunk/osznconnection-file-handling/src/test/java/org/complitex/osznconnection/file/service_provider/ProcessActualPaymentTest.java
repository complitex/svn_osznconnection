/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import java.util.Date;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 *
 * @author Artem
 */
public class ProcessActualPaymentTest extends AbstractTest {

    public static void main(String[] args) throws Exception {
        new ProcessActualPaymentTest().executeTest();
    }

    @Override
    protected void test(ServiceProviderAdapter adapter) throws Exception {
        ActualPayment p = new ActualPayment();
        p.setAccountNumber("1000000000");
        try {
            adapter.processActualPayment(new CalculationCenterInfo(2, "test", ImmutableSet.of(1L)), p, new Date());
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + p.getStatus()
                + ", P1 : " + p.getField(ActualPaymentDBF.P1) + ", N1 : " + p.getField(ActualPaymentDBF.N1)
                + ", P2 : " + p.getField(ActualPaymentDBF.P2) + ", N2 : " + p.getField(ActualPaymentDBF.N2));
    }
}
