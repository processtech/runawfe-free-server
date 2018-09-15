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
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * Logging task assignment.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "2")
public class CurrentTaskAssignLog extends CurrentTaskLog implements TaskAssignLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskAssignLog() {
    }

    public CurrentTaskAssignLog(Task task, Executor newExecutor) {
        super(task);
        if (task.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, task.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_ASSIGN;
    }

    @Override
    @Transient
    public String getOldExecutorName() {
        return getAttribute(ATTR_OLD_VALUE);
    }

    @Override
    @Transient
    public String getNewExecutorName() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskAssignLog(this);
    }
}
