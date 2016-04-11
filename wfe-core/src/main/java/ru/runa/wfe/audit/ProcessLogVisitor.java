package ru.runa.wfe.audit;

public interface ProcessLogVisitor {

    void onProcessStartLog(ProcessStartLog processStartLog);

    void onProcessEndLog(ProcessEndLog processEndLog);

    void onProcessCancelLog(ProcessCancelLog processCancelLog);

    void onNodeEnterLog(NodeEnterLog nodeEnterLog);

    void onNodeLeaveLog(NodeLeaveLog nodeLeaveLog);

    void onReceiveMessageLog(ReceiveMessageLog receiveMessageLog);

    void onSendMessageLog(SendMessageLog sendMessageLog);

    void onSubprocessStartLog(SubprocessStartLog subprocessStartLog);

    void onSubprocessEndLog(SubprocessEndLog subprocessEndLog);

    void onActionLog(ActionLog actionLog);

    void onCreateTimerActionLog(CreateTimerActionLog createTimerActionLog);

    void onTaskCreateLog(TaskCreateLog taskCreateLog);

    void onTaskAssignLog(TaskAssignLog taskAssignLog);

    void onTaskEndLog(TaskEndLog taskEndLog);

    void onTaskEscalationLog(TaskEscalationLog taskEscalationLog);

    void onTaskDelegaionLog(TaskDelegationLog taskDelegationLog);

    void onTaskRemovedOnProcessEndLog(TaskRemovedOnProcessEndLog taskRemovedOnProcessEndLog);

    void onTaskExpiredLog(TaskExpiredLog taskExpiredLog);

    void onTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog);

    void onTaskEndByAdminLog(TaskEndByAdminLog taskEndByAdminLog);

    void onTaskCancelledLog(TaskCancelledLog taskCancelledLog);

    void onSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog);

    void onTransitionLog(TransitionLog transitionLog);

    void onVariableCreateLog(VariableCreateLog variableCreateLog);

    void onVariableDeleteLog(VariableDeleteLog variableDeleteLog);

    void onVariableUpdateLog(VariableUpdateLog variableUpdateLog);

    void onAdminActionLog(AdminActionLog adminActionLog);
}
