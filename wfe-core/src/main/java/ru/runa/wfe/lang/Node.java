package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentNodeEnterLog;
import ru.runa.wfe.audit.CurrentNodeLeaveLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.execution.logic.ProcessExecutionListener;
import ru.runa.wfe.execution.logic.TokenNodeNameExtractor;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.task.TaskCompletionInfo;

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
    @Autowired
    protected transient TokenNodeNameExtractor tokenNodeNameExtractor;

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
        if (executionContext.getCurrentToken().hasEnded()) {
            throw new IllegalStateException("Execution in ended " + executionContext.getCurrentToken() + " does not allowed");
        }
        log.debug("Entering " + this + " with " + executionContext);
        CurrentToken token = executionContext.getCurrentToken();
        // update the runtime context information
        token.setNodeId(getNodeId());
        token.setNodeType(getNodeType());
        token.setNodeName(tokenNodeNameExtractor.extract(this));
        token.setNodeEnterDate(new Date());
        // fire the leave-node event for this node
        fireEvent(executionContext, ActionEvent.NODE_ENTER);
        executionContext.addLog(new CurrentNodeEnterLog(this));
        if (this instanceof BoundaryEventContainer && !(this instanceof EmbeddedSubprocessEndNode)) {
            for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) this).getBoundaryEvents()) {
                Node boundaryNode = (Node) boundaryEvent;
                CurrentToken eventToken = new CurrentToken(executionContext.getCurrentToken(), boundaryNode.getNodeId());
                eventToken.setNodeId(boundaryNode.getNodeId());
                eventToken.setNodeType(boundaryNode.getNodeType());
                eventToken.setNodeName(tokenNodeNameExtractor.extract(boundaryNode));
                eventToken.setNodeEnterDate(new Date());
                ApplicationContextFactory.getCurrentTokenDao().create(eventToken);
                ExecutionContext eventExecutionContext = new ExecutionContext(executionContext.getParsedProcessDefinition(), eventToken);
                eventExecutionContext.addLog(new CurrentNodeEnterLog((Node) boundaryEvent));
                ((Node) boundaryEvent).handle(eventExecutionContext);
            }
            if (executionContext.getCurrentToken().hasEnded()) {
                log.debug("Execution has been interrupted by boundary event");
                return;
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
        if (executionContext.getParsedProcessDefinition().getNodeAsyncExecution() != null) {
            return executionContext.getParsedProcessDefinition().getNodeAsyncExecution();
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
     * override this method to customize the node behavior.
     */
    public void cancel(ExecutionContext executionContext) {
        List<CurrentToken> activeTokens = executionContext.getCurrentToken().getActiveChildren(true);
        for (CurrentToken token : activeTokens) {
            ApplicationContextFactory.getExecutionLogic().endToken(token, executionContext.getParsedProcessDefinition(), null,
                    TaskCompletionInfo.createForHandler("cancel"), false);
        }
    }

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
            CurrentToken parentToken = executionContext.getCurrentToken().getParent();
            ((Node) getParentElement()).onBoundaryEvent(new ExecutionContext(executionContext.getParsedProcessDefinition(), parentToken),
                    (BoundaryEvent) this);
            for (CurrentToken token : parentToken.getChildren()) {
                if (Objects.equal(token, executionContext.getToken())) {
                    continue;
                }
                if (token.hasEnded()) {
                    // inactive ParallelGateway behaviour
                    // https://redmine.mikhe.ru/issues/6254#note-28
                    token.setAbleToReactivateParent(false);
                }
                ApplicationContextFactory.getExecutionLogic().endToken(token, executionContext.getParsedProcessDefinition(), null,
                        ((BoundaryEvent) this).getTaskCompletionInfoIfInterrupting(executionContext), true);
            }
        }
        CurrentToken token = executionContext.getCurrentToken();
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
        token.setNodeName(null);
        token.setNodeEnterDate(null);
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
            ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();
            List<BoundaryEvent> boundaryEvents = ((BoundaryEventContainer) this).getBoundaryEvents();
            List<CurrentToken> activeTokens = executionContext.getCurrentToken().getActiveChildren(false);
            log.debug("Ending boundary event tokens " + activeTokens + " for " + boundaryEvents);
            for (CurrentToken token : executionContext.getCurrentToken().getActiveChildren(false)) {
                Node node = token.getNodeNotNull(executionContext.getParsedProcessDefinition());
                if (boundaryEvents.contains(node)) {
                    executionLogic.endToken(token, executionContext.getParsedProcessDefinition(), null, null, false);
                }
            }
        }
    }

    protected boolean endBoundaryEventTokensOnNodeLeave() {
        return true;
    }

    protected void onBoundaryEvent(ExecutionContext executionContext, BoundaryEvent boundaryEvent) {
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null,
                boundaryEvent.getTaskCompletionInfoIfInterrupting(executionContext), false);
    }

}