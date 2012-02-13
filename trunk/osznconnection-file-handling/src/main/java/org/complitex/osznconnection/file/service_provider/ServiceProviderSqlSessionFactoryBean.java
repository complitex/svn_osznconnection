/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
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

    public static final String CONFIGURATION_FILE_NAME = "mybatis-remote-config.xml";
    private static final Logger log = LoggerFactory.getLogger(ServiceProviderSqlSessionFactoryBean.class);
    private final ConcurrentMap<String, SqlSessionManager> sqlSessionManagerMap;

    public ServiceProviderSqlSessionFactoryBean() {
        sqlSessionManagerMap = new ConcurrentHashMap<String, SqlSessionManager>();
    }

    protected SqlSessionManager newSqlSessionManager(String jdbcDataSource) {
        final String configurationFile = getConfigurationDirectory() + "/" + getConfigurationFileName();
        final Properties props = new Properties();
        props.setProperty("remoteDataSource", jdbcDataSource);
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(configurationFile);
            return SqlSessionManager.newInstance(reader, props);
        } catch (Exception e) {
            log.error("Configuration for jdbc data source " + jdbcDataSource + " couldn't be created.");
            throw new RuntimeException(e);
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

    public SqlSessionManager getSqlSessionManager(String jdbcDataSource) {
        SqlSessionManager sqlSessionManager = sqlSessionManagerMap.get(jdbcDataSource);
        if (sqlSessionManager == null) {
            sqlSessionManager = newSqlSessionManager(jdbcDataSource);
            sqlSessionManagerMap.putIfAbsent(jdbcDataSource, sqlSessionManager);
        }
        return sqlSessionManager;
    }

    protected String getConfigurationFileName() {
        return CONFIGURATION_FILE_NAME;
    }

    protected String getConfigurationDirectory() {
        return getClass().getPackage().getName().replace('.', '/');
    }
}
