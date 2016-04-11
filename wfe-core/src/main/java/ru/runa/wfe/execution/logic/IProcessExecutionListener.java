package ru.runa.wfe.execution.logic;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.Transition;

public interface IProcessExecutionListener {

    public void onNodeLeave(ExecutionContext executionContext, Node node, Transition transition);

}
