/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.osznconnection.service_provider_type.strategy.ServiceProviderTypeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ServiceProviderSqlSessionFactoryBean {

    private static final Logger log = LoggerFactory.getLogger(ServiceProviderSqlSessionFactoryBean.class);
    private static final String SERVICE_PROVIDER_TYPE_ENVIRONMENT_PREFIX = "service_provider_type_";
    private Map<Long, SqlSessionManager> sqlSessionManagerMap;

    @PostConstruct
    protected void init() {
        sqlSessionManagerMap = new ConcurrentHashMap<Long, SqlSessionManager>();

        final String configurationFile = getConfigurationFile();

        for (long serviceProviderTypeId : ServiceProviderTypeStrategy.RESERVED_SERVICE_PROVIDER_TYPES) {
            Reader reader = null;
            try {
                reader = Resources.getResourceAsReader(configurationFile);
                sqlSessionManagerMap.put(serviceProviderTypeId,
                        SqlSessionManager.newInstance(reader, SERVICE_PROVIDER_TYPE_ENVIRONMENT_PREFIX + serviceProviderTypeId));
            } catch (Exception e) {
                log.error("Environment configuration for service provider type " + serviceProviderTypeId + " not found.", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("Could not close reader.", e);
                    }
                }
            }
        }
    }

    public SqlSessionManager getSqlSessionManager(Set<Long> serviceProviderTypeId) {
        SortedSet<Long> sorted = new TreeSet<Long>(serviceProviderTypeId);
        return sqlSessionManagerMap.get(sorted.first());
    }

    protected String getConfigurationFile() {
        return SqlSessionFactoryBean.CONFIGURATION_FILE;
    }
}
