package ru.runa.wfe.audit;

public interface ProcessLogVisitor {

    default void onProcessStartLog(ProcessStartLog processStartLog) {
    }

    default void onProcessActivateLog(ProcessActivateLog processActivateLog) {
    }

    default void onProcessSuspendLog(ProcessSuspendLog processSuspendLog) {
    }

    default void onProcessEndLog(ProcessEndLog processEndLog) {
    }

    default void onProcessCancelLog(ProcessCancelLog processCancelLog) {
    }

    default void onNodeEnterLog(NodeEnterLog nodeEnterLog) {
    }

    default void onNodeLeaveLog(NodeLeaveLog nodeLeaveLog) {
    }

    default void onNodeErrorLog(NodeErrorLog nodeErrorLog) {
    }

    default void onReceiveMessageLog(ReceiveMessageLog receiveMessageLog) {
    }

    default void onSendMessageLog(SendMessageLog sendMessageLog) {
    }

    default void onSubprocessStartLog(SubprocessStartLog subprocessStartLog) {
    }

    default void onSubprocessEndLog(SubprocessEndLog subprocessEndLog) {
    }

    default void onActionLog(ActionLog actionLog) {
    }

    default void onCreateTimerLog(CreateTimerLog createTimerLog) {
    }

    default void onTaskCreateLog(TaskCreateLog taskCreateLog) {
    }

    default void onTaskAssignLog(TaskAssignLog taskAssignLog) {
    }

    default void onTaskEndLog(TaskEndLog taskEndLog) {
    }

    default void onTaskEscalationLog(TaskEscalationLog taskEscalationLog) {
    }

    default void onTaskDelegaionLog(TaskDelegationLog taskDelegationLog) {
    }

    default void onTaskEndBySubstitutorLog(TaskEndBySubstitutorLog taskEndBySubstitutorLog) {
    }

    default void onTaskEndByAdminLog(TaskEndByAdminLog taskEndByAdminLog) {
    }

    default void onTaskCancelledLog(TaskCancelledLog taskCancelledLog) {
    }

    default void onSwimlaneAssignLog(SwimlaneAssignLog swimlaneAssignLog) {
    }

    default void onTransitionLog(TransitionLog transitionLog) {
    }

    default void onVariableCreateLog(VariableCreateLog variableCreateLog) {
    }

    default void onVariableDeleteLog(VariableDeleteLog variableDeleteLog) {
    }

    default void onVariableUpdateLog(VariableUpdateLog variableUpdateLog) {
    }

    default void onAdminActionLog(AdminActionLog adminActionLog) {
    }

    default void onNodeInfoLog(NodeInfoLog nodeInfoLog) {
    }
}
