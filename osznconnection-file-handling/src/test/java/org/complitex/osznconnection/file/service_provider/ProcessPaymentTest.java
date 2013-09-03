/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class ProcessPaymentTest extends AbstractTest {

    @Override
    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderTestAdapter() {

            @Override
            protected Long findInternalOwnership(String calculationCenterOwnership, long calculationCenterId) {
                System.out.println("Original OWN_FRM : " + calculationCenterOwnership);
                return 1L;
            }

            @Override
            protected String findOSZNOwnershipCode(Long internalOwnership, long osznId, long userOrganizationId) {
                return "12";
            }

            @Override
            protected String getSubsidyTarifCode(BigDecimal T11_CS_UNI, long organizationId, long userOrganizationId) {
                System.out.println("T11_CS_UNI : " + T11_CS_UNI);
                return "0";
            }
        };
    }

    public static void main(String[] args) throws Exception {
        new ProcessPaymentTest().executeTest();
    }

    @Override
    protected void test(ServiceProviderAdapter adapter) throws Exception {
        Payment p = new Payment() {

            @Override
            public <T> T getField(PaymentDBF paymentDBF) {
                if (paymentDBF == PaymentDBF.DAT1) {
                    return (T) new Date();
                }
                throw new IllegalStateException();
            }

            @Override
            protected void setField(String fieldName, Object object) {
                dbfFields.put(fieldName, object != null ? object.toString() : null);
            }
        };
        Benefit b = new Benefit() {

            @Override
            protected void setField(String fieldName, Object object) {
                dbfFields.put(fieldName, object != null ? object.toString() : null);
            }
        };
        b.setId(1L);
        p.setAccountNumber("1000001108");
        p.setOrganizationId(1L);

        try {
            adapter.processPaymentAndBenefit(new CalculationContext(0L, 3L, 2L, "test", ImmutableSet.of(1L)), p,
                    Lists.newArrayList(b));
        } catch (DBException e) {
            System.out.println("DB error.");
            throw new RuntimeException(e);
        }
        System.out.println("Status : " + p.getStatus() + ", FROG : " + p.getStringField(PaymentDBF.FROG) + ", FL_PAY : " + p.getStringField(PaymentDBF.FL_PAY)
                + ", NM_PAY : " + p.getStringField(PaymentDBF.NM_PAY) + ", DEBT : " + p.getStringField(PaymentDBF.DEBT) + ", NUMB : " + p.getStringField(PaymentDBF.NUMB)
                + ", MARK : " + p.getStringField(PaymentDBF.MARK) + ", HOSTEL : " + b.getStringField(BenefitDBF.HOSTEL)
                + ", OWN_FRM : " + b.getStringField(BenefitDBF.OWN_FRM)
                + ", NORM_F_1 : " + p.getStringField(PaymentDBF.NORM_F_1)
                + ", NORM_F_2 : " + p.getStringField(PaymentDBF.NORM_F_2)
                + ", CODE2_1 : " + p.getStringField(PaymentDBF.CODE2_1)
                + ", CODE2_2 : " + p.getStringField(PaymentDBF.CODE2_2));
    }
}
