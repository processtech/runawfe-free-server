package ru.runa.wfe.job.impl;

import java.util.Date;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

@CommonsLog
@Component
public class TimerJobFactory {

    public StartProcessTimerJob createTimerJobFromTimerEventDefinition(TimerEventDefinition def, Long processDefinitionId) {
        val now = new Date();
        StartProcessTimerJob job;
        if (def instanceof TimerEventDefinition.TimeDate) {
            val d = (TimerEventDefinition.TimeDate) def;
            if (d.timestamp.getTime() < now.getTime()) {
                log.warn("TimerJob for processDefinitionId = " + processDefinitionId
                        + ": skipped, because timeDate is already in the past");
                return null;
            }
            job = new StartProcessTimerJob();
            job.setTimerEventNextDate(d.timestamp);
            job.setTimerEventRemainingCount(1L);
        } else if (def instanceof TimerEventDefinition.TimeDuration) {
            val d = (TimerEventDefinition.TimeDuration) def;
            job = new StartProcessTimerJob();
            job.setTimerEventNextDate(d.addDurationToDate(now));
            job.setTimerEventRemainingCount(1L);
        } else if (def instanceof TimerEventDefinition.TimeCycle) {
            val d = (TimerEventDefinition.TimeCycle) def;
            if (d.start == null) {
                job = new StartProcessTimerJob();
                job.setTimerEventNextDate(d.addDurationToDate(now));
                job.setTimerEventRemainingCount(d.count);
            } else if (d.start.getTime() >= now.getTime()) {
                job = new StartProcessTimerJob();
                job.setTimerEventNextDate(d.start);
                job.setTimerEventRemainingCount(d.count);
            } else {
                // d.start is in the past, so we must skip already passed (expired) periods (durations).
                val pair = TimerEventDefinition.computeNextEvent(d, now, d.start, d.count, 0);
                if (pair == null) {
                    log.warn("TimerJob for processDefinitionId = " + processDefinitionId
                            + ": skipped, because all timeCycle runs are already in the past");
                    return null;
                }
                job = new StartProcessTimerJob();
                job.setTimerEventNextDate(pair.getValue1());
                job.setTimerEventRemainingCount(pair.getValue2());
            }
        } else {
            throw new InternalApplicationException("Unknown TimerEventDefinition subclass: " + def);
        }
        job.setCreateDate(now);
        job.setTimerEventType(def.getType());
        job.setTimerEventExpression(def.getExpression());
        job.setDefinitionId(processDefinitionId);
        return job;
    }
}
