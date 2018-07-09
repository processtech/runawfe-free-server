package ru.runa.wfe.extension.handler.var;

import java.util.Map;

import ru.runa.wfe.var.VariableProvider;

public class CreateCalendarHandler extends SetDateVariableHandler {

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        log.debug("Running in pre-v4.3.0 compatibility mode");
        return executeAction(variableProvider, false);
    }

}
