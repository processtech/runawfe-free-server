package ru.runa.wfe.lang.bpmn2;

import java.util.List;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.collect.Lists;

public class CatchEventNode extends BaseMessageNode implements BoundaryEventContainer, BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();
    private Boolean boundaryEventInterrupting;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    public void cancelBoundaryEvent(CurrentToken token) {
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting() {
        return TaskCompletionInfo.createForHandler(getEventType().name());
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getCurrentToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        super.leave(executionContext, transition);
        executionContext.getCurrentToken().setMessageSelector(null);
    }
}
