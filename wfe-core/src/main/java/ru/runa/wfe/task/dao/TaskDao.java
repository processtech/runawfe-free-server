package ru.runa.wfe.task.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.task.QTask;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Executor;

@Component
@SuppressWarnings("unchecked")
public class TaskDao extends GenericDao<Task> {

    public TaskDao() {
        super(Task.class);
    }

    @Override
    protected void checkNotNull(Task entity, Object identity) {
        if (entity == null) {
            throw new TaskDoesNotExistException(identity);
        }
    }

    public List<Task> findByExecutor(Executor executor) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.executor.eq(executor)).fetch();
    }

    public List<Task> findByProcess(CurrentProcess process) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process)).fetch();
    }

    public List<Task> findByProcessAndSwimlane(CurrentProcess process, CurrentSwimlane swimlane) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.swimlane.eq(swimlane))).fetch();
    }

    public List<Task> findByProcessAndNodeId(CurrentProcess process, String nodeId) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.nodeId.eq(nodeId))).fetch();
    }

    public List<Task> findByProcessAndDeadlineExpressionContaining(CurrentProcess process, String expression) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.deadlineDateExpression.like("%" + expression + "%"))).fetch();
    }

    public List<Task> findByToken(CurrentToken token) {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.token.eq(token)).fetch();
    }

    /**
     * @return active tasks but not assigned.
     */
    public List<Task> findUnassignedTasksInActiveProcesses() {
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.executor.isNull().and(t.token.executionStatus.ne(ExecutionStatus.SUSPENDED))).fetch();
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
        val t = QTask.task;
        return queryFactory.selectFrom(t).where(t.deadlineDate.lt(curDate)).fetch();
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting tasks for process " + process.getId());
        List<Task> tasks = findByProcess(process);
        for (Task task : tasks) {
            task.delete();
        }
        flushPendingChanges();
    }
}
