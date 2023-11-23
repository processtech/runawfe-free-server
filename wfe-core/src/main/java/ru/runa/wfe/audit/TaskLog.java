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
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.task.Task;

/**
 * Base class for logging task execution.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "0")
public abstract class TaskLog extends ProcessLog {
    private static final long serialVersionUID = 1L;

    public TaskLog() {
    }

    public TaskLog(Task task) {
        setNodeId(task.getNodeId());
        addAttribute(ATTR_TASK_ID, task.getId().toString());
        addAttribute(ATTR_TASK_NAME, task.getName());
        if (task.getIndex() != null) {
            addAttribute(ATTR_INDEX, task.getIndex().toString());
        }
        addAttribute(ATTR_SWIMLANE_NAME, task.getSwimlaneName());
        setSeverity(Severity.INFO);
    }

    public TaskLog(Process process, StartNode startNode) {
        setNodeId(startNode.getNodeId());
        addAttribute(ATTR_TASK_ID, String.valueOf(-1 * process.getId()));
        addAttribute(ATTR_TASK_NAME, startNode.getName());
        addAttribute(ATTR_SWIMLANE_NAME, startNode.getFirstTaskNotNull().getSwimlane().getName());
        setSeverity(Severity.INFO);
    }

    @Transient
    public Long getTaskId() {
        String taskIdString = getAttribute(ATTR_TASK_ID);
        if (taskIdString != null) {
            return Long.parseLong(taskIdString);
        }
        return null;
    }

    @Transient
    public String getTaskName() {
        return getAttributeNotNull(ATTR_TASK_NAME);
    }

    @Transient
    public Integer getTaskIndex() {
        String taskIndexString = getAttribute(ATTR_INDEX);
        if (taskIndexString != null) {
            return Integer.valueOf(taskIndexString);
        }
        return null;
    }

    @Transient
    public String getSwimlaneName() {
        return getAttribute(ATTR_SWIMLANE_NAME);
    }

}
