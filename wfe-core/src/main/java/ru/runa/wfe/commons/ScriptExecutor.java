package ru.runa.wfe.commons;

import java.util.Map;

import ru.runa.wfe.var.VariableProvider;

public interface ScriptExecutor {

    public Map<String, Object> executeScript(VariableProvider variableProvider, String script);

    public Object evaluateScript(VariableProvider variableProvider, String script);

}
