package ru.runa.wfe.task;

import com.google.common.base.MoreObjects;
import ru.runa.wfe.user.Executor;

public class TaskCompletionInfo {
    private final TaskCompletionBy completionBy;
    private final Executor executor;
    private final String handlerInfo;
    private final Long processId;
    private String transitionName;

    private TaskCompletionInfo(TaskCompletionBy completionBy, Executor executor, String handlerInfo, Long processId) {
        this.completionBy = completionBy;
        this.executor = executor;
        this.handlerInfo = handlerInfo;
        this.processId = processId;
    }
	
    private TaskCompletionInfo(TaskCompletionBy completionBy, Executor executor, String handlerInfo, Long processId, String transitionName) {
        this.completionBy = completionBy;
        this.executor = executor;
        this.handlerInfo = handlerInfo;
        this.processId = processId;
        this.transitionName = transitionName;
    }

    private TaskCompletionInfo(TaskCompletionBy completionBy, Executor executor, String transitionName) {
        this(completionBy, executor, null, null, transitionName);
    }

    private TaskCompletionInfo(TaskCompletionBy completionBy, String handlerInfo) {
        this(completionBy, null, handlerInfo, null);
    }

    public static TaskCompletionInfo createForUser(TaskCompletionBy completionBy, Executor executor, String transitionName) {
        return new TaskCompletionInfo(completionBy, executor, null, null, transitionName);
    }

    public static TaskCompletionInfo createForTimer(String transitionName) {
        return new TaskCompletionInfo(TaskCompletionBy.TIMER, null, null, null, transitionName);
    }

    public static TaskCompletionInfo createForProcessEnd(Long processId) {
        return new TaskCompletionInfo(TaskCompletionBy.PROCESS_END, null, null, processId);
    }

    public static TaskCompletionInfo createForEmbeddedSubprocessEnd() {
        return new TaskCompletionInfo(TaskCompletionBy.EMBEDDED_SUBPROCESS_END, null, null, null);
    }

    public static TaskCompletionInfo createForHandler(String handlerInfo) {
        return new TaskCompletionInfo(TaskCompletionBy.HANDLER, null, handlerInfo, null);
    }

    public static TaskCompletionInfo createForSignal(Executor executor, String transitionName) {
        return new TaskCompletionInfo(TaskCompletionBy.SIGNAL, executor, transitionName);
    }

    public TaskCompletionBy getCompletionBy() {
        return completionBy;
    }

    public Executor getExecutor() {
        return executor;
    }

    public String getHandlerInfo() {
        return handlerInfo;
    }

    public Long getProcessId() {
        return processId;
    }

    public String getTransitionName() {
        return transitionName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("by", completionBy).add("executor", executor).add("processId", processId).toString();
    }

}
