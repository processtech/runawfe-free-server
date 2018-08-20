package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.ScriptExecutor;
import ru.runa.wfe.var.VariableProvider;

public class GroovyHandler extends CommonHandler {

    protected ScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        return getScriptExecutor().executeScript(variableProvider, configuration);
    }

}
