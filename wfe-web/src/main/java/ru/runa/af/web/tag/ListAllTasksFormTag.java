package ru.runa.af.web.tag;

import java.util.List;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.MessagesCommon;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.tag.ListTasksFormTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listAllTasksForm")
public class ListAllTasksFormTag extends ListTasksFormTag {
    private static final long serialVersionUID = 1L;
    private Long executorId;

    @Attribute(required = true, rtexprvalue = true)
    public void setExecutorId(Long aExecutorId) {
        this.executorId = aExecutorId;
    }

    public Long getExecutorId() {
        return executorId;
    }

    @Override
    protected String getTitle() {
        Executor executor = Delegates.getExecutorService().getExecutor(getUser(), executorId);
        if (executor.getClass().getSimpleName().equals("Group")) {
            return MessagesProcesses.TITLE_GROUP_TASKS.message(pageContext) + ": " +
                    (executor.getFullName() != null ? executor.getFullName() : executor.getLabel());
        }
        return MessagesProcesses.TITLE_EXECUTOR_TASKS.message(pageContext) + ": " +
                (executor.getFullName() != null ? executor.getFullName() : executor.getLabel());
    }

    @Override
    protected List<WfTask> getTasksList(BatchPresentation batchPresentation) {
        List<WfTask> tasks = Delegates.getTaskService().getExecutorTasks(getUser(), executorId, batchPresentation);
        return tasks;
    }


    @Override
    protected boolean isFormButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }


}
