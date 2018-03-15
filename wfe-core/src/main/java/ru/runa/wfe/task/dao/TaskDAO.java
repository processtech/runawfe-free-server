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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
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

    public List<Task> findByExecutor(Executor executor) {
        return getHibernateTemplate().find("from Task where executor=?", executor);
    }

    public List<Task> findByProcess(Process process) {
        return getHibernateTemplate().find("from Task where process=?", process);
    }

    public List<Task> findByProcessAndSwimlane(Process process, Swimlane swimlane) {
        return getHibernateTemplate().find("from Task where process=? and swimlane=?", process, swimlane);
    }

    public List<Task> findByProcessAndNodeId(Process process, String nodeId) {
        return getHibernateTemplate().find("from Task where process=? and nodeId=?", process, nodeId);
    }

    public List<Task> findByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        return getHibernateTemplate().find("from Task where process=? and deadlineDateExpression like ?", process, "%" + expression + "%");
    }

    public List<Task> findByToken(Token token) {
        return getHibernateTemplate().find("from Task where token=?", token);
    }

    /**
     * @return active tasks but not assigned.
     */
    public List<Task> findUnassignedTasksInActiveProcesses() {
        return getHibernateTemplate().find("from Task where executor is null and token.executionStatus != ?", ExecutionStatus.SUSPENDED);
    }

    public List<Task> findUnassignedTasks() {
        return getHibernateTemplate().find("from Task where executor is null");
    }

    /**
     * @return tasks, opened by user.
     */
    public List<Long> getOpenedTasks(Long actorId, List<Long> tasksIds) {
        if (tasksIds.isEmpty()) {
            return new ArrayList<Long>();
        }
        return getHibernateTemplate().findByNamedParam("select id from Task where :actorId in elements(openedByExecutorIds) and id in (:tasksIds)",
                new String[] { "actorId", "tasksIds" }, new Object[] { actorId, tasksIds });
    }

    /**
     * @return return all expired tasks.
     */
    public List<Task> getAllExpiredTasks(Date curDate) {
        return getHibernateTemplate().find("from Task where deadlineDate < ?", curDate);
    }

    public void deleteAll(Process process) {
        log.debug("deleting tasks for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Task where process=?", process);
    }
}
