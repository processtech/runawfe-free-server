package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;

/**
 * History graph node for generic element, such as tasks, decision, subprocess
 * and so on. Elements with no child tokens and one incoming token.
 */
public class HistoryGraphGenericNodeModel extends HistoryGraphBaseNodeModel {

    public HistoryGraphGenericNodeModel(ProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, node, definitionModel, nodeFactory);
    }

    public HistoryGraphGenericNodeModel(ProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, definitionModel, nodeFactory);
    }

    @Override
    public boolean mayAcceptNewTransition() {
        return getTransitions().size() == 0;
    }

    @Override
    public <TContext> void processBy(HistoryGraphNodeVisitor<TContext> visitor, TContext context) {
        visitor.onGenericNode(this, context);
    }
}
