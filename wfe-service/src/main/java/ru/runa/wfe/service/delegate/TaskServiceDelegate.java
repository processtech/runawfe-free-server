package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class TaskServiceDelegate extends EJB3Delegate implements TaskService {

    public TaskServiceDelegate() {
        super(TaskService.class);
    }

    private TaskService getTaskService() {
        return (TaskService) getService();
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) {
        try {
            getTaskService().completeTask(user, taskId, variables, swimlaneActorId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTask> getProcessTasks(User user, Long processId, boolean includeSubprocesses) {
        try {
            return getTaskService().getProcessTasks(user, processId, includeSubprocesses);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) {
        try {
            getTaskService().assignTask(user, taskId, previousOwner, newExecutor);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int reassignTasks(User user, BatchPresentation batchPresentation) {
        try {
            return getTaskService().reassignTasks(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void reassignTask(User user, Long taskId) {
        try {
            getTaskService().reassignTask(user, taskId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void markTaskOpened(User user, Long taskId) {
        try {
            getTaskService().markTaskOpened(user, taskId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTask> getMyTasks(User user, BatchPresentation batchPresentation) {
        try {
            return getTaskService().getMyTasks(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        try {
            return getTaskService().getTasks(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfTask getTask(User user, Long taskId) {
        try {
            return getTaskService().getTask(user, taskId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepOwners, List<? extends Executor> newOwners) {
        try {
            getTaskService().delegateTask(user, taskId, currentOwner, true, newOwners);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
