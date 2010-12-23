/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.complitex.osznconnection.file.entity.Payment;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.PaymentDBF;

/**
 *
 * @author Artem
 */
public class AcquireAccountCorrectionDetailsTest {

    private static SqlSessionFactory sqlSessionFactory;
    private static ICalculationCenterAdapter adapter;

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

        adapter = new DefaultCalculationCenterAdapter() {

            @Override
            protected SqlSession sqlSession() {
                return sqlSessionFactory.openSession(false);
            }
        };
    }

    public static void main(String[] args) {
        init();

        try {
            testByOsznAccount();
        } catch (DBException e) {
            System.out.println("DB error.");
        }
    }

    private static void testByAddress() throws DBException {
        System.out.println(adapter.acquireAccountDetailsByAddress(newPayment()));
    }

    private static void testByOsznAccount() throws DBException {
        System.out.println(adapter.acquireAccountDetailsByOsznAccount(newPayment()));
    }

    private static void testByMegabankAccount() throws DBException {
        System.out.println(adapter.acquireAccountDetailsByMegabankAccount(newPayment(), "9876543"));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        p.setId(1L);
        p.setOutgoingDistrict("ЦЕНТРАЛЬНЫЙ");
        p.setOutgoingStreet("ФРАНТИШЕКА КРАЛА");
        p.setOutgoingStreetType("УЛ");
        p.setOutgoingBuildingNumber("25А");
        p.setOutgoingBuildingCorp("");
        p.setOutgoingApartment("40");
        p.setField(PaymentDBF.DAT1, new Date());
        p.setField(PaymentDBF.OWN_NUM_SR, "1234567");
        return p;
    }
}
