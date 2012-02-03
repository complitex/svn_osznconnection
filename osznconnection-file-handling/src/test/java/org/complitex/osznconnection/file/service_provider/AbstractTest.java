/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import java.util.Set;
import org.apache.ibatis.session.SqlSession;

/**
 *
 * @author Artem
 */
public abstract class AbstractTest {

    private ServiceProviderSqlSessionFactoryBean sqlSessionFactoryBean;

    protected AbstractTest() {
        sqlSessionFactoryBean = new ServiceProviderSqlSessionFactoryBean() {

            @Override
            protected String getConfigurationFile() {
                return "mybatis-test.xml";
            }
        };
        sqlSessionFactoryBean.init();
    }

    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderAdapter() {

            @Override
            protected SqlSession sqlSession(Set<Long> serviceProviderTypeIds) {
                return sqlSessionFactoryBean.getSqlSessionManager(serviceProviderTypeIds).openSession(false);
            }

            @Override
            protected String displayServiceProviderTypes(Set<Long> serviceProviderTypeIds) {
                return serviceProviderTypeIds.toString();
            }
        };
    }

    protected ServiceProviderSqlSessionFactoryBean getSqlSessionFactoryBean() {
        return sqlSessionFactoryBean;
    }

    protected abstract void test(Set<Long> serviceProviderTypeIds, ServiceProviderAdapter adapter) throws Exception;

    public void executeTest(Set<Long> serviceProviderTypeIds) throws Exception {
        test(serviceProviderTypeIds, newAdapter());
    }
}
