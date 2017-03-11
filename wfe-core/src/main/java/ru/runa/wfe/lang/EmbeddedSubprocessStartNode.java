package ru.runa.wfe.lang;

import java.util.List;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Preconditions;

/**
 * Used for embedded subprocess merging.
 *
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessStartNode extends StartNode implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private SubprocessNode subprocessNode;

    public void setSubprocessNode(SubprocessNode subprocessNode) {
        this.subprocessNode = subprocessNode;
    }

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return subprocessNode.getBoundaryEvents();
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
    protected void execute(ExecutionContext executionContext) throws Exception {
        leave(executionContext);
    }

}
