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

import java.awt.Color;
import java.util.Date;
import java.util.Map;

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
import ru.runa.wfe.graph.image.figure.TransitionFigureBase;
import ru.runa.wfe.graph.image.figure.bpmn.BPMNFigureFactory;
import ru.runa.wfe.graph.image.figure.uml.UMLFigureFactory;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskDeadlineUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphImageBuilder {
    private final ProcessDefinition processDefinition;
    private Token highlightedToken;
    private final Map<String, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigureBase, RenderHits> transitionFigureBases = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newHashMap();
    private final boolean smoothTransitions;

    public GraphImageBuilder(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        this.smoothTransitions = DrawProperties.isSmoothLinesEnabled() && processDefinition.getDeployment().getLanguage() == Language.BPMN2;
    }

    public void setHighlightedToken(Token highlightedToken) {
        this.highlightedToken = highlightedToken;
    }

    public byte[] createDiagram(Process process, ProcessLogs logs) throws Exception {
        AbstractFigureFactory factory;
        if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
            factory = new BPMNFigureFactory();
        } else {
            factory = new UMLFigureFactory();
        }
        for (Node node : processDefinition.getNodes(false)) {
            AbstractFigure nodeFigure = factory.createFigure(node, DrawProperties.useEdgingOnly());
            allNodeFigures.put(node.getNodeId(), nodeFigure);
        }
        for (Node node : processDefinition.getNodes(false)) {
            String nodeId = node.getNodeId();
            AbstractFigure nodeFigure = allNodeFigures.get(node.getNodeId());
            Preconditions.checkNotNull(nodeFigure, "Node figure not found by id " + nodeId);
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            int leavingTransitionsCount = node.getLeavingTransitions().size();
            if (nodeFigure.isHasTimer()) {
                leavingTransitionsCount--;
            }
            if (node.getNodeType() == NodeType.END_PROCESS) {
                continue;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                TransitionFigureBase transitionFigureBase = factory.createTransitionFigure();
                transitionFigureBase.init(transition, nodeFigure, figureTo, smoothTransitions);
                if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
                    boolean exclusiveNode = node.getNodeType() != NodeType.FORK && node.getNodeType() != NodeType.JOIN
                            && node.getNodeType() != NodeType.PARALLEL_GATEWAY;
                    transitionFigureBase.setExclusive(exclusiveNode && leavingTransitionsCount > 1);
                }
                nodeFigure.addTransition(transitionFigureBase);
                if (!DrawProperties.useEdgingOnly()) {
                    transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        for (TransitionLog transitionLog : logs.getLogs(TransitionLog.class)) {
            Transition transition = transitionLog.getTransitionOrNull(processDefinition);
            if (transition != null) {
                // Mark 'from' block as PASSED
                AbstractFigure nodeModelFrom = allNodeFigures.get(transition.getFrom().getTransitionNodeId(false));
                nodeFigures.put(nodeModelFrom, new RenderHits(DrawProperties.getHighlightColor(), true));
                // Mark 'to' block as PASSED
                AbstractFigure nodeModelTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                nodeFigures.put(nodeModelTo, new RenderHits(DrawProperties.getHighlightColor(), true));
                // Mark transition as PASSED
                TransitionFigureBase transitionFigureBase = nodeModelFrom.getTransition(transition.getName());
                transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getHighlightColor(), true));
            }
        }
        fillActiveSubprocesses(process.getRootToken());
        fillTasks(logs);
        GraphImage graphImage = new GraphImage(processDefinition, transitionFigureBases, nodeFigures);
        return graphImage.getImageBytes();
    }

    private void fillActiveSubprocesses(Token token) {
        for (Token childToken : token.getActiveChildren()) {
            fillActiveSubprocesses(childToken);
        }
        if (processDefinition.getNode(token.getNodeId()) != null && token.getNodeNotNull(processDefinition) instanceof SubprocessNode) {
            AbstractFigure node = allNodeFigures.get(token.getNodeNotNull(processDefinition).getNodeId());
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
            AbstractFigure figure = allNodeFigures.get(entry.getKey().getNodeId());
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
                nodeFigures.put(figure, new RenderHits(color, true, true));
            }
        }
    }
}
