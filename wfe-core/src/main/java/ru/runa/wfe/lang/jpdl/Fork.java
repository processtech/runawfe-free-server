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

import java.util.Map;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Maps;

/**
 * Launch child tokens from the fork over the leaving transitions.
 */
public class Fork extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.FORK;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        checkCyclicExecution(token);
        Map<Token, Transition> childTokens = Maps.newHashMap();
        for (Transition leavingTransition : getLeavingTransitions()) {
            Token childToken = new Token(token, getNodeId() + "/" + leavingTransition.getNodeId());
            childTokens.put(childToken, leavingTransition);
        }
        ApplicationContextFactory.getTokenDAO().flushPendingChanges();
        log.debug("Child tokens created: " + childTokens.keySet());
        for (Map.Entry<Token, Transition> entry : childTokens.entrySet()) {
            ExecutionContext childExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), entry.getKey());
            leave(childExecutionContext, entry.getValue());
        }
    }

    private void checkCyclicExecution(Token token) {
        if (token.getDepth() > SystemProperties.getTokenMaximumDepth()) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }

}
