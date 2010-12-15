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
import org.complitex.osznconnection.file.entity.PaymentDBF;

/**
 *
 * @author Artem
 */
public class AcquireAccountCorrectionDetailsTest {

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
//            @Override
//            public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) {
//                List<AccountDetail> accountCorrectionDetails = null;
//                SqlSession session = null;
//                try {
//                    session = sqlSession();
//
//                    Map<String, Object> params = Maps.newHashMap();
//                    String districtName = "ДЗЕРЖИНСКИЙ";
//                    params.put("pDistrName", districtName);
//                    params.put("pStSortName", "УЛ");
//                    params.put("pStreetName", "АХСАРОВА");
//                    params.put("pHouseNum", "23");
//                    params.put("pHousePart", "");
//                    params.put("pFlatNum", "240");
//                    params.put("dat1", new Date());
//
//
//
//                    session.commit();
//                } catch (Exception e) {
//                    try {
//                        if (session != null) {
//                            session.rollback();
//                        }
//                    } catch (Exception exc) {
//                        log.error("", exc);
//                    }
//                    log.error("", e);
//                } finally {
//                    try {
//                        if (session != null) {
//                            session.close();
//                        }
//                    } catch (Exception e) {
//                        log.error("", e);
//                    }
//                }
//                return accountCorrectionDetails;
//            }
        };
        try {
            System.out.println(adapter.acquireAccountCorrectionDetails(newPayment()));
        } catch (AccountNotFoundException e) {
            System.out.println("Account not found");
        }
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
        return p;
    }
}
