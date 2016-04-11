package ru.runa.wfe.graph.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.SubProcessState;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

/**
 * Creates graph element presentations for tooltips.
 */
public class CreateGraphElementPresentation implements HistoryGraphNodeVisitor<CreateGraphElementPresentationContext> {

    private final List<GraphElementPresentation> presentationElements = new ArrayList<GraphElementPresentation>();
    private final GraphHistoryBuilderData data;
    private final HashSet<HistoryGraphNode> visited = new HashSet<HistoryGraphNode>();

    public CreateGraphElementPresentation(GraphHistoryBuilderData data) {
        super();
        this.data = data;
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, CreateGraphElementPresentationContext context) {
        noTooltipNodeProcessing(node, context);
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, CreateGraphElementPresentationContext context) {
        noTooltipNodeProcessing(node, context);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, CreateGraphElementPresentationContext context) {
        noTooltipNodeProcessing(node, context);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, CreateGraphElementPresentationContext context) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        addedTooltipOnGraph(node);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
    }

    private void addedTooltipOnGraph(HistoryGraphNode historyNode) {
        NodeEnterLog nodeEnterLog = historyNode.getNodeLog(NodeEnterLog.class);
        NodeLeaveLog nodeLeaveLog = historyNode.getNodeLog(NodeLeaveLog.class);
        if (nodeEnterLog == null) {
            return;
        }
        GraphElementPresentation presentation;
        NodeType nodeType = historyNode.getNode().getNodeType();
        NodeLayoutData layoutData = NodeLayoutData.get(historyNode);
        switch (nodeType) {
        case SUBPROCESS:
            presentation = new SubprocessGraphElementPresentation();
            ((SubprocessGraphElementPresentation) presentation).setSubprocessAccessible(true);
            SubprocessStartLog startSub = historyNode.getNodeLog(SubprocessStartLog.class);
            if (startSub != null) {
                ((SubprocessGraphElementPresentation) presentation).setSubprocessId(startSub.getSubprocessId());
                break;
            }
            if (((SubProcessState) historyNode.getNode()).isEmbedded()) {
                NodeEnterLog subprocessLog = null;
                for (NodeEnterLog candidate : historyNode.getNodeLogs(NodeEnterLog.class)) {
                    if (candidate.getNodeType() == NodeType.SUBPROCESS) {
                        subprocessLog = candidate;
                        break;
                    }
                }
                if (subprocessLog == null) {
                    return;
                }
                ((SubprocessGraphElementPresentation) presentation).setSubprocessId(subprocessLog.getProcessId());
                SubprocessDefinition subprocessDefinition = data.getEmbeddedSubprocess(((SubProcessState) historyNode.getNode()).getSubProcessName());
                ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessId(subprocessDefinition.getNodeId());
                ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessGraphWidth(subprocessDefinition.getGraphConstraints()[2]);
                ((SubprocessGraphElementPresentation) presentation).setEmbeddedSubprocessGraphHeight(subprocessDefinition.getGraphConstraints()[3]);

                if (nodeLeaveLog == null) {
                    presentation.initialize(historyNode.getNode(), layoutData.getConstraints());
                    presentation.setData("");
                    presentationElements.add(presentation);
                    return;
                }
            }
            break;
        case MULTI_SUBPROCESS:
            presentation = new MultiinstanceGraphElementPresentation();
            for (SubprocessStartLog subprocessStartLog : historyNode.getNodeLogs(SubprocessStartLog.class)) {
                ((MultiinstanceGraphElementPresentation) presentation).addSubprocessInfo(subprocessStartLog.getSubprocessId(), true, false);
            }
            break;
        case TASK_STATE:
            presentation = new TaskGraphElementPresentation();
            break;
        default:
            presentation = new GraphElementPresentation();
        }

        if (nodeLeaveLog == null) {
            return;
        }

        presentation.initialize(historyNode.getNode(), layoutData.getConstraints());
        String executionPeriodString = getPeriodDateString(nodeEnterLog, nodeLeaveLog);

        if (nodeType.equals(NodeType.SUBPROCESS) || nodeType.equals(NodeType.MULTI_SUBPROCESS)) {
            presentation.setData("Time period is " + executionPeriodString);
        } else if (nodeType.equals(NodeType.TASK_STATE)) {
            StringBuffer str = new StringBuffer();
            TaskEndLog taskEndLog = historyNode.getNodeLog(TaskEndLog.class);
            if (taskEndLog != null) {
                String actor = taskEndLog.getActorName();
                TaskAssignLog prev = historyNode.getNodeLog(TaskAssignLog.class);
                if (prev != null) {
                    if (prev.getOldExecutorName() != null && !prev.getOldExecutorName().equals(actor)) {
                        actor = prev.getOldExecutorName();
                    }
                }

                Executor performedTaskExecutor = data.getExecutorByName(actor);
                if (performedTaskExecutor != null) {
                    if (performedTaskExecutor instanceof Actor && ((Actor) performedTaskExecutor).getFullName() != null) {
                        str.append("Full Name is " + ((Actor) performedTaskExecutor).getFullName() + ".</br>");
                    }
                    str.append("Login is " + performedTaskExecutor.getName() + ".</br>");
                }
            }

            str.append("Time period is " + executionPeriodString + ".");
            presentation.setData(str.toString());
        }
        presentationElements.add(presentation);
    }

    private String getPeriodDateString(ProcessLog firstLog, ProcessLog secondLog) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(firstLog.getCreateDate());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(secondLog.getCreateDate());
        long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        periodCal.setTimeInMillis(period - periodCal.getTimeZone().getOffset(period));

        StringBuilder result = new StringBuilder();
        long days = period / (24 * 60 * 60 * 1000);
        if (days > 0) {
            result.append((days == 1) ? "1 day " : (String.valueOf(days) + " days "));
        }
        result.append(CalendarUtil.formatTime(periodCal.getTime()));
        return result.toString();
    }

    private void noTooltipNodeProcessing(HistoryGraphNode node, CreateGraphElementPresentationContext context) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
    }

    public List<GraphElementPresentation> getPresentationElements() {
        return presentationElements;
    }
}
