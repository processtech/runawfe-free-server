package ru.runa.wf.logic.bot;

/**
 * Interface for components, which execute bot tasks and may be interrupted.
 */
public interface BotExecutionStatus {

    public abstract WorkflowBotTaskExecutionStatus getExecutionStatus();

    public abstract boolean interruptExecution();

    public abstract int getExecutionInSeconds();
}