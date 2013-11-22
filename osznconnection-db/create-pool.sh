#!/bin/sh

GLASSFISH_ASADMIN=asadmin

echo ---------------------------------------------------
echo Local database and Realm
echo ---------------------------------------------------
echo
echo Register the JDBC connection pool
$GLASSFISH_ASADMIN create-jdbc-connection-pool --datasourceclassname="com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource" --restype="javax.sql.ConnectionPoolDataSource" --property="url=jdbc\:mysql\://localhost\:3306/osznconnection:user=osznconnection:password=osznconnection:characterResultSets=utf8:characterEncoding=utf8:useUnicode=true" osznconnectionPool

echo
echo Create a JDBC resource with the specified JNDI name
$GLASSFISH_ASADMIN create-jdbc-resource --connectionpoolid osznconnectionPool jdbc/osznconnectionResource

echo
echo Add the named authentication realm
$GLASSFISH_ASADMIN create-auth-realm --classname="com.sun.enterprise.security.ee.auth.realm.jdbc.JDBCRealm" --property="jaas-context=jdbcRealm:datasource-jndi=jdbc/osznconnectionResource:user-table=user:user-name-column=login:password-column=password:group-table=usergroup:group-name-column=group_name:charset=UTF-8:digest-algorithm=MD5" osznconnectionRealm
 
echo
echo ---------------------------------------------------
echo Remote database
echo ---------------------------------------------------
echo
echo Register the JDBC connection pool
$GLASSFISH_ASADMIN create-jdbc-connection-pool --driverclassname oracle.jdbc.OracleDriver --restype java.sql.Driver --property url=jdbc\:oracle\:thin\:@192.168.1.102\:1521\:cnhar:user=comp:password=comp osznconnectionRemotePool

echo
echo Create a JDBC resource with the specified JNDI name
$GLASSFISH_ASADMIN create-jdbc-resource --connectionpoolid osznconnectionRemotePool jdbc/osznconnectionRemoteResource