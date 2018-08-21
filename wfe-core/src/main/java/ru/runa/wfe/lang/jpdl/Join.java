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

import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

import com.google.common.base.Objects;

public class Join extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.JOIN;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        CurrentToken token = executionContext.getToken();
        token.end(executionContext.getProcessDefinition(), null, null, false);
        if (token.isAbleToReactivateParent()) {
            token.setAbleToReactivateParent(false);
            CurrentToken parentToken = token.getParent();
            boolean reactivateParent = true;
            for (CurrentToken childToken : parentToken.getActiveChildren()) {
                if (childToken.isAbleToReactivateParent()) {
                    reactivateParent = false;
                    log.debug("There are exists at least 1 active token that can reactivate parent: " + childToken);
                    break;
                }
                if (!Objects.equal(childToken.getNodeId(), getNodeId())) {
                    reactivateParent = false;
                    log.debug(childToken + " is in state (" + childToken.getNodeId() + ") instead of this join (" + getNodeId() + ")");
                    break;
                }
            }
            if (reactivateParent) {
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            }
        } else {
            log.debug(token + " unable to activate the parent");
        }
    }

}
