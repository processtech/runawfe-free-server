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

import java.io.Serializable;

import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Executor;

/**
 * common superclass for {@link ru.runa.wfe.task.Task}s and {@link CurrentSwimlane}s used by the
 * {@link ru.runa.wfe.extension.AssignmentHandler} interface.
 */
public interface Assignable extends Serializable {

    String getName();

    String getSwimlaneName();

    /**
     * sets the responsible for this assignable object. Use this method to assign the task into a user's personal task list.
     * 
     * @param cascadeUpdate
     *            for task: update swimlane; for swimlane: update tasks
     */
    void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate);

    /**
     * @return currently assigned executor
     */
    Executor getExecutor();
}
