package ru.runa.wfe.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.TaskServiceLocal;
import ru.runa.wfe.service.decl.TaskServiceRemote;
import ru.runa.wfe.service.decl.TaskWebServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.service.jaxb.VariableConverter;
import ru.runa.wfe.service.utils.FileVariablesUtil;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskFormDraft;
import ru.runa.wfe.task.dao.TaskFormDraftDao;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFormDraft;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Stateless(name = "TaskServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class})
@WebService(name = "TaskAPI", serviceName = "TaskWebService")
@SOAPBinding
public class TaskServiceBean implements TaskServiceLocal, TaskServiceRemote, TaskWebServiceRemote {
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TaskFormDraftDao taskFormDraftDao;

    @Override
    @WebResult(name = "result")
    public List<WfTask> getMyTasks(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.getMyTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getTasks(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.getTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public WfTask getTask(@WebParam(name = "user") @NonNull User user, @WebParam(name = "taskId") @NonNull Long taskId) {
        return taskLogic.getTask(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getProcessTasks(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "includeSubprocesses") boolean includeSubprocesses) {
        return taskLogic.getTasks(user, processId, includeSubprocesses);
    }

    @WebMethod(exclude = true)
    @Override
    public void completeTask(@NonNull User user, @NonNull Long taskId, Map<String, Object> variables, Long swimlaneActorId) {
        completeTask(user, taskId, variables);
    }

    @WebMethod(exclude = true)
    @Override
    public WfTask completeTask(@NonNull User user, @NonNull Long taskId, Map<String, Object> variables) {
        Task task = taskLogic.getTaskEntity(user, taskId);
        FileVariablesUtil.unproxyFileVariables(user, task.getProcess().getId(), taskId, variables);
        return taskLogic.completeTask(user, taskId, variables);
    }

    @Override
    @WebResult(name = "result")
    public void assignTask(@WebParam(name = "user") @NonNull User user, @WebParam(name = "taskId") @NonNull Long taskId,
            @WebParam(name = "previousOwner") Executor previousOwner, @WebParam(name = "newExecutor") Executor newExecutor) {
        taskLogic.assignTask(user, taskId, previousOwner, newExecutor);
    }

    @Override
    @WebResult(name = "result")
    public int reassignTasks(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.reassignTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public boolean reassignTask(@WebParam(name = "user") @NonNull User user, @WebParam(name = "taskId") @NonNull Long taskId) {
        return taskLogic.reassignTask(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public void markTaskOpened(@WebParam(name = "user") @NonNull User user, @WebParam(name = "taskId") @NonNull Long taskId) {
        taskLogic.markTaskOpened(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public void completeTaskWS(@WebParam(name = "user") @NonNull User user, @WebParam(name = "taskId") @NonNull Long taskId,
            @WebParam(name = "variables") List<Variable> variables, @WebParam(name = "swimlaneActorId") Long swimlaneActorId) {
        Task task = taskLogic.getTaskEntity(user, taskId);
        ParsedProcessDefinition parsedProcessDefinition = executionLogic.getDefinition(task.getProcess());
        completeTask(user, taskId, VariableConverter.unmarshal(parsedProcessDefinition, variables), swimlaneActorId);
    }

    @WebMethod(exclude = true)
    @Override
    public void delegateTask(@NonNull User user, @NonNull Long taskId, Executor currentOwner, boolean keepCurrentOwners,
            @NonNull List<? extends Executor> newOwners) {
        taskLogic.delegateTask(user, taskId, currentOwner, keepCurrentOwners, newOwners);
    }

    @WebMethod(exclude = true)
    @Override
    public void delegateTasks(@NonNull User user, @NonNull Set<Long> taskIds, boolean keepCurrentOwners, List<? extends Executor> newOwners) {
        for (Long taskId : taskIds) {
            Task task = taskLogic.getTaskEntity(user, taskId);
            taskLogic.delegateTask(user, taskId, task.getExecutor(), keepCurrentOwners, newOwners);
        }
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getUnassignedTasks(@WebParam(name = "user") @NonNull User user) {
        return taskLogic.getUnassignedTasks(user);
    }

    @Override
    public boolean isTaskDelegationEnabled() {
        return taskLogic.isTaskDelegationEnabled();
    }

    @Override
    @WebResult(name = "result")
    public WfTaskFormDraft getTaskFormDraft(User user, Long taskId) {
        TaskFormDraft taskFormDraft = taskFormDraftDao.find(user, taskId);
        if (null == taskFormDraft)
            return null;

        return new WfTaskFormDraft(taskFormDraft);
    }

    @WebMethod(exclude = true)
    @Override
    public void setTaskFormDraft(User user, Long taskId, String varB64) {
        taskFormDraftDao.save(user, taskId, varB64);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteTaskFormDraft(User user, Long taskId) {
        taskFormDraftDao.delete(user, taskId);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteAllTaskFormDrafts(Long taskId) {
        taskFormDraftDao.delete(taskId);
    }
}