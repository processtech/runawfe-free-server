package ru.runa.wfe.graph.image;

import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.jpdl.Action;
import ru.runa.wfe.lang.jpdl.ActionEvent;
import ru.runa.wfe.lang.jpdl.CancelTimerAction;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;

public class GraphImageHelper {
    public static int processActionsInEvent(ActionEvent actionEvent) {
        int result = 0;
        for (Action action : actionEvent.getActions()) {
            if (action instanceof CreateTimerAction || action instanceof CancelTimerAction || TimerJob.ESCALATION_NAME.equals(action.getName())) {
                continue;
            }
            result++;
        }
        return result;
    }

    public static int getNodeActionsCount(GraphElement node) {
        int result = 0;
        for (ActionEvent actionEvent : node.getEvents().values()) {
            result += processActionsInEvent(actionEvent);
        }
        if (node instanceof TaskNode) {
            for (TaskDefinition taskDefinition : ((TaskNode) node).getTasks()) {
                result += getNodeActionsCount(taskDefinition);
            }
        }
        return result;
    }

    public static int getTransitionActionsCount(Transition transition) {
        ActionEvent actionEvent = transition.getEventNotNull(ActionEvent.TRANSITION);
        return actionEvent.getActions().size();
    }

}
