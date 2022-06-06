package ru.runa.wfe.lang;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentNodeLeaveLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
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
    private transient CurrentTokenDao tokenDao;
    @Autowired
    private transient ExecutionLogic executionLogic;

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
        CurrentToken enterToken = executionContext.getCurrentToken();
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
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
        leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), enterToken));
    }

    @Override
    protected void addLeaveLog(ExecutionContext executionContext) {
        super.addLeaveLog(executionContext);
        executionContext.getCurrentToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new CurrentNodeLeaveLog(subprocessNode));
        executionContext.getCurrentToken().setNodeId(getNodeId());
    }

    /**
     * This implementation has a pitfall: in case of multiple active embedded subprocesses all will be cancelled by single event
     */
    @Override
    public void endBoundaryEventTokens(ExecutionContext executionContext) {
        List<BoundaryEvent> boundaryEvents = ((BoundaryEventContainer) this).getBoundaryEvents();
        for (BoundaryEvent boundaryEvent : boundaryEvents) {
            if (boundaryEvent instanceof CatchEventNode) {
                String boundaryEventNodeId = ((CatchEventNode) boundaryEvent).getNodeId();
                List<CurrentToken> activeTokens = tokenDao.findByProcessAndNodeIdAndExecutionStatus(executionContext.getCurrentProcess(),
                        boundaryEventNodeId, ExecutionStatus.ACTIVE);
                ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();
                for (CurrentToken token : activeTokens) {
                    executionLogic.endToken(token, executionContext.getParsedProcessDefinition(), null, null, false);
                }
            }
        }
    }

}
