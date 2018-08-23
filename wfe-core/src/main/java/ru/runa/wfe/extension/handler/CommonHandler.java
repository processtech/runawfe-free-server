package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public abstract class CommonHandler extends TaskHandlerBase implements ActionHandler {

    protected abstract Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception;

    @Override
    public void execute(ExecutionContext context) throws Exception {
        Map<String, Object> result = executeAction(context.getVariableProvider());
        if (result != null) {
            context.setVariableValues(result);
        }
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        return executeAction(variableProvider);
    }

}
