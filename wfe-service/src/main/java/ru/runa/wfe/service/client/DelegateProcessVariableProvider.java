package ru.runa.wfe.service.client;

import com.google.common.base.MoreObjects;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ExecutorLoader;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Implementation which uses service call for each variable retrieval (through RunaWFE delegates).
 *
 * @author Dofs
 * @since 4.0
 */
public class DelegateProcessVariableProvider extends AbstractVariableProvider {
    protected final ExecutionService executionService;
    protected final DefinitionService definitionService;
    protected final User user;
    protected final Long processId;
    protected Long processDefinitionVersionId;
    protected String processDefinitionName;
    protected ParsedProcessDefinition parsedProcessDefinition;

    public DelegateProcessVariableProvider(ExecutionService executionService, DefinitionService definitionService, User user, Long processId) {
        this.executionService = executionService;
        this.definitionService = definitionService;
        this.user = user;
        this.processId = processId;
    }

    public DelegateProcessVariableProvider(User user, Long processId) {
        this(Delegates.getExecutionService(), Delegates.getDefinitionService(), user, processId);
    }

    @Override
    protected ExecutorLoader getExecutorLoader() {
        return new DelegateExecutorLoader(user);
    }

    @Override
    public Long getProcessDefinitionVersionId() {
        if (processDefinitionVersionId == null) {
            WfProcess process = executionService.getProcess(user, processId);
            processDefinitionVersionId = process.getDefinitionVersionId();
        }
        return processDefinitionVersionId;
    }

    @Override
    public String getProcessDefinitionName() {
        if (processDefinitionName == null) {
            processDefinitionName = executionService.getProcess(user, processId).getName();
        }
        return processDefinitionName;
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        if (parsedProcessDefinition == null) {
            parsedProcessDefinition = definitionService.getParsedProcessDefinition(user, getProcessDefinitionVersionId());
        }
        return parsedProcessDefinition;
    }

    @Override
    public Long getProcessId() {
        return processId;
    }

    @Override
    public UserType getUserType(String name) {
        return definitionService.getUserType(user, getProcessDefinitionVersionId(), name);
    }

    @Override
    public Object getValue(String variableName) {
        WfVariable variable = getVariable(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return executionService.getVariable(user, processId, variableName);
    }

    @Override
    public DelegateProcessVariableProvider getSameProvider(Long processId) {
        return new DelegateProcessVariableProvider(executionService, definitionService, user, processId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("processId", processId).toString();
    }
}
