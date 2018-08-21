package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;

/**
 * History graph node for parallel gateway.
 */
public class HistoryGraphParallelNodeModel extends HistoryGraphBaseNodeModel {
    public HistoryGraphParallelNodeModel(IProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, node, definitionModel, nodeFactory);
    }

    public HistoryGraphParallelNodeModel(IProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, definitionModel, nodeFactory);
    }

    @Override
    public boolean mayAcceptNewTransition() {
        return getTransitions().size() < getNode().getLeavingTransitions().size();
    }

    @Override
    public <TContext> void processBy(HistoryGraphNodeVisitor<TContext> visitor, TContext context) {
        visitor.onParallelNode(this, context);
    }

    public boolean isForkNode() {
        return getNode().getLeavingTransitions().size() > 1;
    }
}
