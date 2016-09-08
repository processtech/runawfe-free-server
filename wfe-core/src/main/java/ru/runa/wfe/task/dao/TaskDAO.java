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
package ru.runa.wfe.task.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Executor;

@SuppressWarnings("unchecked")
public class TaskDAO extends GenericDAO<Task> {

    @Override
    protected void checkNotNull(Task entity, Object identity) {
        if (entity == null) {
            throw new TaskDoesNotExistException(identity);
        }
    }

    /**
     * @return active tasks assigned to a given executor.
     */
    public List<Task> findTasks(Executor executor) {
        return getHibernateTemplate().find("from Task where executor=?", executor);
    }

    public List<Task> findTasksByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        return getHibernateTemplate().find("from Task where process=? and deadlineDateExpression like ?", process, "%" + expression + "%");
    }

    /**
     * @return active tasks but not assigned.
     */
    public List<Task> findUnassignedTasksInActiveProcesses() {
        return getHibernateTemplate().find("from Task where executor is null and token.executionStatus=?", ExecutionStatus.ACTIVE);
    }

}
