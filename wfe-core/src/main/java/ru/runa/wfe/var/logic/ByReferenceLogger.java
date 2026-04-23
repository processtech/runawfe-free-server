package ru.runa.wfe.var.logic;

import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.audit.Attributes;
import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;

@CommonsLog
class ByReferenceLogger {

    private static final int MAX_LOG_VALUE_LENGTH = 4000;
    private static final int LOG_TRUNCATION_SUFFIX_LENGTH = 3;

    private final ProcessLogDao processLogDao;
    private final CurrentProcess currentProcess;
    private final CurrentToken currentToken;

    ByReferenceLogger(ProcessLogDao processLogDao, CurrentProcess currentProcess, CurrentToken currentToken) {
        this.processLogDao = processLogDao;
        this.currentProcess = currentProcess;
        this.currentToken = currentToken;
    }

    void logVariable(String operation, VariableDefinition variableDefinition, Long id, UserTypeMap fullMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("byReference ").append(operation).append(": variable='").append(variableDefinition.getName())
                .append("', type='").append(variableDefinition.getUserType().getName())
                .append("', id=").append(id).append(", values={");
        appendAttributes(sb, variableDefinition.getUserType().getAttributes(), fullMap);
        sb.append("}");
        writeToLog(variableDefinition.getName(), sb.toString(), operation, "variable '" + variableDefinition.getName() + "'");
    }

    void logContainerElement(String operation, VariableDefinition containerDef, int index,
            UserType componentUserType, Long id, UserTypeMap fullMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("byReference ").append(operation)
                .append(": container='").append(containerDef.getName())
                .append("[").append(index).append("]'")
                .append(", type='").append(componentUserType.getName())
                .append("', id=").append(id).append(", values={");
        appendAttributes(sb, componentUserType.getAttributes(), fullMap);
        sb.append("}");
        writeToLog(containerDef.getName(), sb.toString(), operation, "container '" + containerDef.getName() + "[" + index + "]'");
    }

    private void appendAttributes(StringBuilder sb, java.util.List<VariableDefinition> attributes, UserTypeMap fullMap) {
        boolean first = true;
        for (VariableDefinition attr : attributes) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(attr.getName()).append("=").append(fullMap.get(attr.getName()));
            first = false;
        }
    }

    private void writeToLog(String variableName, String message, String operation, String target) {
        log.info(message);
        try {
            CurrentActionLog processLog = new CurrentActionLog();
            processLog.setVariableName(variableName);
            String attrValue = message.length() > MAX_LOG_VALUE_LENGTH
                    ? message.substring(0, MAX_LOG_VALUE_LENGTH - LOG_TRUNCATION_SUFFIX_LENGTH) + "..."
                    : message;
            processLog.getAttributes().put(Attributes.ATTR_ACTION, attrValue);
            processLogDao.addLog(processLog, currentProcess, currentToken);
        } catch (Exception e) {
            log.warn("byReference: failed to write process log for " + operation + " of " + target, e);
        }
    }
}
