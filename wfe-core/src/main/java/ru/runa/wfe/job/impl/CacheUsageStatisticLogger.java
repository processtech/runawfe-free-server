package ru.runa.wfe.job.impl;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.cache.CacheStatistic;

/**
 * Periodic action to drop cache usage statistic to log.
 * 
 * @author Konstantinov Aleksey
 */
@Component
public class CacheUsageStatisticLogger {

    public void execute() {
        CacheStatistic.logCounters();
    }

}
