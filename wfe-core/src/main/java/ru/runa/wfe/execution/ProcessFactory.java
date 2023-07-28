package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.CurrentProcessStartLog;
import ru.runa.wfe.audit.CurrentSubprocessStartLog;
import ru.runa.wfe.audit.CurrentTaskAssignLog;
import ru.runa.wfe.audit.CurrentTaskCreateLog;
import ru.runa.wfe.audit.CurrentTaskEndLog;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.execution.dao.CurrentNodeProcessDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.execution.dao.CurrentSwimlaneDao;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.EventSubprocessStartNode;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
import ru.runa.wfe.var.VariableProvider;

@Component
public class ProcessFactory {
    @Autowired
    private ProcessDefinitionDao processDefinitionDao;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private CurrentNodeProcessDao currentNodeProcessDao;
    @Autowired
    private CurrentSwimlaneDao currentSwimlaneDao;

    private static final Map<Permission, Permission> DEFINITION_TO_PROCESS_PERMISSION_MAP = new HashMap<Permission, Permission>() {{
        put(Permission.READ_PROCESS, Permission.READ);
        put(Permission.CANCEL_PROCESS, Permission.CANCEL);
    }};

    private Set<Permission> getProcessPermissions(Executor executor, ParsedProcessDefinition parsedProcessDefinition) {
        Set<Permission> result = new HashSet<>();
        for (Map.Entry<Permission, Permission> kv : DEFINITION_TO_PROCESS_PERMISSION_MAP.entrySet()) {
            // Using isAllowed() because it takes DEFINITIONS list & executor groups into account.
            if (permissionDao.isAllowed(executor, kv.getKey(), parsedProcessDefinition.getSecuredObject(), false)) {
                result.add(kv.getValue());
            }
        }
        return result;
    }

    /**
     * Creates and starts a new process for the given process definition, puts the root-token (=main path of execution) in the start state and
     * executes the initial node.
     *
     * @param variables
     *            will be inserted into the context variables after the context submodule has been created and before the process-start event is
     *            fired, which is also before the execution of the initial node.
     */
    public CurrentProcess startProcess(ParsedProcessDefinition parsedProcessDefinition, StartNode startNode, Map<String, Object> variables,
            Actor actor, String transitionName, Map<String, Object> transientVariables) {
        Preconditions.checkNotNull(actor, "can't start a process when actor is null");
        ExecutionContext executionContext = createProcessInternal(parsedProcessDefinition, startNode, variables, actor, null, transientVariables,
                transitionName);
        grantProcessPermissions(parsedProcessDefinition, executionContext.getCurrentProcess(), actor);
        startProcessInternal(executionContext, startNode, transitionName);
        return executionContext.getCurrentProcess();
    }

    private void grantProcessPermissions(ParsedProcessDefinition parsedProcessDefinition, Process process, Actor actor) {
        boolean permissionsAreSetToProcessStarter = false;
        Executor processStarter = executorDao.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
        Set<Permission> processStarterPermissions = getProcessPermissions(processStarter, parsedProcessDefinition);
        for (Executor executor : permissionDao.getExecutorsWithPermission(parsedProcessDefinition.getSecuredObject())) {
            Set<Permission> permissions = getProcessPermissions(executor, parsedProcessDefinition);
            if (Objects.equal(actor, executor)) {
                permissions = CollectionUtil.unionSet(permissions, processStarterPermissions);
                permissionsAreSetToProcessStarter = true;
            }
            if (permissions.size() > 0) {
                permissionDao.setPermissions(executor, permissions, process);
            }
        }
        if (!permissionsAreSetToProcessStarter) {
            permissionDao.setPermissions(actor, processStarterPermissions, process);
        }
    }

    public CurrentProcess createSubprocess(ExecutionContext parentExecutionContext, ParsedProcessDefinition parsedProcessDefinition,
            Map<String, Object> variables, int index, boolean validate) {
        CurrentProcess parentProcess = parentExecutionContext.getCurrentProcess();
        CurrentProcess rootProcess = currentNodeProcessDao.getRootProcessByParentProcess(parentProcess);
        SubprocessNode subProcessNode = (SubprocessNode) parentExecutionContext.getNode();
        StartNode startNode = parsedProcessDefinition.getManualStartStateNotNull();
        ExecutionContext subExecutionContext = createProcessInternal(parsedProcessDefinition, startNode, variables, null, parentProcess, null, null);
        currentNodeProcessDao.create(new CurrentNodeProcess(subProcessNode, parentExecutionContext.getCurrentToken(), rootProcess,
                subExecutionContext.getCurrentProcess(), index));
        if (validate) {
            validateVariables(subExecutionContext, new ExecutionVariableProvider(subExecutionContext), parsedProcessDefinition,
                    parsedProcessDefinition.getManualStartStateNotNull().getNodeId(), variables);
        }
        return subExecutionContext.getCurrentProcess();
    }

    public void startSubprocess(ExecutionContext parentExecutionContext, ExecutionContext executionContext) {
        parentExecutionContext.addLog(new CurrentSubprocessStartLog(parentExecutionContext.getNode(), parentExecutionContext.getCurrentToken(),
                executionContext.getCurrentProcess()));
        grantSubprocessPermissions(executionContext.getParsedProcessDefinition(), executionContext.getCurrentProcess(),
                parentExecutionContext.getCurrentProcess());
        StartNode startNode = executionContext.getParsedProcessDefinition().getManualStartStateNotNull();
        startProcessInternal(executionContext, startNode, null);
    }

    private void validateVariables(ExecutionContext executionContext, VariableProvider variableProvider,
            ParsedProcessDefinition parsedProcessDefinition, String nodeId, Map<String, Object> variables) throws ValidationException {
        Interaction interaction = parsedProcessDefinition.getInteractionNotNull(nodeId);
        if (interaction.getValidationData() != null) {
            ValidatorContext context = ValidatorManager.getInstance().validate(executionContext, variableProvider, interaction.getValidationData(),
                    variables);
            if (context.hasGlobalErrors() || context.hasFieldErrors()) {
                throw new ValidationException(context.getFieldErrors(), context.getGlobalErrors());
            }
        }
    }

    private void grantSubprocessPermissions(ParsedProcessDefinition parsedProcessDefinition, Process subProcess, Process parentProcess) {
        Set<Executor> executors = new HashSet<>();
        executors.addAll(permissionDao.getExecutorsWithPermission(parsedProcessDefinition.getSecuredObject()));
        executors.addAll(permissionDao.getExecutorsWithPermission(parentProcess));
        for (Executor executor : executors) {
            List<Permission> permissionsByParentProcess = permissionDao.getIssuedPermissions(executor, parentProcess);
            Set<Permission> permissionsByDefinition = getProcessPermissions(executor, parsedProcessDefinition);
            Set<Permission> permissions = CollectionUtil.unionSet(permissionsByParentProcess, permissionsByDefinition);
            if (permissions.size() > 0) {
                permissionDao.setPermissions(executor, permissions, subProcess);
            }
        }
    }

    private ExecutionContext createProcessInternal(ParsedProcessDefinition parsedProcessDefinition, StartNode startNode,
            Map<String, Object> variables, Actor actor, CurrentProcess parentProcess, Map<String, Object> transientVariables, String transitionName) {
        Preconditions.checkNotNull(parsedProcessDefinition, "can't create a process when parsedProcessDefinition is null");
        CurrentProcess process = new CurrentProcess(processDefinitionDao.get(parsedProcessDefinition.getId()));
        CurrentToken rootToken = new CurrentToken(parsedProcessDefinition, process, startNode);
        process.setRootToken(rootToken);
        currentProcessDao.create(process);
        if (parentProcess != null) {
            process.setParentId(parentProcess.getId());
            process.setExternalData(parentProcess.getExternalData());
        }
        process.setHierarchyIds(
                ProcessHierarchyUtils.createHierarchy(parentProcess != null ? parentProcess.getHierarchyIds() : null, process.getId()));
        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, rootToken);
        if (actor != null) {
            executionContext.addLog(new CurrentProcessStartLog(actor));
            executionContext.addLog(new CurrentTaskCreateLog(process, startNode));
            executionContext.addLog(new CurrentTaskAssignLog(process, startNode, actor));
        }
        if (transientVariables != null) {
            for (Map.Entry<String, Object> entry : transientVariables.entrySet()) {
                executionContext.setTransientVariable(entry.getKey(), entry.getValue());
            }
        }
        executionContext.setVariableValues(variables);
        for (ParsedSubprocessDefinition parsedSubprocessDefinition : parsedProcessDefinition.getEmbeddedSubprocesses().values()) {
            if (parsedSubprocessDefinition.isTriggeredByEvent()) {
                for (StartNode eventStartNode : parsedSubprocessDefinition.getEventStartNodes()) {
                    ((EventSubprocessStartNode) eventStartNode).execute(new ExecutionContext(parsedProcessDefinition, process));
                }
            }
        }
        if (actor != null) {
            if (startNode.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer()) {
                SwimlaneDefinition swimlaneDefinition = startNode.getFirstTaskNotNull().getSwimlane();
                CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, swimlaneDefinition);
                swimlane.assignExecutor(executionContext, actor, false);
            }
            executionContext.addLog(new CurrentTaskEndLog(process, startNode, actor, transitionName));
        }
        return executionContext;
    }

    private void startProcessInternal(ExecutionContext executionContext, StartNode startNode, String transitionName) {
        Transition transition = null;
        if (transitionName != null) {
            transition = startNode.getLeavingTransitionNotNull(transitionName);
        }
        startNode.leave(executionContext, transition);
    }

}
