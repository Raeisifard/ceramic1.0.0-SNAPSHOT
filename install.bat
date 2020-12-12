echo off
cd /D "%~dp0"
echo Current working directory: %cd%
mvn install:install-file -Dfile=.\lib\vertx-sse-master-3.9.4.jar -DgroupId=io.vertx -DartifactId=vertx-sse -Dversion=1.0.0 -Dpackaging=jar
