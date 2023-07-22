package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.jpdl.Fork;

/**
 * History graph node for {@link Fork} element.
 */
public class HistoryGraphForkNodeModel extends HistoryGraphBaseNodeModel {

    public HistoryGraphForkNodeModel(ProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, node, definitionModel, nodeFactory);
    }

    public HistoryGraphForkNodeModel(ProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, definitionModel, nodeFactory);
    }

    @Override
    public boolean mayAcceptNewTransition() {
        return getTransitions().size() < getNode().getLeavingTransitions().size();
    }

    @Override
    public <TContext> void processBy(HistoryGraphNodeVisitor<TContext> visitor, TContext context) {
        visitor.onForkNode(this, context);
    }
}
