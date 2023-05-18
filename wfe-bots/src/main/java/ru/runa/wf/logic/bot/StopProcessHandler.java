package ru.runa.wf.logic.bot;

import java.util.Map;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class StopProcessHandler extends TaskHandlerBase implements ActionHandler {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) {
        paramsDef = ParamsDef.parse(configuration);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Long processId = TypeConversionUtil.convertTo(Long.class,
                paramsDef.getInputParamValueNotNull("processId", executionContext.getVariableProvider()));
        if (processId > 0) {
            CurrentProcess process = ApplicationContextFactory.getCurrentProcessDao().get(processId);
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
            ExecutionContext targetExecutionContext = new ExecutionContext(parsedProcessDefinition, process);
            ApplicationContextFactory.getExecutionLogic().endProcess(process, targetExecutionContext, null);
        } else {
            log.warn("ProcessID = " + processId + ", don't stopping process");
        }
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) {
        Long processId = TypeConversionUtil.convertTo(Long.class, paramsDef.getInputParamValueNotNull("processId", variableProvider));
        if (processId > 0) {
            Delegates.getExecutionService().cancelProcess(user, processId);
        } else {
            log.warn("ProcessID = " + processId + ", don't stopping process");
        }
        return null;
    }
}
