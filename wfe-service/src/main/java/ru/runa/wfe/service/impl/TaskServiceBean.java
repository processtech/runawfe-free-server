package ru.runa.wfe.service.impl;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.TaskServiceLocal;
import ru.runa.wfe.service.decl.TaskServiceRemote;
import ru.runa.wfe.service.decl.TaskServiceRemoteWS;
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

import com.google.common.base.Preconditions;

@Stateless(name = "TaskServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "TaskAPI", serviceName = "TaskWebService")
@SOAPBinding
public class TaskServiceBean implements TaskServiceLocal, TaskServiceRemote, TaskServiceRemoteWS {
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Override
    @WebResult(name = "result")
    public List<WfTask> getMyTasks(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.getMyTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getTasks(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.getTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public WfTask getTask(@WebParam(name = "user") User user, @WebParam(name = "taskId") Long taskId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(taskId != null, "taskId");
        return taskLogic.getTask(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getProcessTasks(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "includeSubprocesses") boolean includeSubprocesses) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(processId != null, "processId");
        return taskLogic.getTasks(user, processId, includeSubprocesses);
    }

    @WebMethod(exclude = true)
    @Override
    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(taskId != null, "taskId");
        Long processId = taskLogic.getProcessId(user, taskId);
        FileVariablesUtil.unproxyFileVariables(user, processId, taskId, variables);
        taskLogic.completeTask(user, taskId, variables, swimlaneActorId);
    }

    @Override
    @WebResult(name = "result")
    public void assignTask(@WebParam(name = "user") User user, @WebParam(name = "taskId") Long taskId,
            @WebParam(name = "previousOwner") Executor previousOwner, @WebParam(name = "newExecutor") Executor newExecutor) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(taskId != null, "taskId");
        taskLogic.assignTask(user, taskId, previousOwner, newExecutor);
    }

    @Override
    @WebResult(name = "result")
    public int reassignTasks(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.reassignTasks(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public boolean reassignTask(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") Long taskId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(taskId != null);
        return taskLogic.reassignTask(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public void markTaskOpened(@WebParam(name = "user") User user, @WebParam(name = "taskId") Long taskId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(taskId != null, "taskId");
        taskLogic.markTaskOpened(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public void completeTaskWS(@WebParam(name = "user") User user, @WebParam(name = "taskId") Long taskId,
            @WebParam(name = "variables") List<Variable> variables, @WebParam(name = "swimlaneActorId") Long swimlaneActorId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(taskId != null, "taskId");
        WfTask task = taskLogic.getTask(user, taskId);
        ProcessDefinition processDefinition = executionLogic.getDefinition(task.getDefinitionId());
        completeTask(user, taskId, VariableConverter.unmarshal(processDefinition, variables), swimlaneActorId);
    }

    @WebMethod(exclude = true)
    @Override
    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> newOwners) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(taskId != null, "taskId");
        Preconditions.checkArgument(newOwners != null, "newOwners");
        taskLogic.delegateTask(user, taskId, currentOwner, keepCurrentOwners, newOwners);
    }

    @Override
    public List<WfTask> getUnassignedTasks(User user) {
        Preconditions.checkArgument(user != null, "user");
        return taskLogic.getUnassignedTasks(user);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTask> getExecutorTasks(User user, @WebParam(name = "executorId") Long executorId,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(executorId != null, "executorId");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TASKS.createNonPaged();
        }
        return taskLogic.getExecutorTasks(user, executorId, batchPresentation);
    }

}