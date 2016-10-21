#!/bin/sh
########Configuration###############
SCRIPT_PATH="scripts/deploy-samples-script.xml"
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/*"

if [ "$JAVA_HOME" != "" ]
then
	JAVA=$JAVA_HOME/bin/java
else
	JAVA=java
fi

JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8789,server=y,suspend=n"

$JAVA $JAVA_OPTS -cp ${CLASSPATH} ru.runa.wfe.service.client.AdminScriptClient ${SCRIPT_PATH} ${LOGIN} ${PASSWORD}
