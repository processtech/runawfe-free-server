package ru.runa.wfe.audit.dao;

import org.hibernate.SessionFactory;
import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.CreateTimerLog;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessActivateLog;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.ProcessSuspendLog;
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.audit.SendMessageLog;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.SwimlaneAssignLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskDelegationLog;
import ru.runa.wfe.audit.TaskEndByAdminLog;
import ru.runa.wfe.audit.TaskEndBySubstitutorLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskEscalationLog;
import ru.runa.wfe.audit.TaskExpiredLog;
import ru.runa.wfe.audit.TaskRemovedOnProcessEndLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableUpdateLog;
import ru.runa.wfe.audit.aggregated.ProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.QProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.QTaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog.EndReason;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class UpdateAggregatedLogOperation implements ProcessLogVisitor {

    private final SessionFactory sessionFactory;
    private final HibernateQueryFactory queryFactory;
    private final Process process;
    private final Token token;
    private final IProcessDefinitionLoader processDefinitionLoader;

    public UpdateAggregatedLogOperation(SessionFactory sessionFactory, HibernateQueryFactory queryFactory, IProcessDefinitionLoader processDefinitionLoader,
            Process process, Token token) {
        this.sessionFactory = sessionFactory;
        this.queryFactory = queryFactory;
        this.processDefinitionLoader = processDefinitionLoader;
        this.process = process;
        this.token = token;
    }

    @Override
    public void onProcessStartLog(ProcessStartLog processStartLog) {
        if (getProcessInstanceLog(processStartLog.getProcessId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new ProcessInstanceAggregatedLog(processStartLog, process, token));
    }

    @Override
    public void onProcessActivateLog(ProcessActivateLog processActivateLog) {
    }

    @Override
    public void onProcessSuspendLog(ProcessSuspendLog processSuspendLog) {
    }

    @Override
    public void onProcessEndLog(ProcessEndLog processEndLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processEndLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processEndLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onProcessCancelLog(ProcessCancelLog processCancelLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processCancelLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processCancelLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onNodeEnterLog(NodeEnterLog nodeEnterLog) {
    }

    @Override
    public void onNodeLeaveLog(NodeLeaveLog nodeLeaveLog) {
    }

    @Override
    public void onReceiveMessageLog(ReceiveMessageLog receiveMessageLog) {
    }

    @Override
    public void onSendMessageLog(SendMessageLog sendMessageLog) {
    }

    @Override
    public void onSubprocessStartLog(SubprocessStartLog subprocessStartLog) {
    }

    @Override
    public void onSubprocessEndLog(SubprocessEndLog subprocessEndLog) {
    }

    @Override
    public void onActionLog(ActionLog actionLog) {
    }

    @Override
    public void onCreateTimerLog(CreateTimerLog createTimerLog) {
    }

    @Override
    public void onTaskCreateLog(TaskCreateLog taskCreateLog) {
        if (getTaskLog(taskCreateLog.getTaskId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new TaskAggregatedLog(taskCreateLog, processDefinitionLoader, process, token));
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
    public void onTaskEscalationLog(TaskEscalationLog taskEscalationLog) {
    }

    @Override
    public void onTaskDelegaionLog(TaskDelegationLog taskDelegationLog) {
    }

    @Override
    public void onTaskRemovedOnProcessEndLog(TaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog) {
        onTaskEnd(taskRemovedOnProcessEndLog, EndReason.PROCESS_END);
    }

    @Override
    public void onTaskExpiredLog(TaskExpiredLog taskExpiredLog) {
        onTaskEnd(taskExpiredLog, EndReason.TIMEOUT);
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
        onTaskEnd(taskCancelledLog, EndReason.CANCELLED);
    }

    @Override
    public void onSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog) {
    }

    @Override
    public void onTransitionLog(TransitionLog transitionLog) {
    }

    @Override
    public void onVariableCreateLog(VariableCreateLog variableCreateLog) {
    }

    @Override
    public void onVariableDeleteLog(VariableDeleteLog variableDeleteLog) {
    }

    @Override
    public void onVariableUpdateLog(VariableUpdateLog variableUpdateLog) {
    }

    @Override
    public void onAdminActionLog(AdminActionLog adminActionLog) {
    }

    private ProcessInstanceAggregatedLog getProcessInstanceLog(long processId) {
        QProcessInstanceAggregatedLog l = QProcessInstanceAggregatedLog.processInstanceAggregatedLog;
        return queryFactory.selectFrom(l).where(l.processInstanceId.eq(processId)).fetchFirst();
    }

    private TaskAggregatedLog getTaskLog(long taskId) {
        QTaskAggregatedLog l = QTaskAggregatedLog.taskAggregatedLog;
        return queryFactory.selectFrom(l).where(l.taskId.eq(taskId)).fetchFirst();
    }

    private void onTaskEnd(TaskEndLog taskEndLog, EndReason endReason) {
        TaskAggregatedLog logEntry = getTaskLog(taskEndLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateOnEnd(taskEndLog.getCreateDate(), taskEndLog.getActorName(), endReason);
        sessionFactory.getCurrentSession().merge(logEntry);
    }
}
