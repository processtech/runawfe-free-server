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
package ru.runa.wf.logic.bot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * Execute task handlers for particular bot.
 * 
 * Configures and executes task handler in same method.
 * 
 * This class is not thread safe.
 * 
 * @author Dofs
 * @since 4.0
 */
public class WorkflowBotExecutor {
    private final User user;
    private Bot bot;
    private final Map<String, BotTask> botTasks = Maps.newHashMap();
    private final Set<WorkflowBotTaskExecutor> botTaskExecutors;

    public WorkflowBotExecutor(User user, Bot bot, List<BotTask> tasks) {
        this.user = user;
        botTaskExecutors = new HashSet<WorkflowBotTaskExecutor>();
        reinitialize(bot, tasks);
    }

    public void reinitialize(Bot bot, List<BotTask> tasks) {
        this.bot = bot;
        botTasks.clear();
        for (BotTask botTask : tasks) {
            botTasks.put(botTask.getName(), botTask);
        }
    }

    public User getUser() {
        return user;
    }

    public Bot getBot() {
        return bot;
    }

    public Map<String, BotTask> getBotTasks() {
        return botTasks;
    }

    public WorkflowBotTaskExecutor createBotTaskExecutor(WfTask task) {
        WorkflowBotTaskExecutor result = new WorkflowBotTaskExecutor(this, task);
        if (botTaskExecutors.contains(result)) {
            for (WorkflowBotTaskExecutor existingBotTaskExecutor : botTaskExecutors) {
                if (existingBotTaskExecutor.equals(result)) {
                    if (existingBotTaskExecutor.getExecutionStatus() != WorkflowBotTaskExecutionStatus.FAILED) {
                        throw new InternalApplicationException("only failed tasks may be recreated: " + existingBotTaskExecutor);
                    }
                    result = existingBotTaskExecutor;
                    result.setExecutionStatus(WorkflowBotTaskExecutionStatus.SCHEDULED);
                    break;
                }
            }
        } else {
            botTaskExecutors.add(result);
        }
        return result;
    }

    public Set<WfTask> getNewTasks() {
        Set<WfTask> result = new HashSet<WfTask>();
        for (Iterator<WorkflowBotTaskExecutor> botIterator = botTaskExecutors.iterator(); botIterator.hasNext();) {
            BotExecutionStatus taskExecutor = botIterator.next();
            if (taskExecutor.getExecutionStatus() == WorkflowBotTaskExecutionStatus.COMPLETED) {
                // Completed bot task hold time is elapsed
                botIterator.remove();
            }
            if (taskExecutor.getExecutionStatus() == WorkflowBotTaskExecutionStatus.SCHEDULING_FAILURE) {
                // Bot task must be rescheduled.
                botIterator.remove();
            }
        }
        List<WfTask> currentTasks = Delegates.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createNonPaged());
        for (WfTask task : currentTasks) {
            BotExecutionStatus testingExecutor = new WorkflowBotTaskExecutor(this, task);
            if (!botTaskExecutors.contains(testingExecutor)) {
                result.add(task);
                continue;
            }
            for (WorkflowBotTaskExecutor taskExecutor : botTaskExecutors) {
                if (Objects.equal(task, taskExecutor.getTask())) {
                    if (taskExecutor.isReadyToAttemptExecuteFailedTask()) {
                        result.add(task);
                    }
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Template " + bot;
    }

}
