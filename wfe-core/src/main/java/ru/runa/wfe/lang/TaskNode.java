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

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.CurrentSwimlane;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code> specifies how task completion triggers continuation of execution.
 */
public class TaskNode extends BaseTaskNode {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.TASK_STATE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        for (TaskDefinition taskDefinition : taskDefinitions) {
            CurrentSwimlane swimlane = getInitializedSwimlaneNotNull(executionContext, taskDefinition);
            // copy the swimlane assignment into the task
            taskFactory.create(executionContext, taskDefinition, swimlane, swimlane.getExecutor(), null);
        }
        if (async) {
            log.debug("continue execution in async " + this);
            leave(executionContext);
        }
    }

}
