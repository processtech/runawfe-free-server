package ru.runa.wfe.audit.dao;

import com.google.common.base.Strings;
import com.querydsl.jpa.JPAExpressions;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.val;
import org.hibernate.SessionFactory;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndByAdminLog;
import ru.runa.wfe.audit.TaskEndBySubstitutorLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskExpiredLog;
import ru.runa.wfe.audit.TaskRemovedOnProcessEndLog;
import ru.runa.wfe.audit.aggregated.QTaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.QTaskAssignmentAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAssignmentAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskEndReason;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

public class UpdateAggregatedLogVisitor extends ProcessLogVisitor {

    private final SessionFactory sessionFactory;
    private final HibernateQueryFactory queryFactory;
    private final CurrentProcess process;
    private final ProcessDefinitionLoader processDefinitionLoader;

    public UpdateAggregatedLogVisitor(
            SessionFactory sessionFactory,
            HibernateQueryFactory queryFactory,
            ProcessDefinitionLoader processDefinitionLoader,
            CurrentProcess process
    ) {
        this.sessionFactory = sessionFactory;
        this.queryFactory = queryFactory;
        this.processDefinitionLoader = processDefinitionLoader;
        this.process = process;
    }

    @Override
    public void onTaskCreateLog(TaskCreateLog l) {
        if (getTaskLog(l.getTaskId()) != null) {
            return;
        }

        String swimlaneName = null;
        Node node = processDefinitionLoader.getDefinition(process).getNode(l.getNodeId());
        if (node instanceof InteractionNode) {
            List<TaskDefinition> tasks = ((InteractionNode) node).getTasks();
            if (tasks != null && !tasks.isEmpty() && tasks.get(0).getSwimlane() != null) {
                swimlaneName = tasks.get(0).getSwimlane().getName();
            }
        }

        TaskAggregatedLog tal = new TaskAggregatedLog();
        tal.setTaskId(l.getTaskId());
        tal.setProcessId(l.getProcessId());
        tal.setCreateDate(l.getCreateDate());
        tal.setDeadlineDate(l.getDeadlineDate());
        tal.setTokenId(l.getTokenId());
        tal.setNodeId(l.getNodeId());
        tal.setTaskName(l.getTaskName());
        tal.setTaskIndex(l.getTaskIndex());
        tal.setSwimlaneName(swimlaneName);
        tal.setEndReason(TaskEndReason.PROCESSING);
        sessionFactory.getCurrentSession().save(tal);
    }

    @Override
    public void onTaskAssignLog(TaskAssignLog taskAssignLog) {
        TaskAggregatedLog l = getTaskLog(taskAssignLog.getTaskId());
        if (l == null) {
            return;
        }

        saveAssignment(l, taskAssignLog.getCreateDate(), taskAssignLog.getNewExecutorName());
        if (Strings.isNullOrEmpty(l.getCompleteActorName())) {
            l.setInitialActorName(taskAssignLog.getNewExecutorName());
        }

        sessionFactory.getCurrentSession().merge(l);
    }

    @Override
    public void onTaskEndLog(TaskEndLog taskEndLog) {
        onTaskEnd(taskEndLog, TaskEndReason.COMPLETED);
    }

    @Override
    public void onTaskRemovedOnProcessEndLog(TaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog) {
        onTaskEnd(taskRemovedOnProcessEndLog, TaskEndReason.PROCESS_END);
    }

    @Override
    public void onTaskExpiredLog(TaskExpiredLog taskExpiredLog) {
        onTaskEnd(taskExpiredLog, TaskEndReason.TIMEOUT);
    }

    @Override
    public void onTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog) {
        onTaskEnd(taskEndBySubstitutorLog, TaskEndReason.SUBSTITUTOR_END);
    }

    @Override
    public void onTaskEndByAdminLog(TaskEndByAdminLog taskEndByAdminLog) {
        onTaskEnd(taskEndByAdminLog, TaskEndReason.ADMIN_END);
    }

    @Override
    public void onTaskCancelledLog(TaskCancelledLog taskCancelledLog) {
        onTaskEnd(taskCancelledLog, taskCancelledLog.getActorName() == null ? TaskEndReason.CANCELLED : TaskEndReason.COMPLETED);
    }

    private TaskAggregatedLog getTaskLog(long taskId) {
        val l = QTaskAggregatedLog.taskAggregatedLog;
        return queryFactory.selectFrom(l).where(l.taskId.eq(taskId)).fetchFirst();
    }

    private void onTaskEnd(TaskEndLog taskEndLog, TaskEndReason endReason) {
        TaskAggregatedLog l = getTaskLog(taskEndLog.getTaskId());
        if (l == null) {
            return;
        }

        saveAssignment(l, taskEndLog.getCreateDate(), taskEndLog.getActorName());
        l.setEndDate(taskEndLog.getCreateDate());
        l.setCompleteActorName(taskEndLog.getActorName());
        l.setEndReason(endReason);

        sessionFactory.getCurrentSession().merge(l);
    }

    private void saveAssignment(TaskAggregatedLog tal, Date assignmentDate, String newExecutorName) {
        // Insert new record if last executor name is different from newExecutorName,
        // and if same record does not already exists (this check is for import operation: assignment may already be saved before import).
        // Instead of loading all detail rows via Hibernate collections, here I check both conditions using single optimized SQL query.
        val l = new QTaskAssignmentAggregatedLog("l");
        val l2 = new QTaskAssignmentAggregatedLog("l2");
        List<String> rows = queryFactory.select(l.newExecutorName)
                .from(l)
                .where(l.id.eq(JPAExpressions.select(l2.id.max()).from(l2).where(l2.log.eq(tal))).or(
                        l.assignDate.eq(assignmentDate).and(l.newExecutorName.eq(newExecutorName))
                ))
                .orderBy(l.id.desc())
                .fetch();

        // If rows.size() == 0, no detail rows exist for given TaskAggregatedLog ==> insert.
        // If rows.size() >  1, we got both last AND existing row ==> DON'T insert.
        // If rows.size() == 1, have to check returned newExecutorName.
        val oldExecutorName = rows.size() == 1 ? rows.get(0) : null;
        if (rows.isEmpty() || rows.size() == 1 && !Objects.equals(oldExecutorName, newExecutorName)) {
            val taal = new TaskAssignmentAggregatedLog();
            taal.setLog(tal);
            //noinspection ConstantConditions
            taal.setAssignDate(assignmentDate);
            taal.setOldExecutorName(oldExecutorName);
            taal.setNewExecutorName(newExecutorName);
            sessionFactory.getCurrentSession().save(taal);
        }
    }
}
