package ru.runa.wfe.graph.image;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.awt.Color;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.graph.image.figure.bpmn.BpmnFigureFactory;
import ru.runa.wfe.graph.image.figure.uml.UmlFigureFactory;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskDeadlineUtils;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphImageBuilder {
    private final ParsedProcessDefinition parsedProcessDefinition;
    private Token highlightedToken;
    private final Map<String, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigure, RenderHits> transitionFigures = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newLinkedHashMap();
    private final Map<String, Set<String>> allSubprocessNameNodeIds = Maps.newHashMap();
    private final boolean smoothTransitions;

    public GraphImageBuilder(ParsedProcessDefinition parsedProcessDefinition) {
        this.parsedProcessDefinition = parsedProcessDefinition;
        this.smoothTransitions = DrawProperties.isSmoothLinesEnabled() && parsedProcessDefinition.getProcessDefinition().getLanguage() == Language.BPMN2;
        fillAllSubprocessNameNodeIds();
    }

    public void setHighlightedToken(Token highlightedToken) {
        this.highlightedToken = highlightedToken;
    }

    public byte[] createDiagram(Process process, ProcessLogs logs) throws Exception {
        AbstractFigureFactory factory;
        if (parsedProcessDefinition.getProcessDefinition().getLanguage() == Language.BPMN2) {
            factory = new BpmnFigureFactory();
        } else {
            factory = new UmlFigureFactory();
        }
        for (Node node : parsedProcessDefinition.getNodes(false)) {
            AbstractFigure nodeFigure = factory.createFigure(node, DrawProperties.useEdgingOnly());
            allNodeFigures.put(node.getNodeId(), nodeFigure);
        }
        for (Node node : parsedProcessDefinition.getNodes(false)) {
            String nodeId = node.getNodeId();
            AbstractFigure nodeFigure = allNodeFigures.get(node.getNodeId());
            Preconditions.checkNotNull(nodeFigure, "Node figure not found by id " + nodeId);
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            int leavingTransitionsCount = node.getLeavingTransitions().size();
            if (node.getNodeType() == NodeType.END_PROCESS) {
                continue;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                TransitionFigure transitionFigure = factory.createTransitionFigure();
                transitionFigure.init(transition, nodeFigure, figureTo, smoothTransitions);
                if (parsedProcessDefinition.getProcessDefinition().getLanguage() == Language.BPMN2) {
                    NodeType nodeType = node.getNodeType();
                    transitionFigure.setExclusive(nodeType != NodeType.EXCLUSIVE_GATEWAY && node.getNodeType() != NodeType.PARALLEL_GATEWAY
                            && leavingTransitionsCount > 1);
                }
                nodeFigure.addTransition(transitionFigure);
                if (!DrawProperties.useEdgingOnly()) {
                    transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        final Set<String> activeNodeIds = getActiveNodeIds(process.getRootToken(), new HashSet<String>());
        for (TransitionLog transitionLog : logs.getLogs(TransitionLog.class)) {
            Transition transition = transitionLog.getTransitionOrNull(parsedProcessDefinition);
            if (transition != null) {
                RenderHits renderHits = new RenderHits(DrawProperties.getHighlightColor(), true);
                // Mark 'from' block as PASSED
                AbstractFigure nodeModelFrom = allNodeFigures.get(transition.getFrom().getTransitionNodeId(false));
                nodeFigures.put(nodeModelFrom, renderHits);
                // Mark 'to' block as PASSED
                AbstractFigure nodeModelTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                nodeFigures.put(nodeModelTo, renderHits);
                if (nodeModelTo.getNode() instanceof SubprocessNode) {
                    fillActiveSubprocess(nodeModelTo, activeNodeIds);
                }
                if (nodeModelTo.getNode() instanceof BoundaryEventContainer) {
                    for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) nodeModelTo.getNode()).getBoundaryEvents()) {
                        AbstractFigure boundaryEventFigure = allNodeFigures.get(((GraphElement) boundaryEvent).getNodeId());
                        if (boundaryEventFigure == null) {
                            // case for EmbeddedSubprocessEndNode
                            continue;
                        }
                        nodeFigures.put(boundaryEventFigure, new RenderHits(DrawProperties.getHighlightColor(), false));
                    }
                }
                // Mark transition as PASSED
                TransitionFigure transitionFigure = nodeModelFrom.getTransition(transition.getName());
                transitionFigures.put(transitionFigure, renderHits);
            }
        }

        fillTasks(logs);
        GraphImage graphImage = new GraphImage(parsedProcessDefinition, transitionFigures, nodeFigures);
        return graphImage.getImageBytes();
    }

    private Set<String> getActiveNodeIds(Token token, Set<String> activeNodeIds) {
        if (token == null) {
            return Collections.emptySet();
        }
        activeNodeIds.add(token.getNodeId());
        for (Token childToken : token.getActiveChildren()) {
            activeNodeIds = getActiveNodeIds(childToken, activeNodeIds);
        }
        return activeNodeIds;
    }

    private void fillActiveSubprocess(AbstractFigure nodeModelTo, Set<String> activeNodeIds) {
        SubprocessNode subprocessNode = (SubprocessNode) nodeModelTo.getNode();
        Color color = DrawProperties.getHighlightColor();
        boolean isBold = false;
        if (!subprocessNode.isEmbedded()) {
            String subprocessNodeId = subprocessNode.getNodeId();
            if (activeNodeIds.contains(subprocessNodeId)) {
                if (highlightedToken == null || !highlightedToken.getNodeId().equals(subprocessNodeId)) {
                    color = DrawProperties.getBaseColor();
                }
                isBold = true;
            }
        } else {
            if (allSubprocessNameNodeIds.containsKey(subprocessNode.getSubProcessName())) {
                Set<String> intersection = allSubprocessNameNodeIds.get(subprocessNode.getSubProcessName());
                intersection.retainAll(activeNodeIds);
                if (!intersection.isEmpty()) {
                    if (highlightedToken == null || !intersection.contains(highlightedToken.getNodeId())) {
                        color = DrawProperties.getBaseColor();
                    }
                    isBold = true;
                }
            }
        }
        nodeFigures.put(nodeModelTo, new RenderHits(color, true, isBold));
    }

    private void fillTasks(ProcessLogs logs) {
        for (Map.Entry<TaskCreateLog, TaskEndLog> entry : logs.getTaskLogs().entrySet()) {
            TaskCreateLog taskCreateLog = entry.getKey();
            TaskEndLog taskEndLog = entry.getValue();

            boolean isActiveTask = taskEndLog == null;
            Date deadlineDate = taskCreateLog.getDeadlineDate();
            Date endDate = isActiveTask ? new Date() : taskEndLog.getCreateDate();
            AbstractFigure figure = allNodeFigures.get(taskCreateLog.getNodeId());
            if (figure == null) {
                // ru.runa.wfe.audit.CurrentTaskCreateLog.getNodeId() = null for old tasks
                continue;
            }
            Date deadlineWarningDate = TaskDeadlineUtils.getDeadlineWarningDate(taskCreateLog.getCreateDate(), deadlineDate);
            Color color = null;
            if (isActiveTask) {
                color = DrawProperties.getBaseColor();
                if (highlightedToken != null && Objects.equal(taskCreateLog.getTokenId(), highlightedToken.getId())) {
                    color = DrawProperties.getHighlightColor();
                }
            }
            if (deadlineDate != null && deadlineDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getAlarmColor();
            } else if (deadlineWarningDate != null && deadlineWarningDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getLightAlarmColor();
            }
            if (color != null) {
                nodeFigures.put(figure, new RenderHits(color, true, isActiveTask));
            }
        }
    }

    private void fillAllSubprocessNameNodeIds() {
        for (ParsedSubprocessDefinition subprocessDefinition : parsedProcessDefinition.getEmbeddedSubprocesses().values()) {
            allSubprocessNameNodeIds.computeIfAbsent(subprocessDefinition.getName(), new ComputeEmbeddedProcessNodeIds(subprocessDefinition));
        }
    }

    static class ComputeEmbeddedProcessNodeIds implements Function<String, Set<String>> {
        ParsedSubprocessDefinition subprocessDefinition;

        ComputeEmbeddedProcessNodeIds(ParsedSubprocessDefinition subprocessDefinition) {
            this.subprocessDefinition = subprocessDefinition;
        }

        @Override
        public Set<String> apply(String t) {
            Set<String> nodeIds = Sets.newHashSet();
            for (Node innerNode : subprocessDefinition.getNodes(false)) {
                nodeIds.add(innerNode.getNodeId());
            }
            return nodeIds;
        }
    }

}
