package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Cancels process task belongs to.
 *
 * Created on 09.11.2006
 *
 * @author Vitaliy S
 * @since 2.0
 */
public class CancelCurrentProcessTaskHandler extends TaskHandlerBase {

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) {
        Delegates.getExecutionService().cancelProcess(user, task.getProcessId());
        return null;
    }
}
