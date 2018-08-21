/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentNodeEnterLog;
import ru.runa.wfe.audit.CurrentNodeLeaveLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ProcessExecutionListener;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.MessageEventType;

public abstract class Node extends GraphElement {
    private static final long serialVersionUID = 1L;
    private Boolean asyncExecution;
    private final List<Transition> leavingTransitions = Lists.newArrayList();
    private final List<Transition> arrivingTransitions = Lists.newArrayList();
    private boolean graphMinimazedView;
    /**
     * Graph constraints on SetMinimized(true) moment call;
     */
    private int[] originalConstraints;

    public abstract NodeType getNodeType();

    @Override
    public void validate() {
        super.validate();
        for (Transition transition : leavingTransitions) {
            transition.validate();
        }
    }

    public Boolean getAsyncExecution() {
        return asyncExecution;
    }

    public void setAsyncExecution(Boolean asyncExecution) {
        this.asyncExecution = asyncExecution;
    }

    public List<Transition> getLeavingTransitions() {
        return leavingTransitions;
    }

    public String getTransitionNodeId(boolean arriving) {
        return getNodeId();
    }

    /**
     * Arriving transitions for node.
     */
    public List<Transition> getArrivingTransitions() {
        return arrivingTransitions;
    }

    /**
     * creates a bidirection relation between this node and the given leaving transition.
     *
     * @throws IllegalArgumentException
     *             if leavingTransition is null.
     */
    public Transition addLeavingTransition(Transition leavingTransition) {
        for (Transition transition : leavingTransitions) {
            if (Objects.equal(transition.getName(), leavingTransition.getName())) {
                throw new InternalApplicationException("Duplicated transition: '" + this.getName() + "/" + leavingTransition.getName() + "'");
            }
        }
        leavingTransitions.add(leavingTransition);
        leavingTransition.setFrom(this);
        return leavingTransition;
    }

    /**
     * checks for the presence of a leaving transition with the given name.
     *
     * @return true if this node has a leaving transition with the given name, false otherwise.
     */
    public boolean hasLeavingTransition(String transitionName) {
        return getLeavingTransition(transitionName) != null;
    }

    /**
     * retrieves a leaving transition by name. note that also the leaving transitions of the supernode are taken into account.
     */
    public Transition getLeavingTransition(String transitionName) {
        Preconditions.checkNotNull(transitionName, "transitionName");
        for (Transition transition : leavingTransitions) {
            if (transitionName.equals(transition.getName())) {
                return transition;
            }
        }
        return null;
    }

    public Transition getLeavingTransitionNotNull(String transitionName) {
        Transition transition = getLeavingTransition(transitionName);
        if (transition == null) {
            throw new InternalApplicationException("leaving transition '" + transitionName + "' does not exist in " + this);
        }
        return transition;
    }

    /**
     * @return the default leaving transition.
     */
    public Transition getDefaultLeavingTransitionNotNull() {
        for (Transition transition : leavingTransitions) {
            if (!transition.isTimerTransition()) {
                return transition;
            }
        }
        if (leavingTransitions.size() > 0) {
            return leavingTransitions.get(0);
        }
        throw new InternalApplicationException("No leaving transitions in " + this);
    }

    /**
     * add a bidirection relation between this node and the given arriving transition.
     */
    public Transition addArrivingTransition(Transition arrivingTransition) {
        arrivingTransitions.add(arrivingTransition);
        arrivingTransition.setTo(this);
        return arrivingTransition;
    }

    public boolean isGraphMinimizedView() {
        return graphMinimazedView;
    }

    public void setGraphMinimizedView(boolean graphMinimazedView) {
        this.graphMinimazedView = graphMinimazedView;
        if (graphMinimazedView) {
            originalConstraints = getGraphConstraints().clone();
            // adjust size
            getGraphConstraints()[2] = 3 * DrawProperties.GRID_SIZE;
            getGraphConstraints()[3] = 3 * DrawProperties.GRID_SIZE;
        } else {
            if (originalConstraints != null) {
                setGraphConstraints(getGraphConstraints()[0], getGraphConstraints()[1], originalConstraints[2], originalConstraints[3]);
            }
        }
    }

    /**
     * called by a transition to pass execution to this node.
     */
    public void enter(ExecutionContext executionContext) {
        log.debug("Entering " + this + " with " + executionContext);
        CurrentToken token = executionContext.getToken();
        // update the runtime context information
        token.setNodeId(getNodeId());
        token.setNodeType(getNodeType());
        // fire the leave-node event for this node
        fireEvent(executionContext, ActionEvent.NODE_ENTER);
        executionContext.addLog(new CurrentNodeEnterLog(this));
        if (this instanceof BoundaryEventContainer && !(this instanceof EmbeddedSubprocessEndNode)) {
            for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) this).getBoundaryEvents()) {
                Node boundaryNode = (Node) boundaryEvent;
                CurrentToken eventToken = new CurrentToken(executionContext.getToken(), boundaryNode.getNodeId());
                eventToken.setNodeId(boundaryNode.getNodeId());
                eventToken.setNodeType(boundaryNode.getNodeType());
                ExecutionContext eventExecutionContext = new ExecutionContext(getProcessDefinition(), eventToken);
                ((Node) boundaryEvent).handle(eventExecutionContext);
            }
        }
        boolean async = getAsyncExecution(executionContext);
        if (async) {
            ApplicationContextFactory.getNodeAsyncExecutor().execute(token, true);
        } else {
            handle(executionContext);
        }
    }

    private boolean getAsyncExecution(ExecutionContext executionContext) {
        if (asyncExecution != null) {
            return asyncExecution;
        }
        if (executionContext.getProcessDefinition().getNodeAsyncExecution() != null) {
            return executionContext.getProcessDefinition().getNodeAsyncExecution();
        }
        return SystemProperties.isProcessExecutionNodeAsyncEnabled(getNodeType());
    }

    public final void handle(ExecutionContext executionContext) {
        try {
            log.debug("Executing " + this + " with " + executionContext);
            executionContext.activateTokenIfHasPreviousError();
            execute(executionContext);
        } catch (Throwable th) {
            log.error("Handling failed in " + this);
            throw Throwables.propagate(th);
        }
    }

    /**
     * override this method to customize the node behavior.
     */
    protected abstract void execute(ExecutionContext executionContext) throws Exception;

    /**
     * called by the implementation of this node to continue execution over the default transition.
     */
    public final void leave(ExecutionContext executionContext) {
        leave(executionContext, null);
    }

    /**
     * called by the implementation of this node to continue execution over the given transition.
     */
    public void leave(ExecutionContext executionContext, Transition transition) {
        log.debug("Leaving " + this + " with " + executionContext);
        if (endBoundaryEventTokensOnNodeLeave()) {
            endBoundaryEventTokens(executionContext);
        }
        if (this instanceof BoundaryEvent && Boolean.TRUE.equals(((BoundaryEvent) this).getBoundaryEventInterrupting())) {
            CurrentToken parentToken = executionContext.getToken().getParent();
            ((Node) getParentElement()).onBoundaryEvent(executionContext.getProcessDefinition(), parentToken, (BoundaryEvent) this);
            for (CurrentToken token : parentToken.getActiveChildren()) {
                if (Objects.equal(token, executionContext.getToken())) {
                    continue;
                }
                token.end(executionContext.getProcessDefinition(), null, ((BoundaryEvent) this).getTaskCompletionInfoIfInterrupting(), true);
            }
        }
        CurrentToken token = executionContext.getToken();
        for (ProcessExecutionListener listener : SystemProperties.getProcessExecutionListeners()) {
            listener.onNodeLeave(executionContext, this, transition);
        }
        // fire the leave-node event for this node
        fireEvent(executionContext, ActionEvent.NODE_LEAVE);
        addLeaveLog(executionContext);
        if (transition == null) {
            transition = getDefaultLeavingTransitionNotNull();
        }
        token.setNodeId(null);
        token.setNodeType(null);
        // take the transition
        transition.take(executionContext);
    }

    protected void addLeaveLog(ExecutionContext executionContext) {
        executionContext.addLog(new CurrentNodeLeaveLog(this));
    }

    @Override
    public Node clone() throws CloneNotSupportedException {
        Node clone = (Node) super.clone();
        if (originalConstraints != null) {
            clone.originalConstraints = originalConstraints.clone();
        }
        return clone;
    }

    public boolean hasErrorEventHandler() {
        if (this instanceof BoundaryEventContainer && !(this instanceof EmbeddedSubprocessStartNode)) {
            for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) this).getBoundaryEvents()) {
                if (boundaryEvent instanceof CatchEventNode && ((CatchEventNode) boundaryEvent).getEventType() == MessageEventType.error) {
                    return true;
                }
            }
        }
        return false;
    }

    public void endBoundaryEventTokens(ExecutionContext executionContext) {
        if (this instanceof BoundaryEventContainer && !(this instanceof EmbeddedSubprocessStartNode)) {
            List<BoundaryEvent> boundaryEvents = ((BoundaryEventContainer) this).getBoundaryEvents();
            for (CurrentToken token : executionContext.getToken().getActiveChildren()) {
                Node node = token.getNodeNotNull(executionContext.getProcessDefinition());
                if (boundaryEvents.contains(node)) {
                    token.end(executionContext.getProcessDefinition(), null, null, false);
                }
            }
        }
    }

    protected boolean endBoundaryEventTokensOnNodeLeave() {
        return true;
    }

    protected void onBoundaryEvent(ProcessDefinition processDefinition, CurrentToken token, BoundaryEvent boundaryEvent) {
        token.end(processDefinition, null, boundaryEvent.getTaskCompletionInfoIfInterrupting(), false);
    }
}