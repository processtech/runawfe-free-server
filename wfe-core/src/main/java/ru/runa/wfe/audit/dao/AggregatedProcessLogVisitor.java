package ru.runa.wfe.audit.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.CreateTimerLog;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndByAdminLog;
import ru.runa.wfe.audit.TaskEndBySubstitutorLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.aggregated.ProcessAggregatedLog;
import ru.runa.wfe.audit.aggregated.QProcessAggregatedLog;
import ru.runa.wfe.audit.aggregated.QSignalListenerAggregatedLog;
import ru.runa.wfe.audit.aggregated.QTaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.QTimerAggregatedLog;
import ru.runa.wfe.audit.aggregated.SignalListenerAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog.EndReason;
import ru.runa.wfe.audit.aggregated.TimerAggregatedLog;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.lang.BaseReceiveMessageNode;
import ru.runa.wfe.lang.NodeType;

@Component
public class AggregatedProcessLogVisitor implements ProcessLogVisitor {

    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private HibernateQueryFactory queryFactory;

    @Override
    public void onProcessStartLog(ProcessStartLog processStartLog) {
        if (getProcessInstanceLog(processStartLog.getProcessId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new ProcessAggregatedLog(processStartLog));
    }

    @Override
    public void onProcessEndLog(ProcessEndLog processEndLog) {
        ProcessAggregatedLog logEntry = getProcessInstanceLog(processEndLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processEndLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onProcessCancelLog(ProcessCancelLog processCancelLog) {
        ProcessAggregatedLog logEntry = getProcessInstanceLog(processCancelLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processCancelLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onNodeEnterLog(NodeEnterLog nodeEnterLog) {
        if (nodeEnterLog.getNode() instanceof BaseReceiveMessageNode) {
            sessionFactory.getCurrentSession().save(
                    new SignalListenerAggregatedLog(nodeEnterLog, ((BaseReceiveMessageNode) nodeEnterLog.getNode()).getEventType()));
        }
    }

    @Override
    public void onNodeLeaveLog(NodeLeaveLog nodeLeaveLog) {
        if (nodeLeaveLog.getNodeType() == NodeType.TIMER) {
            QTimerAggregatedLog l = QTimerAggregatedLog.timerAggregatedLog;
            TimerAggregatedLog logEntry = queryFactory.selectFrom(l)
                    .where(l.processId.eq(nodeLeaveLog.getProcessId()).and(l.nodeId.eq(nodeLeaveLog.getNodeId()))).orderBy(l.id.desc()).fetchFirst();
            if (logEntry == null) {
                return;
            }
            logEntry.setEndDate(nodeLeaveLog.getCreateDate());
            sessionFactory.getCurrentSession().merge(logEntry);
        }
    }

    @Override
    public void onReceiveMessageLog(ReceiveMessageLog receiveMessageLog) {
        QSignalListenerAggregatedLog l = QSignalListenerAggregatedLog.signalListenerAggregatedLog;
        SignalListenerAggregatedLog logEntry = queryFactory.selectFrom(l)
                .where(l.processId.eq(receiveMessageLog.getProcessId()).and(l.nodeId.eq(receiveMessageLog.getNodeId()))).orderBy(l.id.desc())
                .fetchFirst();
        if (logEntry == null) {
            return;
        }
        logEntry.setExecuteDate(receiveMessageLog.getCreateDate());
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onCreateTimerLog(CreateTimerLog createTimerLog) {
        sessionFactory.getCurrentSession().save(new TimerAggregatedLog(createTimerLog));
    }

    @Override
    public void onTaskCreateLog(TaskCreateLog taskCreateLog) {
        if (getTaskLog(taskCreateLog.getTaskId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new TaskAggregatedLog(taskCreateLog));
    }

    @Override
    public void onTaskAssignLog(TaskAssignLog taskAssignLog) {
        TaskAggregatedLog logEntry = getTaskLog(taskAssignLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateAssignment(taskAssignLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onTaskEndLog(TaskEndLog taskEndLog) {
        onTaskEnd(taskEndLog, EndReason.COMPLETED);
    }

    @Override
    public void onTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog) {
        onTaskEnd(taskEndBySubstitutorLog, EndReason.SUBSTITUTOR_END);
    }

    @Override
    public void onTaskEndByAdminLog(TaskEndByAdminLog taskEndByAdminLog) {
        onTaskEnd(taskEndByAdminLog, EndReason.ADMIN_END);
    }

    @Override
    public void onTaskCancelledLog(TaskCancelledLog taskCancelledLog) {
        onTaskEnd(taskCancelledLog, taskCancelledLog.getActorName() == null ? EndReason.CANCELLED : EndReason.COMPLETED);
    }

    private ProcessAggregatedLog getProcessInstanceLog(Long processId) {
        QProcessAggregatedLog l = QProcessAggregatedLog.processAggregatedLog;
        return queryFactory.selectFrom(l).where(l.processInstanceId.eq(processId)).fetchFirst();
    }

    private TaskAggregatedLog getTaskLog(Long taskId) {
        QTaskAggregatedLog l = QTaskAggregatedLog.taskAggregatedLog;
        return queryFactory.selectFrom(l).where(l.taskId.eq(taskId)).fetchFirst();
    }

    private void onTaskEnd(TaskEndLog taskEndLog, EndReason endReason) {
        TaskAggregatedLog logEntry = getTaskLog(taskEndLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateOnEnd(taskEndLog.getCreateDate(), taskEndLog.getActorName(), endReason, taskEndLog.getTransitionName());
        sessionFactory.getCurrentSession().merge(logEntry);
    }
}
