package ru.runa.wfe.audit;

public abstract class ProcessLogVisitor {

    public void onProcessStartLog(ProcessStartLog processStartLog) {}

    public void onProcessActivateLog(ProcessActivateLog processActivateLog) {}

    public void onProcessSuspendLog(ProcessSuspendLog processSuspendLog) {}

    public void onProcessEndLog(ProcessEndLog processEndLog) {}

    public void onProcessCancelLog(ProcessCancelLog processCancelLog) {}

    public void onNodeEnterLog(NodeEnterLog nodeEnterLog) {}

    public void onNodeLeaveLog(NodeLeaveLog nodeLeaveLog) {}

    public void onReceiveMessageLog(ReceiveMessageLog receiveMessageLog) {}

    public void onNodeErrorLog(NodeErrorLog nodeErrorLog) {}

    public void onSendMessageLog(SendMessageLog sendMessageLog) {}

    public void onSubprocessStartLog(SubprocessStartLog subprocessStartLog) {}

    public void onSubprocessEndLog(SubprocessEndLog subprocessEndLog) {}

    public void onActionLog(ActionLog actionLog) {}

    public void onCreateTimerLog(CreateTimerLog createTimerLog) {}

    public void onTaskCreateLog(TaskCreateLog taskCreateLog) {}

    public void onTaskAssignLog(TaskAssignLog taskAssignLog) {}

    public void onTaskEndLog(TaskEndLog taskEndLog) {}

    public void onTaskEscalationLog(TaskEscalationLog taskEscalationLog) {}

    public void onTaskDelegationLog(TaskDelegationLog taskDelegationLog) {}

    public void onTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog) {}

    public void onTaskEndByAdminLog(TaskEndByAdminLog taskEndByAdminLog) {}

    public void onTaskCancelledLog(TaskCancelledLog taskCancelledLog) {}

    public void onSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog) {}

    public void onTransitionLog(TransitionLog transitionLog) {}

    public void onVariableCreateLog(VariableCreateLog variableCreateLog) {}

    public void onVariableDeleteLog(VariableDeleteLog variableDeleteLog) {}

    public void onVariableUpdateLog(VariableUpdateLog variableUpdateLog) {}

    public void onAdminActionLog(AdminActionLog adminActionLog) {};

    public void onNodeInfoLog(NodeInfoLog nodeInfoLog) {};
}
