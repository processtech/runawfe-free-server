package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDAO;

public class ProcessFactory {
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private NodeProcessDao nodeProcessDAO;
    @Autowired
    private SwimlaneDao swimlaneDAO;

    private static final Map<Permission, Permission> DEFINITION_TO_PROCESS_PERMISSION_MAP = new HashMap<Permission, Permission>() {{
        put(Permission.READ_PROCESS, Permission.READ);
        put(Permission.CANCEL_PROCESS, Permission.CANCEL);
    }};

    private Set<Permission> getProcessPermissions(Executor executor, ParsedProcessDefinition parsedProcessDefinition) {
        Set<Permission> result = new HashSet<>();
        for (Map.Entry<Permission, Permission> kv : DEFINITION_TO_PROCESS_PERMISSION_MAP.entrySet()) {
            // Using isAllowed() because it takes DEFINITIONS list & executor groups into account.
            if (permissionDAO.isAllowed(executor, kv.getKey(), parsedProcessDefinition.getProcessDefinition(), false)) {
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
    public Process startProcess(ParsedProcessDefinition parsedProcessDefinition, Map<String, Object> variables, Actor actor, String transitionName,
            Map<String, Object> transientVariables) {
        Preconditions.checkNotNull(actor, "can't start a process when actor is null");
        ExecutionContext executionContext = createProcessInternal(parsedProcessDefinition, variables, actor, null, transientVariables);
        grantProcessPermissions(parsedProcessDefinition, executionContext.getProcess(), actor);
        startProcessInternal(executionContext, transitionName);
        return executionContext.getProcess();
    }

    private void grantProcessPermissions(ParsedProcessDefinition parsedProcessDefinition, Process process, Actor actor) {
        boolean permissionsAreSetToProcessStarter = false;
        Executor processStarter = executorDAO.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
        Set<Permission> processStarterPermissions = getProcessPermissions(processStarter, parsedProcessDefinition);
        for (Executor executor : permissionDAO.getExecutorsWithPermission(parsedProcessDefinition.getProcessDefinition())) {
            Set<Permission> permissions = getProcessPermissions(executor, parsedProcessDefinition);
            if (Objects.equal(actor, executor)) {
                permissions = CollectionUtil.unionSet(permissions, processStarterPermissions);
                permissionsAreSetToProcessStarter = true;
            }
            if (permissions.size() > 0) {
                permissionDAO.setPermissions(executor, permissions, process);
            }
        }
        if (!permissionsAreSetToProcessStarter) {
            permissionDAO.setPermissions(actor, processStarterPermissions, process);
        }
    }

    public Process createSubprocess(ExecutionContext parentExecutionContext, ParsedProcessDefinition parsedProcessDefinition, Map<String, Object> variables,
            int index) {
        Process parentProcess = parentExecutionContext.getProcess();
        Node subProcessNode = parentExecutionContext.getNode();
        ExecutionContext subExecutionContext = createProcessInternal(parsedProcessDefinition, variables, null, parentProcess, null);
        nodeProcessDAO.create(new NodeProcess(subProcessNode, parentExecutionContext.getToken(), subExecutionContext.getProcess(), index));
        return subExecutionContext.getProcess();
    }

    public void startSubprocess(ExecutionContext parentExecutionContext, ExecutionContext executionContext) {
        parentExecutionContext
                .addLog(new SubprocessStartLog(parentExecutionContext.getNode(), parentExecutionContext.getToken(), executionContext.getProcess()));
        grantSubprocessPermissions(executionContext.getParsedProcessDefinition(), executionContext.getProcess(), parentExecutionContext.getProcess());
        startProcessInternal(executionContext, null);
    }

    private void grantSubprocessPermissions(ParsedProcessDefinition parsedProcessDefinition, Process subProcess, Process parentProcess) {
        Set<Executor> executors = new HashSet<>();
        executors.addAll(permissionDAO.getExecutorsWithPermission(parsedProcessDefinition.getProcessDefinition()));
        executors.addAll(permissionDAO.getExecutorsWithPermission(parentProcess));
        for (Executor executor : executors) {
            List<Permission> permissionsByParentProcess = permissionDAO.getIssuedPermissions(executor, parentProcess);
            Set<Permission> permissionsByDefinition = getProcessPermissions(executor, parsedProcessDefinition);
            Set<Permission> permissions = CollectionUtil.unionSet(permissionsByParentProcess, permissionsByDefinition);
            if (permissions.size() > 0) {
                permissionDAO.setPermissions(executor, permissions, subProcess);
            }
        }
    }

    private ExecutionContext createProcessInternal(ParsedProcessDefinition parsedProcessDefinition, Map<String, Object> variables, Actor actor,
            Process parentProcess, Map<String, Object> transientVariables) {
        Preconditions.checkNotNull(parsedProcessDefinition, "can't create a process when parsedProcessDefinition is null");
        Process process = new Process(parsedProcessDefinition.getProcessDefinitionVersion());
        Token rootToken = new Token(parsedProcessDefinition, process);
        process.setRootToken(rootToken);
        processDao.create(process);
        if (parentProcess != null) {
            process.setParentId(parentProcess.getId());
        }
        process.setHierarchyIds(
                ProcessHierarchyUtils.createHierarchy(parentProcess != null ? parentProcess.getHierarchyIds() : null, process.getId()));
        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, rootToken);
        if (actor != null) {
            executionContext.addLog(new ProcessStartLog(actor));
        }
        if (transientVariables != null) {
            for (Map.Entry<String, Object> entry : transientVariables.entrySet()) {
                executionContext.setTransientVariable(entry.getKey(), entry.getValue());
            }
        }
        executionContext.setVariableValues(variables);
        if (actor != null) {
            SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
            Swimlane swimlane = swimlaneDAO.findOrCreate(process, swimlaneDefinition);
            swimlane.assignExecutor(executionContext, actor, false);
        }
        return executionContext;
    }

    private void startProcessInternal(ExecutionContext executionContext, String transitionName) {
        // execute the start node
        StartNode startNode = executionContext.getParsedProcessDefinition().getStartStateNotNull();
        // startNode.enter(executionContext);
        Transition transition = null;
        if (transitionName != null) {
            transition = executionContext.getParsedProcessDefinition().getStartStateNotNull().getLeavingTransitionNotNull(transitionName);
        }
        startNode.leave(executionContext, transition);
    }
}
