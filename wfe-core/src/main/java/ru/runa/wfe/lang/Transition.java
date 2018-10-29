package ru.runa.wfe.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.audit.CurrentTransitionLog;
import ru.runa.wfe.execution.ExecutionContext;

public class Transition extends GraphElement {
    private static final long serialVersionUID = 1L;

    private Node from;
    private Node to;
    // used only in jpdl
    private boolean timerTransition;
    private List<Bendpoint> bendpoints = Lists.newArrayList();
    private String color;

    public String getNodeIdBackCompatibilityPre4_3_0() {
        return from.getNodeId() + "/" + name;
    }

    @Override
    public GraphElement getParentElement() {
        return from;
    }

    @Deprecated
    public GraphElement getParent() {
        return getParentElement();
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(from, "from in " + this);
        Preconditions.checkNotNull(to, "to in " + this);
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public Node getTo() {
        return to;
    }

    public List<Bendpoint> getBendpoints() {
        return bendpoints;
    }

    public boolean isTimerTransition() {
        return timerTransition;
    }

    public void setTimerTransition(boolean timerTransition) {
        this.timerTransition = timerTransition;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Passes execution over this transition.
     */
    public void take(ExecutionContext executionContext) {
        // update the runtime context information
        executionContext.getCurrentToken().setTransitionId(getNodeId());
        executionContext.addLog(new CurrentTransitionLog(this));
        // fire the transition event (if any)
        fireEvent(executionContext, ActionEvent.TRANSITION);
        // pass the token to the destinationNode node
        to.enter(executionContext);
    }

    @Override
    public Transition clone() throws CloneNotSupportedException {
        Transition clone = (Transition) super.clone();
        clone.bendpoints = new ArrayList<>(bendpoints);
        return clone;
    }
}
