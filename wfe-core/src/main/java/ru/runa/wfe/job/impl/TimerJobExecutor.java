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
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.EventSubprocessStartNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

@CommonsLog
public class TimerJobExecutor {

    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Transactional
    @Scheduled(fixedDelayString = "${timertask.period.millis.job.execution}")
    public void execute() {
        Long batchSize = SystemProperties.getJobExecutorBatchSize();
        Long expiredJobsCount = jobDao.getExpiredJobsCount();
        val jobs = jobDao.getExpiredJobs(batchSize);
        log.debug("Expired jobs: " + expiredJobsCount);
        if (expiredJobsCount > batchSize) {
            log.debug("Too many expired jobs. Processing first " + batchSize);
        }
        if (jobs.isEmpty()) {
            return;
        }
        for (val job : jobs) {
            try {
                log.debug("executing " + job);
                if (job instanceof StartProcessTimerJob) {
                    processStartProcessTimerJob((StartProcessTimerJob) job, new Date());
                } else {
                    throw new Exception("Unknown TimerJob subclass: " + job);
                }
            } catch (Exception e) {
                log.error("Error executing job " + job, e);
            }
        }
    }

    private void processStartProcessTimerJob(StartProcessTimerJob job, Date now) {
        Long remainingCount = job.getTimerEventRemainingCount();

        // Start process.
        if ((remainingCount == null || remainingCount > 0) &&
                now.getTime() <= job.getTimerEventNextDate().getTime() + TimerEventDefinition.ALLOW_START_PROCESS_DELAY) {
            val processDefinition = processDefinitionLoader.getDefinition(job.getDefinitionId());
            if (job.getToken() != null) {
                Node startNode = job.getToken().getNodeNotNull(processDefinition);
                if (startNode instanceof EventSubprocessStartNode) {
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, job.getToken());
                    ((EventSubprocessStartNode) startNode).onTimerJob(executionContext, remainingCount);
                }
            }
        }

        // Adjust information for next start.

        // If we currently process last job iteration, then no matter if job was expired by less than 30 seconds or more; delete it.
        if (remainingCount != null && remainingCount == 1) {
            jobDao.delete(job);
            return;
        }

        // This can be only TimeCycle, otherwise remainingCount would be 1 and we won't get here.
        val def = (TimerEventDefinition.TimeCycle) TimerEventDefinition.createFromTypeAndExpression(
                job.getTimerEventType(),
                job.getTimerEventExpression()
        );

        // One period (duration) is always already passed -- it's period before current job.timerEventNextDate.
        // If more periods are already passed (e.g. because site was down), skip all those periods too.
        val pair = TimerEventDefinition.computeNextEvent(def, now, job.getTimerEventNextDate(), remainingCount, 1);
        if (pair == null) {
            jobDao.delete(job);
            return;
        }
        job.setTimerEventNextDate(pair.getValue1());
        job.setTimerEventRemainingCount(pair.getValue2());
    }

}
