package ru.runa.wfe.lang;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;

/**
 * Used for embedded subprocess merging.
 *
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessEndNode extends Node implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private SubprocessNode subprocessNode;
    @Autowired
    private transient TokenDao tokenDao;

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
        leave(executionContext);
    }

    @Override
    protected void addLeaveLog(ExecutionContext executionContext) {
        super.addLeaveLog(executionContext);
        executionContext.getToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new NodeLeaveLog(subprocessNode));
        executionContext.getToken().setNodeId(getNodeId());
    }

    @Override
    public void endBoundaryEventTokens(ExecutionContext executionContext) {
        List<BoundaryEvent> boundaryEvents = ((BoundaryEventContainer) this).getBoundaryEvents();
        for (BoundaryEvent boundaryEvent : boundaryEvents) {
            if (boundaryEvent instanceof CatchEventNode) {
                String boundaryEventNodeId = ((CatchEventNode) boundaryEvent).getNodeId();
                List<Token> activeTokens = tokenDao.findByProcessAndNodeIdAndExecutionStatus(executionContext.getProcess(), boundaryEventNodeId,
                        ExecutionStatus.ACTIVE);
                for (Token token : activeTokens) {
                    token.end(executionContext.getProcessDefinition(), null, null, false);
                }
            }
        }
    }

}
