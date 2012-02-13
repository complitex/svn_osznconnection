/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import org.apache.ibatis.session.SqlSession;

/**
 *
 * @author Artem
 */
public abstract class AbstractTest {

    private ServiceProviderSqlSessionFactoryBean sqlSessionFactoryBean;

    protected class ServiceProviderTestAdapter extends ServiceProviderAdapter {

        @Override
        protected SqlSession sqlSession(String dataSource) {
            return sqlSessionFactoryBean.getSqlSessionManager(dataSource).openSession(false);
        }
    }

    protected AbstractTest() {
        sqlSessionFactoryBean = new ServiceProviderSqlSessionFactoryBean() {

            @Override
            protected String getConfigurationFileName() {
                return "mybatis-remote-config-test.xml";
            }
        };
    }

    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderTestAdapter();
    }

    protected abstract void test(ServiceProviderAdapter adapter) throws Exception;

    public void executeTest() throws Exception {
        test(newAdapter());
    }
}
