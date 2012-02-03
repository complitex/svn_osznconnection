/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.ibatis.session.SqlSession;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;

import java.util.Date;
import java.util.Set;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 *
 * @author Artem
 */
public class ProcessPaymentTest extends AbstractTest {

    @Override
    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderAdapter() {

            @Override
            protected SqlSession sqlSession(Set<Long> serviceProviderTypeIds) {
                return ProcessPaymentTest.this.getSqlSessionFactoryBean().getSqlSessionManager(serviceProviderTypeIds).openSession(false);
            }

            @Override
            protected Long findInternalOwnership(String calculationCenterOwnership, long calculationCenterId) {
                System.out.println("Original OWN_FRM : " + calculationCenterOwnership);
                return 1L;
            }

            @Override
            protected String findOSZNOwnershipCode(Long internalOwnership, long osznId) {
                return "12";
            }

            @Override
            protected Integer getTarifCode(Double T11_CS_UNI, long organizationId) {
                System.out.println("T11_CS_UNI : " + T11_CS_UNI);
                return 0;
            }
        };
    }

    public static void main(String[] args) throws Exception {
        new ProcessPaymentTest().executeTest(Sets.newHashSet(1L));
    }

    @Override
    protected void test(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws Exception {
        Payment p = new Payment();
        Benefit b = new Benefit();
        b.setId(1L);
        p.setAccountNumber("1000001108");
        p.setOrganizationId(1L);
        p.setField(PaymentDBF.DAT1, new Date());

        CalculationCenterInfo calculationCenterInfo = new CalculationCenterInfo();
        calculationCenterInfo.setOrganizationId(2L);
        calculationCenterInfo.setServiceProviderTypeIds(serviceProviderTypeIds);
        try {
            adapter.processPaymentAndBenefit(calculationCenterInfo, p, Lists.newArrayList(b));
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + p.getStatus() + ", FROG : " + p.getField(PaymentDBF.FROG) + ", FL_PAY : " + p.getField(PaymentDBF.FL_PAY)
                + ", NM_PAY : " + p.getField(PaymentDBF.NM_PAY) + ", DEBT : " + p.getField(PaymentDBF.DEBT) + ", NUMB : " + p.getField(PaymentDBF.NUMB)
                + ", MARK : " + p.getField(PaymentDBF.MARK) + ", HOSTEL : " + b.getField(BenefitDBF.HOSTEL)
                + ", OWN_FRM : " + b.getField(BenefitDBF.OWN_FRM)
                + ", NORM_F_1 : " + p.getField(PaymentDBF.NORM_F_1)
                + ", NORM_F_2 : " + p.getField(PaymentDBF.NORM_F_2)
                + ", CODE2_1 : " + p.getField(PaymentDBF.CODE2_1)
                + ", CODE2_2 : " + p.getField(PaymentDBF.CODE2_2));
    }
}
