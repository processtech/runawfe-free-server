package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

public interface TransitionLog extends ProcessLog {

    @Transient
    String getFromNodeId();

    @Transient
    String getToNodeId();

    @Transient
    String getTransitionId();

    @Transient
    Transition getTransitionOrNull(ProcessDefinition processDefinition);
}
