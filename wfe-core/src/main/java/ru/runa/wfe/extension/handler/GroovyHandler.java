package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.var.IVariableProvider;

public class GroovyHandler extends CommonHandler {

    protected IScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        return getScriptExecutor().executeScript(variableProvider, configuration);
    }

}
