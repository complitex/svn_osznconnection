/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

import java.util.Date;

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
        ActualPayment p = new ActualPayment() {

            @Override
            protected void setField(String fieldName, Object object) {
                dbfFields.put(fieldName, object != null ? object.toString() : null);
            }
        };
        p.setAccountNumber("1000000000");
        try {
            adapter.processActualPayment(new CalculationContext(0L, 3L, 2L, "test", ImmutableSet.of(1L)), p, new Date());
        } catch (DBException e) {
            System.out.println("DB error.");
            throw new RuntimeException(e);
        }
        System.out.println("Status : " + p.getStatus()
                + ", P1 : " + p.getStringField(ActualPaymentDBF.P1) + ", N1 : " + p.getStringField(ActualPaymentDBF.N1)
                + ", P2 : " + p.getStringField(ActualPaymentDBF.P2) + ", N2 : " + p.getStringField(ActualPaymentDBF.N2));
    }
}
