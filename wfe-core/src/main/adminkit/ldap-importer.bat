@echo off

REM ########Configuration###############

set LOGIN="Administrator"
set PASSWORD="wf"

REM ########End of Configuration###############

set CLASSPATH=".\conf;.\lib\*"

set JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8789,server=y,suspend=n"

java %JAVA_OPTS% -cp %CLASSPATH% ru.runa.wfe.service.client.LDAPImporterClient %LOGIN% %PASSWORD%
