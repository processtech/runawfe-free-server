#!/bin/sh

########Configuration###############
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/*"

java -cp ${CLASSPATH} ru.runa.wfe.service.client.LDAPImporterClient ${LOGIN} ${PASSWORD}
