package ru.runa.wfe.graph.image;

import ru.runa.wfe.job.CancelTimerAction;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.job.Timer;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;

public class GraphImageHelper {
    public static int processActionsInEvent(Event event) {
        int result = 0;
        for (Action action : event.getActions()) {
            if (action instanceof CreateTimerAction || action instanceof CancelTimerAction || Timer.ESCALATION_NAME.equals(action.getName())) {
                continue;
            }
            result++;
        }
        return result;
    }

    public static int getNodeActionsCount(GraphElement node) {
        int result = 0;
        for (Event event : node.getEvents().values()) {
            result += processActionsInEvent(event);
        }
        if (node instanceof TaskNode) {
            for (TaskDefinition taskDefinition : ((TaskNode) node).getTasks()) {
                result += getNodeActionsCount(taskDefinition);
            }
        }
        return result;
    }

    public static int getTransitionActionsCount(Transition transition) {
        Event event = transition.getEventNotNull(Event.TRANSITION);
        return event.getActions().size();
    }

}
