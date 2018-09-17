@echo off
cd %~dp0
mvn compile exec:java -Dexec.args="%1 %2"
