package ru.runa.wfe.job.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.cache.CacheStatistic;

/**
 * Periodic action to drop cache usage statistic to log.
 * 
 * @author Konstantinov Aleksey
 */
@Component
public class CacheUsageStatisticLogger {

    @Scheduled(cron = "${timertask.cron.cache.usage.statistic.logger}")
    public void execute() {
        CacheStatistic.logCounters();
    }

}
