package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class TaskServiceDelegate extends Ejb3Delegate implements TaskService {

    public TaskServiceDelegate() {
        super(TaskService.class);
    }

    private TaskService getTaskService() {
        return (TaskService) getService();
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) {
        completeTask(user, taskId, variables);
    }

    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables) {
        try {
            getTaskService().completeTask(user, taskId, variables);
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
    public boolean reassignTask(User user, Long taskId) {
        try {
            return getTaskService().reassignTask(user, taskId);
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
    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> newOwners) {
        try {
            getTaskService().delegateTask(user, taskId, currentOwner, keepCurrentOwners, newOwners);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void delegateTasks(User user, Set<Long> taskIds, boolean keepCurrentOwners, List<? extends Executor> newOwners) {
        try {
            getTaskService().delegateTasks(user, taskIds, keepCurrentOwners,  newOwners);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTask> getUnassignedTasks(User user) {
        try {
            return getTaskService().getUnassignedTasks(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
