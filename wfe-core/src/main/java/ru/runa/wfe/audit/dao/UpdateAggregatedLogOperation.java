package ru.runa.wfe.audit.dao;

import org.hibernate.SessionFactory;
import ru.runa.wfe.audit.IActionLog;
import ru.runa.wfe.audit.IAdminActionLog;
import ru.runa.wfe.audit.ICreateTimerLog;
import ru.runa.wfe.audit.INodeEnterLog;
import ru.runa.wfe.audit.INodeLeaveLog;
import ru.runa.wfe.audit.IProcessActivateLog;
import ru.runa.wfe.audit.IProcessCancelLog;
import ru.runa.wfe.audit.IProcessEndLog;
import ru.runa.wfe.audit.IProcessStartLog;
import ru.runa.wfe.audit.IProcessSuspendLog;
import ru.runa.wfe.audit.IReceiveMessageLog;
import ru.runa.wfe.audit.ISendMessageLog;
import ru.runa.wfe.audit.ISubprocessEndLog;
import ru.runa.wfe.audit.ISubprocessStartLog;
import ru.runa.wfe.audit.ISwimlaneAssignLog;
import ru.runa.wfe.audit.ITaskAssignLog;
import ru.runa.wfe.audit.ITaskCancelledLog;
import ru.runa.wfe.audit.ITaskCreateLog;
import ru.runa.wfe.audit.ITaskDelegationLog;
import ru.runa.wfe.audit.ITaskEndByAdminLog;
import ru.runa.wfe.audit.ITaskEndBySubstitutorLog;
import ru.runa.wfe.audit.ITaskEndLog;
import ru.runa.wfe.audit.ITaskEscalationLog;
import ru.runa.wfe.audit.ITaskExpiredLog;
import ru.runa.wfe.audit.ITaskRemovedOnProcessEndLog;
import ru.runa.wfe.audit.ITransitionLog;
import ru.runa.wfe.audit.IVariableCreateLog;
import ru.runa.wfe.audit.IVariableDeleteLog;
import ru.runa.wfe.audit.IVariableUpdateLog;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.aggregated.ProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.QProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.QTaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog.EndReason;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class UpdateAggregatedLogOperation implements ProcessLogVisitor {

    private final SessionFactory sessionFactory;
    private final HibernateQueryFactory queryFactory;
    private final Process process;
    private final Token token;
    private final ProcessDefinitionLoader processDefinitionLoader;

    public UpdateAggregatedLogOperation(SessionFactory sessionFactory, HibernateQueryFactory queryFactory,
            ProcessDefinitionLoader processDefinitionLoader,
            Process process, Token token) {
        this.sessionFactory = sessionFactory;
        this.queryFactory = queryFactory;
        this.processDefinitionLoader = processDefinitionLoader;
        this.process = process;
        this.token = token;
    }

    @Override
    public void onProcessStartLog(IProcessStartLog processStartLog) {
        if (getProcessInstanceLog(processStartLog.getProcessId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new ProcessInstanceAggregatedLog(processStartLog, process, token));
    }

    @Override
    public void onProcessActivateLog(IProcessActivateLog processActivateLog) {
    }

    @Override
    public void onProcessSuspendLog(IProcessSuspendLog processSuspendLog) {
    }

    @Override
    public void onProcessEndLog(IProcessEndLog processEndLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processEndLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processEndLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onProcessCancelLog(IProcessCancelLog processCancelLog) {
        ProcessInstanceAggregatedLog logEntry = getProcessInstanceLog(processCancelLog.getProcessId());
        if (logEntry == null) {
            return;
        }
        logEntry.update(processCancelLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onNodeEnterLog(INodeEnterLog nodeEnterLog) {
    }

    @Override
    public void onNodeLeaveLog(INodeLeaveLog nodeLeaveLog) {
    }

    @Override
    public void onReceiveMessageLog(IReceiveMessageLog receiveMessageLog) {
    }

    @Override
    public void onSendMessageLog(ISendMessageLog sendMessageLog) {
    }

    @Override
    public void onSubprocessStartLog(ISubprocessStartLog subprocessStartLog) {
    }

    @Override
    public void onSubprocessEndLog(ISubprocessEndLog subprocessEndLog) {
    }

    @Override
    public void onActionLog(IActionLog actionLog) {
    }

    @Override
    public void onCreateTimerLog(ICreateTimerLog createTimerLog) {
    }

    @Override
    public void onTaskCreateLog(ITaskCreateLog taskCreateLog) {
        if (getTaskLog(taskCreateLog.getTaskId()) != null) {
            return;
        }
        sessionFactory.getCurrentSession().save(new TaskAggregatedLog(taskCreateLog, processDefinitionLoader, process, token));
    }

    @Override
    public void onTaskAssignLog(ITaskAssignLog taskAssignLog) {
        TaskAggregatedLog logEntry = getTaskLog(taskAssignLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateAssignment(taskAssignLog);
        sessionFactory.getCurrentSession().merge(logEntry);
    }

    @Override
    public void onTaskEndLog(ITaskEndLog taskEndLog) {
        onTaskEnd(taskEndLog, EndReason.COMPLETED);
    }

    @Override
    public void onTaskEscalationLog(ITaskEscalationLog taskEscalationLog) {
    }

    @Override
    public void onTaskDelegaionLog(ITaskDelegationLog taskDelegationLog) {
    }

    @Override
    public void onTaskRemovedOnProcessEndLog(ITaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog) {
        onTaskEnd(taskRemovedOnProcessEndLog, EndReason.PROCESS_END);
    }

    @Override
    public void onTaskExpiredLog(ITaskExpiredLog taskExpiredLog) {
        onTaskEnd(taskExpiredLog, EndReason.TIMEOUT);
    }

    @Override
    public void onTaskEndBySubstitutorLog(ITaskEndBySubstitutorLog taskEndBySubstitutorLog) {
        onTaskEnd(taskEndBySubstitutorLog, EndReason.SUBSTITUTOR_END);
    }

    @Override
    public void onTaskEndByAdminLog(ITaskEndByAdminLog taskEndByAdminLog) {
        onTaskEnd(taskEndByAdminLog, EndReason.ADMIN_END);
    }

    @Override
    public void onTaskCancelledLog(ITaskCancelledLog taskCancelledLog) {
        onTaskEnd(taskCancelledLog, EndReason.CANCELLED);
    }

    @Override
    public void onSwimlaneAssignLog(ISwimlaneAssignLog swimlaneAssignLog) {
    }

    @Override
    public void onTransitionLog(ITransitionLog transitionLog) {
    }

    @Override
    public void onVariableCreateLog(IVariableCreateLog variableCreateLog) {
    }

    @Override
    public void onVariableDeleteLog(IVariableDeleteLog variableDeleteLog) {
    }

    @Override
    public void onVariableUpdateLog(IVariableUpdateLog variableUpdateLog) {
    }

    @Override
    public void onAdminActionLog(IAdminActionLog adminActionLog) {
    }

    private ProcessInstanceAggregatedLog getProcessInstanceLog(long processId) {
        QProcessInstanceAggregatedLog l = QProcessInstanceAggregatedLog.processInstanceAggregatedLog;
        return queryFactory.selectFrom(l).where(l.processInstanceId.eq(processId)).fetchFirst();
    }

    private TaskAggregatedLog getTaskLog(long taskId) {
        QTaskAggregatedLog l = QTaskAggregatedLog.taskAggregatedLog;
        return queryFactory.selectFrom(l).where(l.taskId.eq(taskId)).fetchFirst();
    }

    private void onTaskEnd(ITaskEndLog taskEndLog, EndReason endReason) {
        TaskAggregatedLog logEntry = getTaskLog(taskEndLog.getTaskId());
        if (logEntry == null) {
            return;
        }
        logEntry.updateOnEnd(taskEndLog.getCreateDate(), taskEndLog.getActorName(), endReason);
        sessionFactory.getCurrentSession().merge(logEntry);
    }
}
