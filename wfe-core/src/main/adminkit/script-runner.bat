@echo off

REM ########Configuration###############

set SCRIPT_PATH="scripts/deploy-samples-script.xml"
set LOGIN="Administrator"
set PASSWORD="wf"

REM ########End of Configuration###############

set CLASSPATH=".\conf;.\lib\*"

set JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8789,server=y,suspend=n"

java %JAVA_OPTS% -cp %CLASSPATH% ru.runa.wfe.service.client.AdminScriptClient %SCRIPT_PATH% %LOGIN% %PASSWORD%



