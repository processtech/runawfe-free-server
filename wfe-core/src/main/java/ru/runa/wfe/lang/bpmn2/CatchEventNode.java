package ru.runa.wfe.lang.bpmn2;

import java.util.Objects;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.BaseReceiveMessageNode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableMapping;

public class CatchEventNode extends BaseReceiveMessageNode implements BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private Boolean boundaryEventInterrupting;

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public void cancelBoundaryEvent(CurrentToken token) {
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting(ExecutionContext executionContext) {
        if (getParentElement() instanceof BaseTaskNode) {
            String swimlaneName = ((BaseTaskNode) getParentElement()).getFirstTaskNotNull().getSwimlane().getName();
            for (VariableMapping variableMapping : getVariableMappings()) {
                if (!variableMapping.isPropertySelector()) {
                    if (Objects.equals(swimlaneName, variableMapping.getName())) {
                        return TaskCompletionInfo.createForSignal((Executor) executionContext.getVariableValue(swimlaneName),
                                getLeavingTransitions().get(0).getName());
                    }
                }
            }
        }
        return TaskCompletionInfo.createForSignal(null, getLeavingTransitions().get(0).getName());
    }
}
