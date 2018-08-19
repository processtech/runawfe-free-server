package ru.runa.wfe.audit;

public interface ProcessLogVisitor {

    void onProcessStartLog(IProcessStartLog processStartLog);

    void onProcessActivateLog(IProcessActivateLog processActivateLog);

    void onProcessSuspendLog(IProcessSuspendLog processSuspendLog);

    void onProcessEndLog(IProcessEndLog processEndLog);

    void onProcessCancelLog(IProcessCancelLog processCancelLog);

    void onNodeEnterLog(INodeEnterLog nodeEnterLog);

    void onNodeLeaveLog(INodeLeaveLog nodeLeaveLog);

    void onReceiveMessageLog(IReceiveMessageLog receiveMessageLog);

    void onSendMessageLog(ISendMessageLog sendMessageLog);

    void onSubprocessStartLog(ISubprocessStartLog subprocessStartLog);

    void onSubprocessEndLog(ISubprocessEndLog subprocessEndLog);

    void onActionLog(IActionLog actionLog);

    void onCreateTimerLog(ICreateTimerLog createTimerLog);

    void onTaskCreateLog(ITaskCreateLog taskCreateLog);

    void onTaskAssignLog(ITaskAssignLog taskAssignLog);

    void onTaskEndLog(ITaskEndLog taskEndLog);

    void onTaskEscalationLog(ITaskEscalationLog taskEscalationLog);

    void onTaskDelegaionLog(ITaskDelegationLog taskDelegationLog);

    void onTaskRemovedOnProcessEndLog(ITaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog);

    void onTaskExpiredLog(ITaskExpiredLog taskExpiredLog);

    void onTaskEndBySubstitutorLog(ITaskEndBySubstitutorLog taskEndBySubstitutorLog);

    void onTaskEndByAdminLog(ITaskEndByAdminLog taskEndByAdminLog);

    void onTaskCancelledLog(ITaskCancelledLog taskCancelledLog);

    void onSwimlaneAssignLog(ISwimlaneAssignLog swimlaneAssignLog);

    void onTransitionLog(ITransitionLog transitionLog);

    void onVariableCreateLog(IVariableCreateLog variableCreateLog);

    void onVariableDeleteLog(IVariableDeleteLog variableDeleteLog);

    void onVariableUpdateLog(IVariableUpdateLog variableUpdateLog);

    void onAdminActionLog(IAdminActionLog adminActionLog);
}
