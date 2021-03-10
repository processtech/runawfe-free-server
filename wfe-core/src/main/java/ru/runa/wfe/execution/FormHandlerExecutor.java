package ru.runa.wfe.execution;

import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.task.Task;

public class FormHandlerExecutor {

    @Transactional
    public void execute(long taskId) {
        Task task = ApplicationContextFactory.getTaskDao().get(taskId);
        ParsedProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(task.getProcess());
        InteractionNode node = (InteractionNode) processDefinition.getNodeNotNull(task.getNodeId());
        node.getFirstTaskNotNull().fireEvent(new ExecutionContext(processDefinition, task.getToken()), ActionEvent.TASK_OPEN);
    }
}
