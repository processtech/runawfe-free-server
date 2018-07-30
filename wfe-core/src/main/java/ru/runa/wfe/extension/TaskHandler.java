/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.extension;

import java.util.Map;

import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

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
    Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception;

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
    void onRollback(User user, IVariableProvider variableProvider, WfTask task) throws Exception;
}
