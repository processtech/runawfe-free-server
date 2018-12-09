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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.invoker.BotInvoker;
import ru.runa.wfe.commons.CoreErrorProperties;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class WorkflowThreadPoolBotInvoker implements BotInvoker, Runnable {
    private final Log log = LogFactory.getLog(WorkflowThreadPoolBotInvoker.class);

    private static final long STUCK_TIMEOUT_SECONDS = BotStationResources.getStuckTimeoutInMinutes() * 60;

    private ScheduledThreadPoolExecutor executor;

    private long configurationVersion = -1;

    private long botstationId = -1;

    private final Map<Bot, WorkflowBotExecutor> botExecutors = Maps.newHashMap();

    private Future<?> botInvokerInvocation;

    private final Map<BotExecutionStatus, ScheduledFuture<?>> scheduledTasks = Maps.newConcurrentMap();

    private BotStation botStation;

    /**
     * Checking botInvokerInvocation.isDone() leads to run() method called only once per moment.
     */
    @Override
    public synchronized void invokeBots(BotStation botStation, boolean resetFailedDelay) {
        this.botStation = botStation;
        if (botInvokerInvocation != null && !botInvokerInvocation.isDone()) {
            log.debug("botInvokerInvocation != null && !botInvokerInvocation.isDone()");
            executor.schedule(new Runnable() {

                @Override
                public void run() {
                    invokeBots(botStation, resetFailedDelay);
                }
            }, 1000, TimeUnit.MILLISECONDS);
            return;
        }
        int poolSize = BotStationResources.getThreadPoolSize();
        if (executor == null) {
            log.debug(String.format("Creating new executor(ScheduledExecutorService),size %d", poolSize));
            executor = new ScheduledThreadPoolExecutor(poolSize, new BotNamedThreadFactory());

        } else {
            if (executor.getCorePoolSize() != poolSize) {
                log.debug(String.format("change core thread pool size from %d to %d", executor.getCorePoolSize(), poolSize));
                executor.setCorePoolSize(poolSize);
            }
            if (executor.getMaximumPoolSize() != poolSize) {
                log.debug(String.format("change maximum thread pool size from %d to %d", executor.getMaximumPoolSize(), poolSize));
                executor.setMaximumPoolSize(poolSize);
            }
        }
        checkStuckBots();
        botInvokerInvocation = executor.schedule(this, 1000, TimeUnit.MILLISECONDS);
        logBotsActivites();
        if (resetFailedDelay) {
            for (WorkflowBotExecutor botExecutor : botExecutors.values()) {
                botExecutor.resetFailedDelay();
            }
        }
    }

    private void checkStuckBots() {
        try {
            for (Iterator<Entry<BotExecutionStatus, ScheduledFuture<?>>> iter = scheduledTasks.entrySet().iterator(); iter.hasNext();) {
                Entry<BotExecutionStatus, ScheduledFuture<?>> entry = iter.next();
                if (entry.getValue().isDone()) {
                    iter.remove();
                    continue;
                }
                BotExecutionStatus executor = entry.getKey();
                if (executor.getExecutionStatus() == WorkflowBotTaskExecutionStatus.STARTED
                        && executor.getExecutionInSeconds() > STUCK_TIMEOUT_SECONDS) {
                    if (executor.interruptExecution()) {
                        iter.remove();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Stuck threads search/stop is throwing exception.", e);
        }
    }

    private void configure() {
        String botStationErrorMessage = CoreErrorProperties.getMessage(CoreErrorProperties.BOT_STATION_CONFIGURATION_ERROR, botStation.getName());
        try {
            if (botStation.getVersion() != configurationVersion || botstationId != botStation.getId()) {
                log.info("Will update bots configuration.");
                String username = BotStationResources.getSystemUsername();
                String password = BotStationResources.getSystemPassword();
                User botStationUser = Delegates.getAuthenticationService().authenticateByLoginPassword(username, password);
                Map<Bot, WorkflowBotExecutor> existingBotExecutors = Maps.newHashMap(botExecutors);
                botExecutors.clear();
                List<Bot> bots = Delegates.getBotService().getBots(botStationUser, botStation.getId());
                for (Bot bot : bots) {
                    String botErrorMessage = CoreErrorProperties.getMessage(CoreErrorProperties.BOT_CONFIGURATION_ERROR, bot.getUsername());
                    try {
                        log.info("Configuring " + bot.getUsername());
                        User user = Delegates.getAuthenticationService().authenticateByLoginPassword(bot.getUsername(), bot.getPassword());
                        List<BotTask> tasks = Delegates.getBotService().getBotTasks(user, bot.getId());
                        if (existingBotExecutors.containsKey(bot) && bot.getId().equals(existingBotExecutors.get(bot).getBot().getId())) {
                            WorkflowBotExecutor botExecutor = existingBotExecutors.get(bot);
                            botExecutor.reinitialize(bot, tasks);
                            botExecutors.put(bot, botExecutor);
                        } else {
                            botExecutors.put(bot, new WorkflowBotExecutor(user, bot, tasks));
                        }
                        Errors.removeSystemError(botErrorMessage);
                    } catch (Throwable th) {
                        log.error("Unable to configure " + bot, th);
                        Errors.addSystemError(new ConfigurationException(botErrorMessage, th));
                    }
                }
                configurationVersion = botStation.getVersion();
                botstationId = botStation.getId();
            } else {
                log.debug("bots configuration is up to date, version = " + botStation.getVersion());
            }
            Errors.removeSystemError(botStationErrorMessage);
        } catch (Throwable th) {
            log.error("Botstation configuration error", th);
            Errors.addSystemError(new ConfigurationException(botStationErrorMessage, th));
        }
    }

    /**
     * Checking botInvokerInvocation.isDone() in synchronized invokeBots method leads to run() method called only once per moment.
     */
    @Override
    public void run() {
        configure();
        if (executor == null) {
            log.warn("executor(ScheduledExecutorService) == null");
            return;
        }
        for (WorkflowBotExecutor botExecutor : botExecutors.values()) {
            try {
                if (botExecutor.getBot().isSequentialExecution()) {
                    scheduleSequentialBot(botExecutor);
                } else {
                    Set<WfTask> tasks = botExecutor.getNewTasks();
                    scheduleTasks(botExecutor, tasks);
                }
            } catch (AuthenticationException e) {
                configurationVersion = -1;
                log.error("BotRunner execution failed. Will recreate botstation settings and bots.", e);
            } catch (Exception e) {
                log.error("BotRunner execution failed.", e);
            }
        }
    }

    /**
     * Schedules all task for bot. Each parallel tasks scheduled as self. Sequential tasks is grouped for sequential execution.
     *
     * @param botExecutor
     *            Bot execution data.
     * @param tasks
     *            Tasks to schedule.
     */
    private void scheduleTasks(WorkflowBotExecutor botExecutor, Set<WfTask> tasks) {
        Map<String, List<WorkflowBotTaskExecutor>> sequentialTasks = Maps.newHashMap();
        for (WfTask task : tasks) {
            final String botTaskName = BotTaskConfigurationUtils.getBotTaskName(botExecutor.getUser(), task);
            BotTask botTaskConfiguration = botExecutor.getBotTasks().get(botTaskName);
            if (botTaskConfiguration == null) { // in spite of that, the task
                // should be started because it
                // is necessary to show the
                // error message
                log.error("No handler for bot task " + task.getName() + " in " + botExecutor.getBot());
            } else if (botTaskConfiguration.isSequentialExecution()
                    && scheduledTasks.containsKey(new WorkflowSequentialBotTaskExecutor(botExecutor.getBot(), botTaskConfiguration, null))) {
                continue;
            }
            WorkflowBotTaskExecutor botTaskExecutor = botExecutor.createBotTaskExecutor(task);
            if (botTaskConfiguration != null && botTaskConfiguration.isSequentialExecution()) {
                List<WorkflowBotTaskExecutor> botTasks = sequentialTasks.get(task.getName());
                if (botTasks == null) {
                    botTasks = Lists.newLinkedList();
                    sequentialTasks.put(task.getName(), botTasks);
                }
                botTasks.add(botTaskExecutor);
            } else {
                ScheduledFuture<?> future = executor.schedule(botTaskExecutor, 200, TimeUnit.MILLISECONDS);
                scheduledTasks.put(botTaskExecutor, future);
            }
        }
        for (String taskName : sequentialTasks.keySet()) {
            WorkflowSequentialBotTaskExecutor botTaskExecutor = new WorkflowSequentialBotTaskExecutor(botExecutor.getBot(),
                    botExecutor.getBotTasks().get(taskName), sequentialTasks.get(taskName));
            ScheduledFuture<?> future = executor.schedule(botTaskExecutor, 200, TimeUnit.MILLISECONDS);
            scheduledTasks.put(botTaskExecutor, future);
        }
    }

    /**
     * Schedules new tasks for sequential bot.
     *
     * @param botExecutor
     *            Component, used to create new bot task executors.
     */
    private void scheduleSequentialBot(WorkflowBotExecutor botExecutor) {
        if (scheduledTasks.containsKey(new WorkflowSequentialBotTaskExecutor(botExecutor.getBot(), null, null))) {
            return;
        }
        List<WorkflowBotTaskExecutor> tasksToExecute = Lists.newLinkedList();
        Set<WfTask> tasks = botExecutor.getNewTasks();
        for (WfTask task : tasks) {
            tasksToExecute.add(botExecutor.createBotTaskExecutor(task));
        }
        WorkflowSequentialBotTaskExecutor botTaskExecutor = new WorkflowSequentialBotTaskExecutor(botExecutor.getBot(), null, tasksToExecute);
        ScheduledFuture<?> future = executor.schedule(botTaskExecutor, 200, TimeUnit.MILLISECONDS);
        scheduledTasks.put(botTaskExecutor, future);
    }

    private void logBotsActivites() {
        BotLogger botLogger = BotStationResources.createBotLogger();
        if (botLogger == null) {
            return;
        }
        botLogger.logActivity();
    }

    /**
     * forked from java.util.concurrent.Executors.DefaultThreadFactory due to private access to this class
     */
    private static class BotNamedThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1), threadNumber = new AtomicInteger(1);
        final ThreadGroup group;

        final String namePrefix;

        BotNamedThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            // adding custom prefix
            namePrefix = "bot-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
