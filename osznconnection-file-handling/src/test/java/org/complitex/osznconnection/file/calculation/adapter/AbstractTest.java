/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 *
 * @author Artem
 */
public abstract class AbstractTest {

    private SqlSessionFactory sqlSessionFactory;

    protected void init() {
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader("mybatis-test.xml");
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

    protected ICalculationCenterAdapter newAdapter(final SqlSessionFactory sqlSessionFactory) {
        return new DefaultCalculationCenterAdapter() {

            @Override
            protected SqlSession sqlSession() {
                return sqlSessionFactory.openSession(false);
            }
        };
    }

    protected abstract void test(ICalculationCenterAdapter adapter) throws Exception;

    public void executeTest() throws Exception {
        init();
        test(newAdapter(sqlSessionFactory));
    }
}
