package ru.runa.wfe.lang.jpdl;

import com.google.common.base.Objects;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class Join extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.JOIN;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();
        CurrentToken token = executionContext.getCurrentToken();
        executionLogic.endToken(token, executionContext.getParsedProcessDefinition(), null, null, false);
        if (token.isAbleToReactivateParent()) {
            token.setAbleToReactivateParent(false);
            CurrentToken parentToken = token.getParent();
            boolean reactivateParent = true;
            for (CurrentToken childToken : parentToken.getActiveChildren(false)) {
                if (childToken.isAbleToReactivateParent()) {
                    reactivateParent = false;
                    log.debug("There are exists at least 1 active token that can reactivate parent: " + childToken);
                    break;
                }
                if (!Objects.equal(childToken.getNodeId(), getNodeId())) {
                    reactivateParent = false;
                    log.debug(childToken + " is in state (" + childToken.getNodeId() + ") instead of this join (" + getNodeId() + ")");
                    break;
                }
            }
            if (reactivateParent) {
                leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), parentToken));
            }
        } else {
            log.debug(token + " unable to activate the parent");
        }
    }

}
