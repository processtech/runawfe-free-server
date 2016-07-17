package ru.runa.wfe.lang;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.execution.ExecutionContext;
import com.google.common.base.Preconditions;

/**
 * Used for embedded subprocess merging.
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessStartNode extends StartNode {
    private static final long serialVersionUID = 1L;
    private SubprocessNode subprocessNode;

    public void setSubProcessState(SubprocessNode subprocessNode) {
        this.subprocessNode = subprocessNode;
    }

    @Override
    public String getTransitionNodeId(boolean arriving) {
        if (arriving) {
            return subprocessNode.getNodeId();
        }
        return super.getTransitionNodeId(arriving);
    }

    @Override
    public void enter(ExecutionContext executionContext) {
        Preconditions.checkNotNull(subprocessNode, "subprocessNode");
        executionContext.getToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new NodeEnterLog(subprocessNode));
        super.enter(executionContext);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        leave(executionContext);
    }

}
