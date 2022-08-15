package ru.runa.wfe.lang;

import java.util.List;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;

/**
 * Used for embedded subprocess merging.
 *
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessEndNode extends Node implements BoundaryEventContainer {
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
    protected void execute(ExecutionContext executionContext) throws Exception {
        Token enterToken = executionContext.getToken();
        while (!enterToken.getNodeId().equals(subprocessNode.getNodeId())) {
            enterToken = enterToken.getParent();
            if (enterToken == null) {
                // uncomment in future version
                // throw new InternalApplicationException("No corresponding token found for embedded subprocess end");
                log.warn("No corresponding token found for embedded subprocess end; providing backwards compatibility behavior");
                leave(executionContext);
                return;
            }
        }
        // continue in token from EmbeddedSubprocessStartNode
        executionContext.getToken().end(executionContext.getProcessDefinition(), null, null, false);
        leave(new ExecutionContext(executionContext.getProcessDefinition(), enterToken));
    }

    @Override
    protected void addLeaveLog(ExecutionContext executionContext) {
        super.addLeaveLog(executionContext);
        executionContext.getToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new NodeLeaveLog(subprocessNode));
        executionContext.getToken().setNodeId(getNodeId());
    }

}
