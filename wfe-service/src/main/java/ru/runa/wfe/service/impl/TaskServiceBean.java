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
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.ProcessDefinition;
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
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Stateless(name = "TaskServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "TaskAPI", serviceName = "TaskWebService")
@SOAPBinding
public class TaskServiceBean implements TaskServiceLocal, TaskServiceRemote, TaskWebServiceRemote {
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private ExecutionLogic executionLogic;

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
        Long processId = taskLogic.getProcessId(user, taskId);
        FileVariablesUtil.unproxyFileVariables(user, processId, taskId, variables);
        taskLogic.completeTask(user, taskId, variables, swimlaneActorId);
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
    public boolean reassignTask(@WebParam(name = "user") @NonNull User user, @WebParam(name = "batchPresentation") @NonNull Long taskId) {
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
        WfTask task = taskLogic.getTask(user, taskId);
        ProcessDefinition processDefinition = executionLogic.getDefinition(task.getDefinitionId());
        completeTask(user, taskId, VariableConverter.unmarshal(processDefinition, variables), swimlaneActorId);
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
            WfTask task = taskLogic.getTask(user, taskId);
            taskLogic.delegateTask(user, taskId, task.getOwner(), keepCurrentOwners, newOwners);
        }
    }

    @Override
    public List<WfTask> getUnassignedTasks(@NonNull User user) {
        return taskLogic.getUnassignedTasks(user);
    }
}