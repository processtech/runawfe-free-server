/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.graph.image;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import ru.runa.wfe.audit.*;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.lang.*;
import ru.runa.wfe.task.TaskDeadlineUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ru.runa.wfe.lang.NodeType.EXCLUSIVE_GATEWAY;
import static ru.runa.wfe.lang.NodeType.PARALLEL_GATEWAY;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 * Modified on 19.01.2021 by msin87
 */

/*
todo: Полный рефакторинг!
gavrusev_sergei, надеюсь ты больше так не пишешь код.
Причина: слишком много лишних циклов, большая связность кода. Необходимо инвертировать
зависимости везде, где только можно, название переменных не отражает их сути.
Необходима документация на класс, на методы, на поля.
 */
public class GraphImageBuilder {
    private final ProcessDefinition processDefinition;
    private Token highlightedToken;
    private final Map<String, AbstractFigure> figuresMap = Maps.newHashMap();
    private final Map<TransitionFigure, RenderHits> transitionFigures = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newLinkedHashMap();
    private final boolean smoothTransitions;

    public GraphImageBuilder(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        this.smoothTransitions = DrawProperties.isSmoothLinesEnabled() && processDefinition.getDeployment().getLanguage() == Language.BPMN2;
    }

    public void setHighlightedToken(Token highlightedToken) {
        this.highlightedToken = highlightedToken;
    }

    private void initFiguresMap(List<Node> nodeList, AbstractFigureFactory factory, NodeEnterLog lastNodeEnterLog) {
        for (Node node : nodeList) {
            AbstractFigure nodeFigure = factory.createFigure(node, DrawProperties.useEdgingOnly());
            figuresMap.put(node.getNodeId(), nodeFigure);
        }
    }

    private List<String> getActiveNodesId(ProcessLogs logs) {
        NodeEnterLog lastEnterLog = logs.getLastOrNull(NodeEnterLog.class);
        List<String> activeNodeIdList = new ArrayList<>();
        if (lastEnterLog != null) {
            NodeLeaveLog lastLeaveLog = logs.getLastOrNull(NodeLeaveLog.class);
            //noinspection SwitchStatementWithTooFewBranches
            switch (lastLeaveLog.getNodeType()){
                case PARALLEL_GATEWAY:
                    AbstractFigure figure = figuresMap.get(lastLeaveLog.getNodeId());
                    if (figure != null) {
                        Map<String, TransitionFigure> transitionFigureMap = figure.getTransitions();
                        for (Map.Entry<String, TransitionFigure> transitionFigureEntry : transitionFigureMap.entrySet()) {
                            activeNodeIdList.add(transitionFigureEntry.getValue().getFigureTo().getNode().getNodeId());
                        }
                    }
                    break;
                    //add here other node types
                default:
                    break;
            }
        }
        return activeNodeIdList;
    }

    private void initNodeTransitions(Node node, AbstractFigure nodeFigure, AbstractFigureFactory factory) {
        int leavingTransitionsCount = node.getLeavingTransitions().size();
        for (Transition transition : node.getLeavingTransitions()) {
            AbstractFigure figureTo = figuresMap.get(transition.getTo().getTransitionNodeId(true));
            TransitionFigure transitionFigure = factory.createTransitionFigure();
            transitionFigure.init(transition, nodeFigure, figureTo, smoothTransitions);
            if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
                NodeType nodeType = node.getNodeType();
                transitionFigure.setExclusive(
                        nodeType != EXCLUSIVE_GATEWAY && nodeType != PARALLEL_GATEWAY && leavingTransitionsCount > 1);
            }
            nodeFigure.addTransition(transitionFigure);
            if (!DrawProperties.useEdgingOnly()) {
                transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
            }
        }
    }

    private void initAllNodesTransitions(AbstractFigureFactory factory) {
        for (Node node : processDefinition.getNodes(false)) {
            AbstractFigure nodeFigure = figuresMap.get(node.getNodeId());
            Preconditions.checkNotNull(nodeFigure, "Node figure not found by id " + node.getNodeId());
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            if (node.getNodeType() == NodeType.END_PROCESS) {
                continue;
            }
            initNodeTransitions(node, nodeFigure, factory);
        }
    }

    private boolean isActiveNode(String nodeId, ProcessLogs logs) {
        List<String> activeNodeIdList = getActiveNodesId(logs);
        for (String activeNodeId : activeNodeIdList) {
            if (activeNodeId.equals(nodeId))
                return true;
        }
        return false;
    }

    private void applyRenderHitsToNodeFigures(ProcessLogs logs) {
        for (TransitionLog transitionLog : logs.getLogs(TransitionLog.class)) {
            Transition transition = transitionLog.getTransitionOrNull(processDefinition);
            if (transition != null) {
                RenderHits renderHits = new RenderHits(DrawProperties.getHighlightColor(), true);
                // Mark 'from' block as PASSED
                AbstractFigure nodeModelFrom = figuresMap.get(transition.getFrom().getTransitionNodeId(false));
                nodeFigures.put(nodeModelFrom, renderHits);
                // Mark 'to' block as PASSED
                AbstractFigure nodeModelTo = figuresMap.get(transition.getTo().getTransitionNodeId(true));
                if (isActiveNode(nodeModelTo.getNode().getNodeId(), logs)) {
                    renderHits = new RenderHits(DrawProperties.getHighlightColor(), true, true);
                }
                nodeFigures.put(nodeModelTo, renderHits);
                if (nodeModelTo.getNode() instanceof BoundaryEventContainer) {
                    for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) nodeModelTo.getNode()).getBoundaryEvents()) {
                        AbstractFigure boundaryEventFigure = figuresMap.get(((GraphElement) boundaryEvent).getNodeId());
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
    }

    public byte[] createDiagram(Process process, ProcessLogs logs, AbstractFigureFactory factory) throws Exception {
        initFiguresMap(processDefinition.getNodes(false), factory, logs.getLastOrNull(NodeEnterLog.class));
        initAllNodesTransitions(factory);
        applyRenderHitsToNodeFigures(logs);
        fillActiveSubprocesses(process.getRootToken());
        fillTasks(logs);
        GraphImage graphImage = new GraphImage(processDefinition, transitionFigures, nodeFigures);
        return graphImage.getImageBytes();
    }

    private void fillActiveSubprocesses(Token token) {
        for (Token childToken : token.getActiveChildren()) {
            fillActiveSubprocesses(childToken);
        }
        if (processDefinition.getNode(token.getNodeId()) != null && token.getNodeNotNull(processDefinition) instanceof SubprocessNode) {
            AbstractFigure node = figuresMap.get(token.getNodeNotNull(processDefinition).getNodeId());
            Color color;
            if (highlightedToken != null && Objects.equal(highlightedToken.getId(), token.getId())) {
                color = DrawProperties.getHighlightColor();
            } else {
                color = DrawProperties.getBaseColor();
            }
            if (node != null) {
                nodeFigures.put(node, new RenderHits(color, true, true));
            }
        }
    }

    private void fillTasks(ProcessLogs logs) {
        for (Map.Entry<TaskCreateLog, TaskEndLog> entry : logs.getTaskLogs().entrySet()) {
            boolean activeTask = entry.getValue() == null;
            Date deadlineDate = entry.getKey().getDeadlineDate();
            Date endDate = activeTask ? new Date() : entry.getValue().getCreateDate();
            AbstractFigure figure = figuresMap.get(entry.getKey().getNodeId());
            if (figure == null) {
                // ru.runa.wfe.audit.TaskCreateLog.getNodeId() = null for old
                // tasks
                continue;
            }
            Date deadlineWarningDate = TaskDeadlineUtils.getDeadlineWarningDate(entry.getKey().getCreateDate(), deadlineDate);
            Color color = null;
            if (activeTask) {
                color = DrawProperties.getBaseColor();
                if (highlightedToken != null && Objects.equal(entry.getKey().getTokenId(), highlightedToken.getId())) {
                    color = DrawProperties.getHighlightColor();
                }
            }
            if (deadlineDate != null && deadlineDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getAlarmColor();
            } else if (deadlineWarningDate != null && deadlineWarningDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getLightAlarmColor();
            }
            if (color != null) {
                nodeFigures.put(figure, new RenderHits(color, true, activeTask));
            }
        }
    }
}
