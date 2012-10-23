SET DRIVER=../../../complitex/complitex-db/mysql-connector-java-5.1.20.jar
SET DATABASE=osznconnection

java -jar schemaSpy_5.0.0.jar ^
  -t mysql -dp %DRIVER% ^
  -host localhost -u root -p root ^
  -db %DATABASE% ^
  -gv "C:\Program Files (x86)\Graphviz 2.28" ^
  -o %DATABASE% -ahic -hq -charset utf8 