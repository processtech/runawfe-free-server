package ru.runa.wfe.service;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

/**
 * Task service.
 * 
 * @author gbax
 * @since 4.2.1
 */
public interface TaskService {

    /**
     * Gets tasks for authenticated user by {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfTask> getMyTasks(User user, BatchPresentation batchPresentation);

    /**
     * Gets tasks by {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation);

    /**
     * Gets task by id.
     * 
     * @param user
     *            authorized user
     * @param taskId
     *            task id
     * @return not <code>null</code>
     * @throws TaskDoesNotExistException
     */
    public WfTask getTask(User user, Long taskId) throws TaskDoesNotExistException;

    /**
     * Gets tasks for specified Executor Id
     *
     * @param user
     *            User currently working (used to check rights only!)
     * @param executorId
     *            Executor whose tasks are returned (for group - tasks of actors from group, not group itself).
     * @param batchPresentation
     * @return
     */
    public List<WfTask> getExecutorTasks(User user, Long executorId, BatchPresentation batchPresentation);

    /**
     * Gets all process tasks.
     * 
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @param includeSubprocesses
     *            whether to include tasks from subprocesses
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public List<WfTask> getProcessTasks(User user, Long processId, boolean includeSubprocesses) throws ProcessDoesNotExistException;

    /**
     * Reassigns task to another executor.
     * 
     * @param previousOwner
     *            old executor (check for multi-threaded change)
     * @param newOwner
     *            new executor
     * @throws TaskAlreadyAcceptedException
     *             if previous owner differs from provided
     */
    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newOwner) throws TaskAlreadyAcceptedException;

    /**
     * Reassigns tasks using TaskAssigner.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     *            to load tasks
     * @return reassigned tasks count
     */
    public int reassignTasks(User user, BatchPresentation batchPresentation);

    /**
     * Reassigns task using TaskAssigner.
     * 
     * @param user
     *            authorized user
     * @return whether task was successfully reassigned
     */
    public boolean reassignTask(User user, Long taskId);

    /**
     * Completes task by id.
     * 
     * @param user
     *            authorized user
     * @param taskId
     *            task id
     * @param variables
     *            variable values, can contain transition name by key ru.runa.wfe.execution.dto.WfProcess.SELECTED_TRANSITION_KEY
     * @param swimlaneActorId
     *            actor id who will be assigned to task swimlane, can be <code>null</code>
     * @throws TaskDoesNotExistException
     * @throws ValidationException
     */
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId)
            throws TaskDoesNotExistException, ValidationException;

    /**
     * Marks task as read.
     * 
     * @param user
     *            authorized user
     * @param taskId
     *            task id
     * @throws TaskDoesNotExistException
     */
    public void markTaskOpened(User user, Long taskId) throws TaskDoesNotExistException;

    /**
     * Delegate task to another users or groups.
     * 
     * @param user
     *            authorized user
     * @param taskId
     *            task id
     * @param currentOwner
     *            current executor
     * @param keepCurrentOwners
     *            whether current owners should remain
     * @param newOwners
     *            new executor list
     */
    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> newOwners);

    /**
     * temporary method for #2151
     */
    public List<WfTask> getUnassignedTasks(User user);

}