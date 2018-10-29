package ru.runa.wfe.extension;

import java.util.Map;

import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Interface for bot task execution.
 */
public interface TaskHandler {
    /**
     * If this variable assigned to Boolean.TRUE then bot subsystem will not
     * complete task.
     */
    String SKIP_TASK_COMPLETION_VARIABLE_NAME = "skipTaskCompletion";

    /**
     * Configuring bot task.
     * 
     * @param configuration
     *            Loaded from database configuration.
     * @param embeddedFile
     * 			  Embedded BotTask file which can be used in TaskHandler.
     */
    void setConfiguration(byte[] configuration, byte[] embeddedFile) throws Exception;

    /**
     * Get configuration for debug purpose.
     */
    String getConfiguration();

    /**
     * Handles task assigned to bot.
     * 
     * @param user
     *            bot subject.
     * @param variableProvider
     *            access process variables
     * @param task
     *            task to be processed.
     */
    Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception;

    /**
     * Invoked if task completion fails.
     * 
     * @param user
     *            bot subject.
     * @param variableProvider
     *            access process variables
     * @param task
     *            task to be processed.
     */
    void onRollback(User user, VariableProvider variableProvider, WfTask task) throws Exception;
}
