package ru.runa.wfe.lang;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.commons.bc.legacy.JbpmDuration;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.StartConditionalHandler;
import ru.runa.wfe.lang.bpmn2.EventHolder;
import ru.runa.wfe.lang.bpmn2.EventTrigger;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

@Getter
@Setter
public class StartNode extends InteractionNode implements EventHolder {
    private static final long serialVersionUID = 1L;

    private Delegation delegation;
    private TimerEventDefinition timerEventDefinition;
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

    public boolean isStartByCondition() {
        return eventTrigger.getEventType() == MessageEventType.conditional;
    }

    public TimerEventDefinition getTimerEventDefinition() {
        if (timerEventDefinition == null && isStartByCondition() && delegation != null) {
            initTimerEventDefinition();
        }
        return timerEventDefinition;
    }

    public List<Map<String, Object>> getOnTimerVariablesList() throws Exception {
        StartConditionalHandler handler = delegation.getInstance();
        return handler.execute(getParsedProcessDefinition());
    }

    private void initTimerEventDefinition() {
        String interval = ConditionalEventModel.fromXml(delegation.getConfiguration()).getInterval();
        timerEventDefinition = TimerEventDefinition.createTimeCycle(new JbpmDuration(interval));
    }
}
