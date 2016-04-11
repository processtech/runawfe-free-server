package ru.runa.wfe.commons;

import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;

public interface IScriptExecutor {

    public Map<String, Object> executeScript(IVariableProvider variableProvider, String script);

    public Object evaluateScript(IVariableProvider variableProvider, String script);

}
