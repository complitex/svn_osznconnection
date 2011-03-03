/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.util.Date;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;

/**
 *
 * @author Artem
 */
public class ProcessActualPaymentTest extends AbstractTest {

    public static void main(String[] args) throws Exception {
        new ProcessActualPaymentTest().executeTest();
    }

    @Override
    protected void test(ICalculationCenterAdapter adapter) throws Exception {
        ActualPayment p = new ActualPayment();
        p.setAccountNumber("1000000000");
        try {
            adapter.processActualPayment(p, new Date());
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + p.getStatus() + ", P1 : " + p.getField(ActualPaymentDBF.P1) + ", N1 : " + p.getField(ActualPaymentDBF.N1));
    }
}
