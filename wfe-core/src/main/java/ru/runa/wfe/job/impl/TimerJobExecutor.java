package ru.runa.wfe.job.impl;

import java.util.Date;
import java.util.function.Supplier;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.job.StartEventSubprocessTimerJob;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.job.TimerEventDefinitionTimerJob;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.lang.jpdl.WaitNode;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.user.User;
import ru.runa.wfe.util.LazyValue;

@CommonsLog
public class TimerJobExecutor {

    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TimerJobDao timerJobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private CurrentTokenDao currentTokenDao;

    @Transactional
    @Scheduled(fixedDelayString = "${timertask.period.millis.job.execution}")
    public void execute() {
        Long batchSize = SystemProperties.getJobExecutorBatchSize();
        Long expiredJobsCount = timerJobDao.getExpiredJobsCount();
        val jobs = timerJobDao.getExpiredJobs(batchSize);
        log.debug("Expired jobs: " + expiredJobsCount);
        if (expiredJobsCount > batchSize) {
            log.debug("Too many expired jobs. Processing first " + batchSize);
        }
        if (jobs.isEmpty()) {
            return;
        }
        val systemUser = new LazyValue<User>(new Supplier<User>() {
            @Override
            public User get() {
                return authenticationLogic.authenticate(
                        SystemProperties.getAdministratorName(),
                        SystemProperties.getAdministratorDefaultPassword()
                );
            }
        });
        for (val job : jobs) {
            try {
                log.debug("executing " + job);
                // ATTENTION! Don't move logic to virtual TimerJob.execute(). This is anemic (passive entities, logic in controllers).
                // TODO Use switch (job) { case InProcessTimerJob j -> ... } when it's available in java.
                if (job instanceof DueDateInProcessTimerJob) {
                    val j = (DueDateInProcessTimerJob) job;
                    if (j.getToken().hasEnded()) {
                        log.debug("Token already ended");
                        continue;
                    }
                    val processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
                    ParsedProcessDefinition parsed = processDefinitionLoader.getDefinition(j.getProcess());
                    ExecutionContext executionContext = new ExecutionContext(parsed, j.getToken());
                    log.info("Triggered " + j.getName() + " in " + executionContext);
                    // error handling does not work correctly (rm2427 is not ported from master)
                    if (executionContext.getNode() instanceof TimerNode) {
                        ((TimerNode) executionContext.getNode()).onTimerJob(executionContext, j);
                    } else {
                        log.info("Triggered " + j.getName() + " in " + executionContext);
                        WaitNode.onTimerJob(executionContext, j);
                    }
                } else if (job instanceof StartProcessTimerJob) {
                    processStartProcessTimerJob((StartProcessTimerJob) job, new Date(), systemUser);
                } else if (job instanceof StartEventSubprocessTimerJob) {
                    val j = (StartEventSubprocessTimerJob) job;
                    val processDefinition = processDefinitionLoader.getDefinition(j.getProcess());
                    Node startNode = processDefinition.getNode(j.getNodeId());
                    // creating new token because parent token already can be ENDED
                    CurrentToken timerToken = new CurrentToken(processDefinition, j.getProcess(), processDefinition.getNodeNotNull(j.getNodeId()));
                    currentTokenDao.create(timerToken);
                    startNode.leave(new ExecutionContext(processDefinition, timerToken));
                    adjustInformationForNextStart(j);
                } else {
                    throw new Exception("Unknown TimerJob subclass: " + job);
                }
            } catch (Exception e) {
                log.error("Error executing job " + job, e);
            }
        }
    }

    /**
     * If {@code job.getTimerEventNextDate()} is in the past more than 30 seconds, does not execute it; only adjusts next start time, etc.
     * See: #1243-55, {@link TimerEventDefinition#ALLOW_START_PROCESS_DELAY}, {@link TimerEventDefinition#parseDuration(String)}.
     */
    private void processStartProcessTimerJob(StartProcessTimerJob job, Date now, LazyValue<User> systemUser) {
        Long remainingCount = job.getTimerEventRemainingCount();

        // Start process.
        if ((remainingCount == null || remainingCount > 0) &&
                now.getTime() <= job.getTimerEventNextDate().getTime() + TimerEventDefinition.ALLOW_START_PROCESS_DELAY) {
            val processDefinition = processDefinitionLoader.getDefinition(job.getDefinitionId());
            for (StartNode startNode : processDefinition.getEventStartNodes()) {
                val def = startNode.getTimerEventDefinition();
                if (def == null) {
                    continue;
                }
                executionLogic.startProcess(systemUser.get(), processDefinition, startNode, null, null);
                // now supported only one timer start node, because bpm_job does not contain node_id column
                break;
            }
        }
        adjustInformationForNextStart(job);
    }

    private void adjustInformationForNextStart(TimerEventDefinitionTimerJob job) {
        Long remainingCount = job.getTimerEventRemainingCount();
        // If we currently process last job iteration, then no matter if job was expired by less than 30 seconds or more; delete it.
        if (remainingCount != null && remainingCount == 1) {
            timerJobDao.delete((TimerJob) job);
            return;
        }

        // This can be only TimeCycle, otherwise remainingCount would be 1 and we won't get here.
        val def = (TimerEventDefinition.TimeCycle) TimerEventDefinition.createFromTypeAndExpression(
                job.getTimerEventType(),
                job.getTimerEventExpression()
        );

        // One period (duration) is always already passed -- it's period before current job.timerEventNextDate.
        // If more periods are already passed (e.g. because site was down), skip all those periods too.
        val pair = TimerEventDefinition.computeNextEvent(def, new Date(), job.getTimerEventNextDate(), remainingCount, 1);
        if (pair == null) {
            timerJobDao.delete((TimerJob) job);
            return;
        }
        job.setTimerEventNextDate(pair.getValue1());
        job.setTimerEventRemainingCount(pair.getValue2());
        timerJobDao.update((TimerJob) job);
    }

}
