/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Lists;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.complitex.osznconnection.file.entity.Benefit;

import java.util.Date;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.BenefitDBF;

/**
 *
 * @author Artem
 */
public class ProcessBenefitTest extends AbstractTest {

    @Override
    protected ICalculationCenterAdapter newAdapter(final SqlSessionFactory sqlSessionFactory) {
        return new DefaultCalculationCenterAdapter() {

            @Override
            protected SqlSession sqlSession() {
                return sqlSessionFactory.openSession(false);
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
        new ProcessBenefitTest().executeTest();
    }

    @Override
    protected void test(ICalculationCenterAdapter adapter) throws Exception {
        Benefit b = new Benefit();
        b.setAccountNumber("1000000000");
        b.setField(BenefitDBF.IND_COD, "2142426432");
        b.setOrganizationId(1L);
        try {
            adapter.processBenefit(new Date(), Lists.newArrayList(b), 2);
        } catch (DBException e) {
            System.out.println("DB error.");
        }
        System.out.println("Status : " + b.getStatus());
    }
}
