package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.jpdl.Join;

/**
 * History graph node for {@link Join} element.
 */
public class HistoryGraphJoinNodeModel extends HistoryGraphBaseNodeModel {

    public HistoryGraphJoinNodeModel(ProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, node, definitionModel, nodeFactory);
    }

    public HistoryGraphJoinNodeModel(ProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, definitionModel, nodeFactory);
    }

    @Override
    public boolean mayAcceptNewTransition() {
        return getTransitions().size() == 0;
    }

    @Override
    public <TContext> void processBy(HistoryGraphNodeVisitor<TContext> visitor, TContext context) {
        visitor.onJoinNode(this, context);
    }
}
