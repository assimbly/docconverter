@echo off

IF "%~1"=="" GOTO :BUILDALL
mvn -f ..\..\%~1\pom.xml clean install -Dmaven.test.skip=true
:BUILDALL
mvn -f ..\..\pom.xml clean install -Dmaven.test.skip=true