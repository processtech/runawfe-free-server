package ru.runa.wf.logic.bot;

import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;

/**
 * Bot task executor for sequential execution of all bot tasks.
 */
public class WorkflowSequentialBotTaskExecutor implements Runnable, BotExecutionStatus {
    private static final Log log = LogFactory.getLog(WorkflowSequentialBotTaskExecutor.class);

    /**
     * Used only for equals() and hashCode(). Bot with sequential tasks execution strategy.
     */
    private final Bot bot;
    /**
     * Used only for equals() and hashCode(). Optional bot task, if executor created for specific bot task, not all bot tasks.
     */
    private final BotTask botTask;

    /**
     * Bot tasks, which must be executed by this task executor.
     */
    private final Queue<WorkflowBotTaskExecutor> executors = Queues.newConcurrentLinkedQueue();
    /**
     * Current execution status.
     */
    private WorkflowBotTaskExecutionStatus executionStatus = WorkflowBotTaskExecutionStatus.SCHEDULED;
    /**
     * Flag, equals true, if execution must be stopped; false if sequential bot
     * execution may be continue.
     */
    private boolean stopExecution = false;

    public WorkflowSequentialBotTaskExecutor(Bot bot, BotTask botTask, List<WorkflowBotTaskExecutor> tasksToExecute) {
        super();
        this.bot = bot;
        this.botTask = botTask;
        if (tasksToExecute != null) {
            executors.addAll(tasksToExecute);
        }
    }

    @Override
    public WorkflowBotTaskExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    @Override
    public boolean interruptExecution() {
        stopExecution = true;
        WorkflowBotTaskExecutor currentExecutor = executors.peek();
        if (currentExecutor == null) {
            return true;
        }
        for (WorkflowBotTaskExecutor executor : executors) {
            if (currentExecutor.equals(executor)) {
                continue;
            }
            executor.setExecutionStatus(WorkflowBotTaskExecutionStatus.SCHEDULING_FAILURE);
        }
        return currentExecutor.interruptExecution();
    }

    @Override
    public int getExecutionInSeconds() {
        WorkflowBotTaskExecutor currentExecutor = executors.peek();
        if (currentExecutor == null || currentExecutor.getExecutionStatus() == WorkflowBotTaskExecutionStatus.SCHEDULED) {
            return 0;
        }
        return currentExecutor.getExecutionInSeconds();
    }

    @Override
    public void run() {
        executionStatus = WorkflowBotTaskExecutionStatus.STARTED;
        try {
            while (!executors.isEmpty() && !stopExecution) {
                WorkflowBotTaskExecutor currentExecutor = executors.peek();
                processTaskSafe(currentExecutor);
                executors.poll();
            }
        } finally {
            executionStatus = WorkflowBotTaskExecutionStatus.COMPLETED;
        }
    }

    /**
     * Executes bot task with exception catching.
     * 
     * @param currentExecutor
     *            Bot task to execute.
     */
    private void processTaskSafe(WorkflowBotTaskExecutor currentExecutor) {
        try {
            currentExecutor.run();
        } catch (Throwable e) {
            log.error("Sequential bot execution error for " + currentExecutor, e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bot, botTask);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkflowSequentialBotTaskExecutor) {
            WorkflowSequentialBotTaskExecutor other = (WorkflowSequentialBotTaskExecutor) obj;
            return Objects.equal(bot, other.bot) && Objects.equal(botTask, other.botTask);
        }
        return false;
    }
}
