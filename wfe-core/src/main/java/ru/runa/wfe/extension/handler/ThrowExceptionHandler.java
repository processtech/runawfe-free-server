package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.var.VariableProvider;

public class ThrowExceptionHandler extends CommonHandler {

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        throw new RuntimeException("handler exception");
    }

}
