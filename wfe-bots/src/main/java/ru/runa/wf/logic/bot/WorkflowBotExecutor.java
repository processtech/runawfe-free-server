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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

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
        if (bot.isTransactional()) {

            if (isBotBindedEmbeddedSubprocessEnded()) {
                unbindBotFromEmbeddedSubprocess();
            } else if (isBotTimeoutExpired()) {
                sendErrorMsgToBotEmbeddedSubprocessNode();
                unbindBotFromEmbeddedSubprocess();
            }

            for (WfTask task : currentTasks) {

                if (!isBotBindedToEmbeddedSubprocess()) {

                    if (StringUtils.startsWith(task.getNodeId(), IFileDataProvider.SUBPROCESS_DEFINITION_PREFIX)) {
                        ProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(user,
                                task.getDefinitionId());
                        Node taskNode = processDefinition.getNode(task.getNodeId());

                        SubprocessDefinition subprocessDefinition = (SubprocessDefinition) taskNode.getProcessDefinition();
                        String embeddedSubprocessNodeId = processDefinition.getEmbeddedSubprocessNodeIdNotNull(subprocessDefinition.getName());
                        SubprocessNode subprocessNode = (SubprocessNode) processDefinition.getNode(embeddedSubprocessNodeId);

                        if (subprocessNode.isTransaction()) {
                            bindBotToEmbeddedSubprocess(task.getProcessId(), embeddedSubprocessNodeId, subprocessDefinition.getNodeId());
                        }
                    }

                } else if (!isBotBindedToProcessWithId(task.getProcessId())) {
                    continue;
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

    private boolean isBotBindedToEmbeddedSubprocess() {
        return bot.getBotTimeout() != null;
    }

    private boolean isBotTimeoutExpired() {
        return isBotBindedToEmbeddedSubprocess() ? bot.getBotTimeout().before(new Date()) : false;
    }

    private boolean isBotBindedToProcessWithId(Long processId) {
        return isBotBindedToEmbeddedSubprocess() ? processId.equals(bot.getProcessId()) : false;
    }

    private void bindBotToEmbeddedSubprocess(Long processId, String subprocessNodeId, String subprocessDefinitionNodeId) {
        bot.setBotTimeout(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(bot.getTimeout())));
        bot.setProcessId(processId);
        bot.setSubprocessNodeId(subprocessNodeId);
        bot.setSubprocessDefinitionNodeId(subprocessDefinitionNodeId);
    }

    private void unbindBotFromEmbeddedSubprocess() {
        bot.setBotTimeout(null);
        bot.setProcessId(null);
        bot.setSubprocessNodeId(null);
        bot.setSubprocessDefinitionNodeId(null);
    }

    private boolean isBotBindedEmbeddedSubprocessEnded() {
        if (!isBotBindedToEmbeddedSubprocess()) {
            return false;
        }

        boolean isEnded = false;

        ProcessLogFilter filter = new ProcessLogFilter();
        filter.setRootClassName(NodeLeaveLog.class.getName());
        filter.setProcessId(bot.getProcessId());

        WfProcess process = Delegates.getExecutionService().getProcess(user, bot.getProcessId());
        ProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(user, process.getDefinitionId());
        SubprocessDefinition subprocessDefinition = processDefinition.getEmbeddedSubprocessByIdNotNull(bot.getSubprocessDefinitionNodeId());
        List<EmbeddedSubprocessEndNode> endNodes = subprocessDefinition.getEndNodes();

        for (EmbeddedSubprocessEndNode endNode : endNodes) {
            filter.setNodeId(endNode.getNodeId());

            ProcessLogs processLogs = Delegates.getAuditService().getProcessLogs(user, filter);
            if (processLogs.getLogs().size() > 0) {
                isEnded = true;
                break;
            }
        }

        return isEnded;
    }

    private void sendErrorMsgToBotEmbeddedSubprocessNode() {
        if (!isBotBindedToEmbeddedSubprocess()) {
            return;
        }

        new TransactionalExecutor() {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                Utils.sendBpmnErrorMessage(bot.getProcessId(), bot.getSubprocessNodeId(),
                        new Throwable("Transactional bot " + bot.getUsername() + " timeout expired"));
            }
        }.executeInTransaction(false);
    }
}
