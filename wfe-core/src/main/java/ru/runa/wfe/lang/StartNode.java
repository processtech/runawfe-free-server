package ru.runa.wfe.lang;

import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.bpmn2.EventHolder;
import ru.runa.wfe.lang.bpmn2.EventTrigger;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

public class StartNode extends InteractionNode implements EventHolder {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private TimerEventDefinition timerEventDefinition;

    @Getter
    private final EventTrigger eventTrigger = new EventTrigger();

    @Override
    public NodeType getNodeType() {
        return NodeType.START_EVENT;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
    }

    public boolean isStartByEvent() {
        return eventTrigger.getEventType() != null;
    }
}
