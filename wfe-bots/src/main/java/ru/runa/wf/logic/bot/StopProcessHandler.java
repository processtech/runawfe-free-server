package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

public class StopProcessHandler extends TaskHandlerBase implements ActionHandler {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Long processId = TypeConversionUtil.convertTo(Long.class,
                paramsDef.getInputParamValueNotNull("processId", executionContext.getVariableProvider()));
        if (processId > 0) {
            ru.runa.wfe.execution.Process process = ApplicationContextFactory.getProcessDAO().get(processId);
            IProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process.getProcessDefinitionVersion().getId());
            ExecutionContext targetExecutionContext = new ExecutionContext(parsedProcessDefinition, process);
            process.end(targetExecutionContext, null);
        } else {
            log.warn("ProcessID = " + processId + ", don't stopping process");
        }
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        Long processId = TypeConversionUtil.convertTo(Long.class, paramsDef.getInputParamValueNotNull("processId", variableProvider));
        if (processId > 0) {
            Delegates.getExecutionService().cancelProcess(user, processId);
        } else {
            log.warn("ProcessID = " + processId + ", don't stopping process");
        }
        return null;
    }
}
