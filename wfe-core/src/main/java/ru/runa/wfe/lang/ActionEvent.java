package ru.runa.wfe.lang;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ActionEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TRANSITION = "transition";
        
    public static final String NODE_ENTER = "node-enter";
    public static final String NODE_LEAVE = "node-leave";
            
    public static final String TASK_CREATE = "task-create";
    public static final String TASK_ASSIGN = "task-assign";
    public static final String TASK_END = "task-end";
    
    public static final String TIMER = "timer";

    private final String eventType;
    private final List<Action> actions = Lists.newArrayList();

    public ActionEvent(String eventType) {
        this.eventType = eventType;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action addAction(Action action) {
        Preconditions.checkNotNull(action, "can't add a null action to an event");
        actions.add(action);
        action.setEvent(this);
        return action;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return eventType;
    }
}
