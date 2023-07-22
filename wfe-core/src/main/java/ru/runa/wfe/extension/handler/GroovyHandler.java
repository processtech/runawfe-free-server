package ru.runa.wfe.extension.handler;

import java.util.Map;
import ru.runa.wfe.commons.GroovyNodeInfoLogExecutor;
import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class GroovyHandler extends TaskHandlerBase implements ActionHandler {

    @Override
    public void execute(ExecutionContext context) throws Exception {
        Map<String, Object> result = executeAction(context.getVariableProvider(), new GroovyNodeInfoLogExecutor(context));
        if (result != null) {
            context.setVariableValues(result);
        }
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        return executeAction(variableProvider, null);
    }

    private Map<String, Object> executeAction(VariableProvider variableProvider, GroovyNodeInfoLogExecutor nodeInfoLogExecutor) {
        return new GroovyScriptExecutor().executeScript(variableProvider, configuration, nodeInfoLogExecutor);
    }
}
