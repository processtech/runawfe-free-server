package ru.runa.wfe.task;

import com.google.common.base.MoreObjects;
import ru.runa.wfe.user.Executor;

public class TaskCompletionInfo {
    private final TaskCompletionBy completionBy;
    private final Executor executor;
    private final String handlerInfo;
    private final Long processId;

    private TaskCompletionInfo(TaskCompletionBy completionBy, Executor executor, String handlerInfo, Long processId) {
        this.completionBy = completionBy;
        this.executor = executor;
        this.handlerInfo = handlerInfo;
        this.processId = processId;
    }

    private TaskCompletionInfo(TaskCompletionBy completionBy, Executor executor) {
        this(completionBy, executor, null, null);
    }

    private TaskCompletionInfo(TaskCompletionBy completionBy, String handlerInfo) {
        this(completionBy, null, handlerInfo, null);
    }

    public static TaskCompletionInfo createForUser(TaskCompletionBy completionBy, Executor executor) {
        return new TaskCompletionInfo(completionBy, executor, null, null);
    }

    public static TaskCompletionInfo createForTimer() {
        return new TaskCompletionInfo(TaskCompletionBy.TIMER, null, null, null);
    }

    public static TaskCompletionInfo createForProcessEnd(Long processId) {
        return new TaskCompletionInfo(TaskCompletionBy.PROCESS_END, null, null, processId);
    }

    public static TaskCompletionInfo createForHandler(String handlerInfo) {
        return new TaskCompletionInfo(TaskCompletionBy.HANDLER, null, handlerInfo, null);
    }

    public static TaskCompletionInfo createForSignal(Executor executor) {
        return new TaskCompletionInfo(TaskCompletionBy.SIGNAL, executor);
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("by", completionBy).add("executor", executor).add("processId", processId).toString();
    }
}
