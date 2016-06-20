package ru.runa.wfe.lang;

import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.execution.ExecutionContext;

/**
 * Used for embedded subprocess merging.
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessEndNode extends Node {
    private static final long serialVersionUID = 1L;
    private SubProcessState subProcessState;

    public void setSubProcessState(SubProcessState subProcessState) {
        this.subProcessState = subProcessState;
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
        return subProcessState.getNodeId();
    }
    
    @Override
    public void execute(ExecutionContext executionContext) {
        leave(executionContext);
    }

    @Override
    protected void addLeaveLog(ExecutionContext executionContext) {
        super.addLeaveLog(executionContext);
        executionContext.getToken().setNodeId(subProcessState.getNodeId());
        executionContext.addLog(new NodeLeaveLog(subProcessState));
        executionContext.getToken().setNodeId(getNodeId());
    }

}
