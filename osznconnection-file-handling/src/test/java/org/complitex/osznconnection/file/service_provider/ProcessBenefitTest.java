/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.complitex.osznconnection.file.entity.Benefit;

import java.util.Date;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

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
        Benefit b = new Benefit();
        b.setAccountNumber("1000000000");
        b.setField(BenefitDBF.IND_COD, "2142426432");
        b.setOrganizationId(1L);

        try {
            adapter.processBenefit(new CalculationContext(2, "test", ImmutableSet.of(1L), 3), new Date(), Lists.newArrayList(b));
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + b.getStatus());
    }
}
