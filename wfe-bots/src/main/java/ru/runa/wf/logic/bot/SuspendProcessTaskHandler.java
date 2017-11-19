package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Suspend process by id.
 *
 * @author Vitaly Alekseev
 * @since 4.3.0
 */
public class SuspendProcessTaskHandler extends TaskHandlerBase {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        Long processId = paramsDef.getInputParamValueNotNull("processId", variableProvider);
        boolean excludeMe = TypeConversionUtil.convertTo(boolean.class, paramsDef.getInputParamValueNotNull("excludeMe", variableProvider));
        Delegates.getExecutionService().suspendProcess(user, processId);
        if (excludeMe) {
            WfProcess process = Delegates.getExecutionService().getProcess(user, variableProvider.getProcessId());
            if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
                Delegates.getExecutionService().activateProcess(user, variableProvider.getProcessId());
            }
        }
        return null;
    }
}
