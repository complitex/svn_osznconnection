/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

import java.util.Date;

/**
 *
 * @author Artem
 */
public class ProcessBenefitTest extends AbstractTest {

    @Override
    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderTestAdapter() {

            @Override
            protected Long findInternalPrivilege(String calculationCenterPrivilege, long calculationCenterId) {
                System.out.println("calculationCenterPrivilege code : " + calculationCenterPrivilege);
                return 1L;
            }

            @Override
            protected String findOSZNPrivilegeCode(Long internalPrivilege, long osznId, long userOrganizationId) {
                return "11";
            }
        };
    }

    public static void main(String[] args) throws Exception {
        new ProcessBenefitTest().executeTest();
    }

    @Override
    protected void test(ServiceProviderAdapter adapter) throws Exception {
        Benefit b = new Benefit() {

            @Override
            public void setField(String fieldName, Object object) {
                getDbfFields().put(fieldName, object != null ? object.toString() : null);
            }

            @Override
            public Object getField(BenefitDBF benefitDBF) {
                if (benefitDBF == BenefitDBF.IND_COD) {
                    return "2142426432";
                }
                throw new IllegalStateException();
            }
        };
        b.setAccountNumber("1000000000");
        b.setOrganizationId(1L);

        try {
            adapter.processBenefit(new CalculationContext(0L, 3L, 2L, "test", ImmutableSet.of(1L)), new Date(),
                    Lists.newArrayList(b));
        } catch (DBException e) {
            System.out.println("DB error.");
            throw new RuntimeException(e);
        }
        System.out.println("Status : " + b.getStatus());
    }
}
