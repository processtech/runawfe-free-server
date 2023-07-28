package ru.runa.wfe.commons;

import ru.runa.wfe.audit.CurrentNodeInfoLog;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.execution.ExecutionContext;

public class GroovyNodeInfoLogExecutor {
    private final ExecutionContext executionContext;

    public GroovyNodeInfoLogExecutor(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void debug(String message) {
        executionContext.addLog(new CurrentNodeInfoLog(executionContext.getNode(), Severity.DEBUG, message));
    }

    public void info(String message) {
        executionContext.addLog(new CurrentNodeInfoLog(executionContext.getNode(), Severity.INFO, message));
    }
}
