package ru.runa.wfe.extension.decision;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.DecisionHandler;

public class GroovyDecisionHandler implements DecisionHandler {
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String decide(ExecutionContext executionContext) throws Exception {
        Object result = new GroovyScriptExecutor().evaluateScript(executionContext.getVariableProvider(), configuration);
        return String.valueOf(result);
    }

}
