@echo off

:: Set the project path
IF "%~1"=="" (SET "POM_PATH=..\..\pom.xml") ELSE (SET "POM_PATH=..\..\%~1\pom.xml")

:: Run build without the sign-artifacts profile
mvn -f %POM_PATH% clean install -Dmaven.test.skip=true