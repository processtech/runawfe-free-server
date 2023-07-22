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
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.QTask;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Executor;

@Component
@SuppressWarnings("unchecked")
public class TaskDao extends GenericDao<Task> {

    @Override
    protected void checkNotNull(Task entity, Object identity) {
        if (entity == null) {
            throw new TaskDoesNotExistException(identity);
        }
    }

    public List<Task> findByExecutor(Executor executor) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.executor.eq(executor)).fetch();
    }

    public List<Task> findByProcess(Process process) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process)).fetch();
    }

    public List<Task> findByProcessAndSwimlane(Process process, Swimlane swimlane) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.swimlane.eq(swimlane))).fetch();
    }

    public List<Task> findByProcessAndNodeId(Process process, String nodeId) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.nodeId.eq(nodeId))).fetch();
    }

    public List<Task> findByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.deadlineDateExpression.like("%" + expression + "%"))).fetch();
    }

    public List<Task> findByToken(Token token) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.token.eq(token)).fetch();
    }

    /**
     * @return active tasks but not assigned.
     */
    public List<Task> findUnassignedTasksInActiveProcesses() {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t)
                .where(t.executor.isNull().and(t.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.token.endDate.isNull())).fetch();
    }

    public List<Task> findUnassignedTasks() {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.executor.isNull()).fetch();
    }

    /**
     * @return tasks, opened by user.
     */
    public List<Long> getOpenedTasks(Long actorId, List<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sessionFactory.getCurrentSession().createQuery("select id from Task where :actorId in elements(openedByExecutorIds) and id in (:taskIds)")
                .setParameter("actorId", actorId)
                .setParameterList("taskIds", taskIds)
                .list();
    }

    /**
     * @return return all expired tasks
     */
    public List<Task> getAllExpiredTasks(Date curDate) {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.deadlineDate.lt(curDate)).fetch();
    }

    public void deleteAll(Process process) {
        log.debug("deleting tasks for process " + process.getId());
        List<Task> tasks = findByProcess(process);
        for (Task task : tasks) {
            task.delete();
        }
        flushPendingChanges();
    }

    /**
     * @return return all async tasks with ended parent process
     */
    public List<Task> findByEndedProcess() {
        QTask t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.endDate.isNotNull()).fetch();
    }
}
