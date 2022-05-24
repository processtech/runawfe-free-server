package ru.runa.wfe.lang;

import com.google.common.base.Preconditions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentNodeEnterLog;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentTokenDao;

/**
 * Used for embedded subprocess merging.
 *
 * @since 4.1.0
 * @author dofs
 */
public class EmbeddedSubprocessStartNode extends StartNode implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private SubprocessNode subprocessNode;
    @Autowired
    private transient CurrentTokenDao currentTokenDao;

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
        executionContext.getCurrentToken().setNodeId(subprocessNode.getNodeId());
        executionContext.addLog(new CurrentNodeEnterLog(subprocessNode));
        super.enter(executionContext);
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        // leave this token point to declared subprocessNode
        executionContext.getCurrentToken().setNodeId(subprocessNode.getNodeId());
        executionContext.getCurrentToken().setNodeType(subprocessNode.getNodeType());
        executionContext.getCurrentToken().setNodeName(tokenNodeNameExtractor.extract(subprocessNode));
        // run in child token to prevent boundary event cancellation from embedded subprocess
        CurrentToken token = new CurrentToken(executionContext.getCurrentToken(), getNodeId());
        currentTokenDao.flushPendingChanges();
        leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), token));
    }

}
