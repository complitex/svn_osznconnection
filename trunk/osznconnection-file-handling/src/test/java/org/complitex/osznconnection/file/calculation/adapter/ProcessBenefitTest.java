/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Lists;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.complitex.osznconnection.file.entity.Benefit;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.BenefitDBF;

/**
 *
 * @author Artem
 */
public class ProcessBenefitTest {

    private static SqlSessionFactory sqlSessionFactory;

    private static void init() {
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader("Configuration-test.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "remote");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        init();

        ICalculationCenterAdapter adapter = new DefaultCalculationCenterAdapter() {

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
        Benefit b = new Benefit();
        b.setAccountNumber("1000000000");
        b.setField(BenefitDBF.IND_COD, "2142426432");
        b.setOrganizationId(1L);
        try {
            adapter.processBenefit(new Date(), Lists.newArrayList(b), 2);
        } catch (DBException e){
            System.out.println("DB error.");
        }
        System.out.println("Status : " + b.getStatus());
    }
}
