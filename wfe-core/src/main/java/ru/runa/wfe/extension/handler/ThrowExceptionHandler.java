package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;

public class ThrowExceptionHandler extends CommonHandler {

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        throw new RuntimeException("handler exception");
    }

}
