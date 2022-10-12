package ru.runa.wfe.lang;

import java.util.List;
import ru.runa.wfe.audit.CurrentNodeLeaveLog;
import ru.runa.wfe.execution.ExecutionContext;

/**
 * Used for embedded subprocess merging.
 * 
 * @since 4.1.0
 * @author dofs
 */
public abstract class EmbeddedSubprocessEndNode extends Node implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    protected SubprocessNode subprocessNode;

    public void setSubprocessNode(SubprocessNode subprocessNode) {
        this.subprocessNode = subprocessNode;
    }

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return subprocessNode.getBoundaryEvents();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.END_PROCESS;
    }

    @Override
    public String getTransitionNodeId(boolean arriving) {
        if (arriving) {
            return super.getTransitionNodeId(arriving);
        }
        return subprocessNode.getNodeId();
    }

    @Override
    protected void addLeaveLog(ExecutionContext executionContext) {
        super.addLeaveLog(executionContext);
        executionContext.getCurrentToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new CurrentNodeLeaveLog(subprocessNode));
        executionContext.getCurrentToken().setNodeId(getNodeId());
    }

}
