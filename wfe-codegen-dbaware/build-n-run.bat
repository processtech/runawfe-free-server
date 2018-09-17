@echo off
cd %~dp0

rem UNTESTED! Not sure how to pass 2 args.
mvn compile exec:java -Dexec.args="%1 %2"
