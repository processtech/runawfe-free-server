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
package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

/**
 * Logging transition passing.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "T")
public class TransitionLog extends ProcessLog {
    private static final long serialVersionUID = 1L;

    public TransitionLog() {
    }

    public TransitionLog(Transition transition) {
        setNodeId(transition.getNodeId());
        addAttribute(ATTR_TRANSITION_ID, transition.getName());
        addAttribute(ATTR_NODE_ID_FROM, transition.getFrom().getTransitionNodeId(false));
        addAttribute(ATTR_NODE_ID_TO, transition.getTo().getTransitionNodeId(true));
    }

    @Transient
    public String getFromNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_FROM);
    }

    @Transient
    public String getToNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_TO);
    }

    @Transient
    public String getTransitionId() {
        return getAttributeNotNull(ATTR_TRANSITION_ID);
    }

    public Transition getTransitionOrNull(ProcessDefinition processDefinition) {
        // due to process definition version update it can be null
        Node node = processDefinition.getNode(getFromNodeId());
        if (node == null) {
            return null;
        }
        return node.getLeavingTransition(getTransitionId());
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_TRANSITION_ID) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTransitionLog(this);
    }
}
