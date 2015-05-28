# Local database and Realm #

## Register the JDBC connection pool ##
```
C:\glassfishv3\glassfish\bin>asadmin.bat create-jdbc-connection-pool --driverclassname com.mysql.jdbc.Driver --restype java.sql.Driver --property url=jdbc\:mysql\://localhost\:3306/osznconnection:user=osznconnection:password=osznconnection:characterResultSets=utf8:characterEncoding=utf8:useUnicode=true osznconnectionPool
```

## Create a JDBC resource with the specified JNDI name ##
```
C:\glassfishv3\glassfish\bin>asadmin.bat create-jdbc-resource
--connectionpoolid osznconnectionPool jdbc/osznconnectionResource
```

## Add the named authentication realm ##
```
C:\glassfishv3\glassfish\bin>asadmin.bat create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=jdbc/osznconnectionResource:user-table=user:user-name-column=login:password-column=password:group-table=usergroup:group-name-column=group_name:charset=UTF-8:digest-algorithm=MD5 osznconnectionRealm
```


# Remote database #

## Register the JDBC connection pool ##
```
C:\glassfishv3\glassfish\bin>asadmin.bat create-jdbc-connection-pool 
--driverclassname oracle.jdbc.OracleDriver 
--restype java.sql.Driver 
--property url=jdbc\:oracle\:thin\:@192.168.1.102\:1521\:cnhar:user=comp:password=comp osznconnectionRemotePool
```

## Create a JDBC resource with the specified JNDI name ##
```
C:\glassfishv3\glassfish\bin>asadmin.bat create-jdbc-resource
--connectionpoolid osznconnectionPool jdbc/osznconnectionRemoteResource
```