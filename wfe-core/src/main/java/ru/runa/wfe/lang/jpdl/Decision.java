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
package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.DecisionHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * decision node.
 */
public class Decision extends Node {
    private static final long serialVersionUID = 1L;

    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.DECISION;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
        delegation.validate();
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            DecisionHandler decisionHandler = delegation.getInstance();
            String transitionName = decisionHandler.decide(executionContext);
            Preconditions.checkNotNull(transitionName, "Null transition name by condition");
            Transition transition = getLeavingTransitionNotNull(transitionName);
            log.debug("decision " + name + " is taking '" + transition + "'");
            leave(executionContext, transition);
        } catch (Exception exception) {
            throw Throwables.propagate(exception);
        }
    }

}
