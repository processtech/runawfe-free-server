package ru.runa.wfe.graph.view;

import com.google.common.base.Objects;
import java.util.List;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.User;

/**
 * Operation to add identities of started subprocesses to graph elements.
 */
public class ProcessGraphInfoVisitor extends NodeGraphElementVisitor {

    /**
     * Current subject.
     */
    private final User user;

    /**
     * Instances of subprocesses, which must be added to graph elements.
     */
    private final List<NodeProcess> nodeProcesses;
    private final ProcessDefinition definition;
    private final Process process;
    private final ProcessLogs processLogs;

    /**
     * Create instance of operation to set starting process readable flag.
     *
     * @param nodeProcesses
     *            Subprocesses which must be added to graph elements.
     */
    public ProcessGraphInfoVisitor(User user, ProcessDefinition definition, Process process, ProcessLogs processLogs, List<NodeProcess> nodeProcesses) {
        this.user = user;
        this.definition = definition;
        this.process = process;
        this.processLogs = processLogs;
        this.nodeProcesses = nodeProcesses;
    }

    @Override
    public void visit(NodeGraphElement element) {
        super.visit(element);
        if (processLogs != null) {
            List<IProcessLog> logs = processLogs.getLogs(element.getNodeId());
            if (logs.size() > 0) {
                element.setData(logs);
            }
        }
    }

    @Override
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {
        for (NodeProcess nodeProcess : nodeProcesses) {
            if (Objects.equal(nodeProcess.getNodeId(), element.getNodeId())) {
                element.addSubprocessInfo(nodeProcess.getSubProcess().getId(), hasReadPermission(nodeProcess.getSubProcess()), nodeProcess
                        .getSubProcess().hasEnded());
            }
        }
    }

    @Override
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {
        if (element.isEmbedded()) {
            boolean b = ApplicationContextFactory.getProcessLogDao().isNodeEntered(process, element.getNodeId());
            element.setSubprocessAccessible(b);
            element.setSubprocessId(process.getId());
            SubprocessDefinition subprocessDefinition = definition.getEmbeddedSubprocessByNameNotNull(element.getSubprocessName());
            element.setEmbeddedSubprocessId(subprocessDefinition.getNodeId());
            element.setEmbeddedSubprocessGraphWidth(subprocessDefinition.getGraphConstraints()[2]);
            element.setEmbeddedSubprocessGraphHeight(subprocessDefinition.getGraphConstraints()[3]);
        } else {
            Process lastSubProcess = null;
            for (NodeProcess nodeProcess : nodeProcesses) {
                if (Objects.equal(nodeProcess.getNodeId(), element.getNodeId())) {
                    if (lastSubProcess == null || lastSubProcess.getId().compareTo(nodeProcess.getSubProcess().getId()) < 0) {
                        lastSubProcess = nodeProcess.getSubProcess();
                    }
                }
            }
            if (lastSubProcess != null) {
                element.setSubprocessId(lastSubProcess.getId());
                element.setSubprocessAccessible(hasReadPermission(lastSubProcess));
            }
        }
    }

    /**
     * Check READ permission on process instance for current subject.
     * 
     * @param process
     *            Process instance to check READ permission.
     * @return true, if current actor can read process definition and false otherwise.
     */
    private boolean hasReadPermission(Process process) {
        PermissionDao permissionDao = ApplicationContextFactory.getPermissionDao();
        return permissionDao.isAllowed(user, Permission.LIST, process);
    }

}
