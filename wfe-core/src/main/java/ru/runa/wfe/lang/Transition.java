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

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.execution.ExecutionContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Transition extends GraphElement {
    private static final long serialVersionUID = 1L;

    private Node from;
    private Node to;
    private boolean timerTransition;
    private List<Bendpoint> bendpoints = Lists.newArrayList();

    public String getNodeIdBackCompatibilityPre4_3_0() {
        return from.getNodeId() + "/" + name;
    }

    @Override
    public GraphElement getParent() {
        return from;
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

    /**
     * Passes execution over this transition.
     */
    public void take(ExecutionContext executionContext) {
        // update the runtime context information
        executionContext.getToken().setTransitionId(getNodeId());
        executionContext.addLog(new TransitionLog(this));
        // fire the transition event (if any)
        fireEvent(executionContext, Event.TRANSITION);
        // pass the token to the destinationNode node
        to.enter(executionContext);
    }

    @Override
    public Transition clone() throws CloneNotSupportedException {
        Transition clone = (Transition) super.clone();
        clone.bendpoints = new ArrayList<Bendpoint>(bendpoints);
        return clone;
    }
}
