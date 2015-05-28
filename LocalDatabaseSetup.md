# Setting up the DB #

1. Create _osznconnection_ user like this (in a totally insecure way):

```
GRANT ALL PRIVILEGES ON *.* TO 'osznconnection'@'localhost' IDENTIFIED BY 'osznconnection' WITH GRANT OPTION;
```

2. Create database in UTF-8:

```
CREATE DATABASE osznconnection DEFAULT CHARACTER SET utf8;
```

Alternatively, specify default character set in server config:

```
[mysqld]
default-character-set=utf8
```

# Mock data #

See [Ant script](http://code.google.com/p/osznconnection/source/browse/trunk/osznconnection-db/build.xml) in the repository.