package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Activate resumed process by id.
 *
 * @author Vitaly Alekseev
 * @since 4.3.0
 */
public class ResumeProcessTaskHandler extends TaskHandlerBase {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        Long processId = paramsDef.getInputParamValueNotNull("processId", variableProvider);
        Delegates.getExecutionService().activateProcess(user, processId);
        return null;
    }

}
