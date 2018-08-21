package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.jpdl.Join;

/**
 * History graph node for {@link Join} element.
 */
public class HistoryGraphJoinNodeModel extends HistoryGraphBaseNodeModel {

    public HistoryGraphJoinNodeModel(IProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        super(processLog, node, definitionModel, nodeFactory);
    }

    public HistoryGraphJoinNodeModel(IProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
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
