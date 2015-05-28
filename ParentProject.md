# Основные модули #

  * complitex-address
  * complitex-admin
  * complitex-dictionary
  * complitex-images
  * complitex-logging
  * complitex-resources
  * complitex-template
находятся в отдельном репозиторий http://code.google.com/p/complitex/

Зависимости к этим модулям автоматически разрешаются с помощью механизма наследования и модульности maven:
```
<parent>
    <groupId>org.complitex</groupId>
    <artifactId>complitex</artifactId>
    <version>2.0-SNAPSHOT</version>
    <relativePath>../../complitex/trunk/pom.xml</relativePath>
</parent>

<modules>
    <module>osznconnection-organization</module>
    <module>osznconnection-ownership</module>
    <module>osznconnection-privilege</module>
    <module>osznconnection-file-handling</module>
    <module>osznconnection-web</module>

    <module>../complitex/complitex-dictionary</module>
    <module>../complitex/complitex-template</module>
    <module>../complitex/complitex-admin</module>
    <module>../complitex/complitex-address</module>
    <module>../complitex/complitex-logging</module>
    <module>../complitex/complitex-resources</module>
    <module>../complitex/complitex-images</module>
</modules>
```

По умолчанию проекты должны находиться в одной папке и иметь названия `complitex` и `osznconnection`:

```
# my_complitex_projects
# |
# `--complitex       <-- checkout from http://complitex.googlecode.com/svn/trunk/
# |   
# `--osznconnection  <-- checkout from http://osznconnection.googlecode.com/svn/trunk/
```

![http://osznconnection.googlecode.com/svn/wiki/images/overview.png](http://osznconnection.googlecode.com/svn/wiki/images/overview.png)