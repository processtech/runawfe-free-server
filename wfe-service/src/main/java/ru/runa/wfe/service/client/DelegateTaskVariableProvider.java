package ru.runa.wfe.service.client;

import com.google.common.base.MoreObjects;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Implementation which uses service call for each variable retrieval (through RunaWFE delegates).
 * 
 * @author Dofs
 * @since 4.2.1
 */
public class DelegateTaskVariableProvider extends DelegateProcessVariableProvider {
    private final Long taskId;

    public DelegateTaskVariableProvider(ExecutionService executionService, DefinitionService definitionService, User user, Long processId, Long taskId) {
        super(executionService, definitionService, user, processId);
        this.taskId = taskId;
    }

    public DelegateTaskVariableProvider(User user, WfTask task) {
        super(user, task.getProcessId());
        this.processDefinitionVersionId = task.getDefinitionVersionId();
        this.taskId = task.getId();
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return executionService.getTaskVariable(user, processId, taskId, variableName);
    }

    @Override
    public DelegateTaskVariableProvider getSameProvider(Long processId) {
        return new DelegateTaskVariableProvider(executionService, definitionService, user, processId, taskId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("processId", processId).add("taskId", taskId).toString();
    }
}
