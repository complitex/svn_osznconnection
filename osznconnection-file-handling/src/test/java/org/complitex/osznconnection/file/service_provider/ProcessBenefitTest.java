/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.complitex.osznconnection.file.entity.Benefit;

import java.util.Date;
import java.util.Set;
import org.apache.ibatis.session.SqlSession;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.service_provider.exception.DBException;

/**
 *
 * @author Artem
 */
public class ProcessBenefitTest extends AbstractTest {

    @Override
    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderAdapter() {

            @Override
            protected SqlSession sqlSession(Set<Long> serviceProviderTypeIds) {
                return ProcessBenefitTest.this.getSqlSessionFactoryBean().getSqlSessionManager(serviceProviderTypeIds).openSession(false);
            }

            @Override
            protected Long findInternalPrivilege(String calculationCenterPrivilege, long calculationCenterId) {
                System.out.println("calculationCenterPrivilege code : " + calculationCenterPrivilege);
                return 1L;
            }

            @Override
            protected String findOSZNPrivilegeCode(Long internalPrivilege, long osznId) {
                return "11";
            }
        };
    }

    public static void main(String[] args) throws Exception {
        new ProcessBenefitTest().executeTest(Sets.newHashSet(1L));
    }

    @Override
    protected void test(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws Exception {
        Benefit b = new Benefit();
        b.setAccountNumber("1000000000");
        b.setField(BenefitDBF.IND_COD, "2142426432");
        b.setOrganizationId(1L);

        CalculationCenterInfo calculationCenterInfo = new CalculationCenterInfo();
        calculationCenterInfo.setOrganizationId(2L);
        calculationCenterInfo.setServiceProviderTypeIds(serviceProviderTypeIds);
        try {
            adapter.processBenefit(calculationCenterInfo, new Date(), Lists.newArrayList(b));
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + b.getStatus());
    }
}
