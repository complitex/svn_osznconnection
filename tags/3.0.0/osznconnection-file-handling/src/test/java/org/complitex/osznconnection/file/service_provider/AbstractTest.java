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
    protected class ServiceProviderTestAdapter extends ServiceProviderAdapter {

        @Override
        protected SqlSession sqlSession(String dataSource) {
            return null;
        }
    }

    protected AbstractTest() {
    }

    protected ServiceProviderAdapter newAdapter() {
        return new ServiceProviderTestAdapter();
    }

    protected abstract void test(ServiceProviderAdapter adapter) throws Exception;

    public void executeTest() throws Exception {
        test(newAdapter());
    }
}
