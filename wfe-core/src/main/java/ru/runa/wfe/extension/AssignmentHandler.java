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
package ru.runa.wfe.extension;

import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Executor;

/**
 * assigns {@link ru.runa.wfe.task.Task}s or {@link CurrentSwimlane}s to actors.
 */
public interface AssignmentHandler extends Configurable {

    /**
     * assigns the assignable (={@link ru.runa.wfe.task.Task} or a {@link CurrentSwimlane} to an swimlaneActorId.
     * <p>
     * The swimlaneActorId is the user that is responsible for the given task or swimlane.
     * The pooledActors represents a pool of actors to which the task or swimlane is offered.
     * Any actors from the pool can then take a Task by calling {@link ru.runa.wfe.task.Task#setExecutor(Executor)}.
     */
    void assign(ExecutionContext executionContext, Assignable assignable);
}
