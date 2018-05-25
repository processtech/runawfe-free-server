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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.EmbeddedSubprocessEndNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * Execute task handlers for particular bot.
 *
 * Configures and executes task handler in same method.
 *
 * This class is not thread safe. Configures and executes task handler in same method.
 *
 * This class is not thread safe.
 *
 * @author Dofs
 * @since 4.0
 */
public class WorkflowBotExecutor {
    private static final Log log = LogFactory.getLog(WorkflowBotExecutor.class);
    private final User user;
    private Bot bot;
    private final Map<String, BotTask> botTasks = Maps.newHashMap();
    private final Set<WorkflowBotTaskExecutor> botTaskExecutors = new HashSet<WorkflowBotTaskExecutor>();

    public WorkflowBotExecutor(User user, Bot bot, List<BotTask> tasks) {
        this.user = user;
        reinitialize(bot, tasks);
    }

    public void reinitialize(Bot bot, List<BotTask> tasks) {
        this.bot = bot;
        botTasks.clear();
        for (BotTask botTask : tasks) {
            botTasks.put(botTask.getName(), botTask);
        }
    }

    public void resetFailedDelay() {
        for (WorkflowBotTaskExecutor botTaskExecutor : botTaskExecutors) {
            botTaskExecutor.resetFailedDelay();
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
            WorkflowBotTaskExecutor taskExecutor = botIterator.next();
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
        if (bot.isTransactional()) {
            if (isEmbeddedSubprocessEndedToWhichBotIsBound()) {
                log.debug("Unbinding bot from " + bot.getBoundProcessId() + ":" + bot.getBoundSubprocessId());
                bot.unbindFromEmbeddedSubprocess();
                Delegates.getBotService().updateBot(user, bot, false);
            } else if (isBotTransactionalBindingExpired()) {
                log.debug("Sending timeout error from " + bot.getBoundProcessId() + ":" + bot.getBoundSubprocessId());
                sendErrorToTransactionalEmbeddedSubprocessNode();
                bot.unbindFromEmbeddedSubprocess();
                Delegates.getBotService().updateBot(user, bot, false);
            }

            for (WfTask task : currentTasks) {
                if (isBotBoundToEmbeddedSubprocess()) {
                    if (!task.getProcessId().equals(bot.getBoundProcessId())) {
                        log.debug("Bot is bound to " + bot.getBoundProcessId() + "; ignoring task from another process " + task.getProcessId());
                        continue;
                    }
                    if (!StringUtils.startsWith(task.getNodeId(), bot.getBoundSubprocessId())) {
                        log.debug("Bot is bound to " + bot.getBoundProcessId() + ":" + bot.getBoundSubprocessId() + "; ignoring task "
                                + task.getNodeId());
                        continue;
                    }
                } else {
                    if (StringUtils.startsWith(task.getNodeId(), IFileDataProvider.SUBPROCESS_DEFINITION_PREFIX)) {
                        ProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(user,
                                task.getDefinitionId());
                        Node taskNode = processDefinition.getNode(task.getNodeId());

                        SubprocessDefinition subprocessDefinition = (SubprocessDefinition) taskNode.getProcessDefinition();
                        String embeddedSubprocessNodeId = processDefinition.getEmbeddedSubprocessNodeIdNotNull(subprocessDefinition.getName());
                        SubprocessNode subprocessNode = (SubprocessNode) processDefinition.getNode(embeddedSubprocessNodeId);

                        if (subprocessNode.isTransactional()) {
                            bot.bindToEmbeddedSubprocess(task.getProcessId(), subprocessDefinition.getNodeId());
                            log.debug("Binding bot to " + bot.getBoundProcessId() + ":" + bot.getBoundSubprocessId());
                            Delegates.getBotService().updateBot(user, bot, false);
                        }
                    }
                }
                addTaskToExecutionSet(result, task);
            }
        } else {
            for (WfTask task : currentTasks) {
                addTaskToExecutionSet(result, task);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "Template " + bot;
    }

    private void addTaskToExecutionSet(Set<WfTask> result, WfTask task) {
        BotExecutionStatus testingExecutor = new WorkflowBotTaskExecutor(this, task);
        if (!botTaskExecutors.contains(testingExecutor)) {
            result.add(task);
            return;
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

    private boolean isBotBoundToEmbeddedSubprocess() {
        return bot.getBoundDueDate() != null;
    }

    private boolean isBotTransactionalBindingExpired() {
        return isBotBoundToEmbeddedSubprocess() ? bot.getBoundDueDate().before(new Date()) : false;
    }

    private boolean isEmbeddedSubprocessEndedToWhichBotIsBound() {
        if (!isBotBoundToEmbeddedSubprocess()) {
            return false;
        }

        ProcessLogFilter filter = new ProcessLogFilter();
        filter.setRootClassName(NodeLeaveLog.class.getName());
        filter.setProcessId(bot.getBoundProcessId());

        WfProcess process = Delegates.getExecutionService().getProcess(user, bot.getBoundProcessId());
        if (process.isEnded()) {
            return true;
        }
        ProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(user, process.getDefinitionId());
        SubprocessDefinition subprocessDefinition = processDefinition.getEmbeddedSubprocessByIdNotNull(bot.getBoundSubprocessId());
        List<EmbeddedSubprocessEndNode> endNodes = subprocessDefinition.getEndNodes();

        for (EmbeddedSubprocessEndNode endNode : endNodes) {
            filter.setNodeId(endNode.getNodeId());

            ProcessLogs processLogs = Delegates.getAuditService().getProcessLogs(user, filter);
            if (processLogs.getLogs().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void sendErrorToTransactionalEmbeddedSubprocessNode() {
        Preconditions.checkArgument(isBotBoundToEmbeddedSubprocess());

        WfProcess process = Delegates.getExecutionService().getProcess(user, bot.getBoundProcessId());
        ProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(user, process.getDefinitionId());
        SubprocessDefinition subprocessDefinition = processDefinition.getEmbeddedSubprocessByIdNotNull(bot.getBoundSubprocessId());
        final String embeddedSubprocessNodeId = processDefinition.getEmbeddedSubprocessNodeIdNotNull(subprocessDefinition.getName());

        new TransactionalExecutor() {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                Utils.sendBpmnErrorMessage(bot.getBoundProcessId(), embeddedSubprocessNodeId, new Throwable("Transactional bot " + bot.getUsername()
                        + " timeout expired"));
            }
        }.executeInTransaction(false);
    }

}
