package ru.runa.wfe.lang;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.execution.ExecutionContext;
import com.google.common.base.Preconditions;

/**
 * Used for embedded subprocess merging.
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessStartNode extends StartState {
    private static final long serialVersionUID = 1L;
    private SubProcessState subProcessState;

    public void setSubProcessState(SubProcessState subProcessState) {
        this.subProcessState = subProcessState;
    }

    @Override
    public String getTransitionNodeId(boolean arriving) {
        if (arriving) {
            return subProcessState.getNodeId();
        }
        return super.getTransitionNodeId(arriving);
    }

    @Override
    public void enter(ExecutionContext executionContext) {
        Preconditions.checkNotNull(subProcessState, "subProcessState");
        executionContext.getToken().setNodeId(subProcessState.getNodeId());
        executionContext.addLog(new NodeEnterLog(subProcessState));
        super.enter(executionContext);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        leave(executionContext);
    }

}
