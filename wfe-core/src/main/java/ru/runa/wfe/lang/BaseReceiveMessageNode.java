package ru.runa.wfe.lang;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.VariableMapping;

public class BaseReceiveMessageNode extends BaseMessageNode implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
    }

    public void leave(ExecutionContext executionContext, Map<String, Object> map) {
        String swimlaneName = null;
        boolean setSwimlaneValue = true;
        if (this instanceof BoundaryEvent && getParentElement() instanceof BaseTaskNode) {
            TaskDefinition taskDefinition = ((BaseTaskNode) getParentElement()).getFirstTaskNotNull();
            swimlaneName = taskDefinition.getSwimlane().getName();
            setSwimlaneValue = taskDefinition.isReassignSwimlaneToTaskPerformer();
        }
        for (VariableMapping variableMapping : getVariableMappings()) {
            if (!variableMapping.isPropertySelector()) {
                if (map.containsKey(variableMapping.getMappedName())) {
                    Object value = map.get(variableMapping.getMappedName());
                    if (Objects.equals(swimlaneName, variableMapping.getName()) && !setSwimlaneValue) {
                        log.warn("ignored to set swimlane value from signal for '" + variableMapping.getMappedName() + "'");
                        continue;
                    }
                    executionContext.setVariableValue(variableMapping.getName(), value);
                } else {
                    log.warn("message does not contain value for '" + variableMapping.getMappedName() + "'");
                }
            }
        }
        super.leave(executionContext);
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        executionContext.getToken().setMessageSelector(null);
        super.leave(executionContext, transition);
    }

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

}
