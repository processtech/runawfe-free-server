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
package ru.runa.wfe.commons.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Statistic counters for cache usages.
 * @author Konstantinov Aleksey
 */
@CommonsLog
public class CacheStatistic {

    /**
     * Registered statistic counters. 
     */
    private static ConcurrentHashMap<String, StatisticCounter> counters = new ConcurrentHashMap<>();

    /**
     * Get statistic counter for specified cache. Register it, if this counter not register yet.
     * Also increments cache rebuild events count. 
     * @param cacheName Cache name.
     * @return statistic counter for cache.
     */
    public static StatisticCounter registerCacheCounter(String cacheName) {
        StatisticCounter counter = counters.get(cacheName);
        if (counter != null) {
            return counter;
        }
        counter = new StatisticCounter();
        StatisticCounter registeredCounter = counters.putIfAbsent(cacheName, counter);
        counter = registeredCounter == null ? counter : registeredCounter;
        counter.registerCacheRebuild();
        return counter;
    }

    /**
     * Log cache counters.
     */
    public static void logCounters() {
        StringBuilder logMessage = new StringBuilder("\n");
        Map<String, StatisticCounter> statisticSnapshot = getSnapshotAndReset();
        for (Map.Entry<String, StatisticCounter> entry : statisticSnapshot.entrySet()) {
            StatisticCounter counter = entry.getValue();
            long elapsedMillis = counter.getCreationDate() - counter.getResetDate();
            if (elapsedMillis <= 0) {
                elapsedMillis = 1;
            }
            logMessage.append("Statistic for counter '").append(entry.getKey()).append("'. Report interval from ").
                append(counter.getResetDate()).append(" to ").append(counter.getCreationDate()).
                append(" ( elapsed ").append(elapsedMillis).append(" milliseconds).\n");
            logMessage.append("  Rebuild: ").append(counter.getRebuildValue()).append(" (").
                append(getPerSecond(counter.getRebuildValue(), elapsedMillis)).append(" per second); ");
            logMessage.append("Commit: ").append(counter.getCommitValue()).append(" (").
                append(getPerSecond(counter.getCommitValue(), elapsedMillis)).append(" per second); ");
            logMessage.append("Hit on get: ").append(counter.getHitOnGetValue()).append(" (").
                append(getPerSecond(counter.getHitOnGetValue(), elapsedMillis)).append(" per second); ");
            logMessage.append("Miss on get: ").append(counter.getMissOnGetValue()).append(" (").
                append(getPerSecond(counter.getMissOnGetValue(), elapsedMillis)).append(" per second); ");
            logMessage.append("Hit on contains: ").append(counter.getHitOnContainsValue()).append(" (").
                append(getPerSecond(counter.getHitOnContainsValue(), elapsedMillis)).append(" per second); ");
            logMessage.append("Miss on contains: ").append(counter.getMissOnContainsValue()).append(" (").
                append(getPerSecond(counter.getMissOnContainsValue(), elapsedMillis)).append(" per second).\n");
        }
        if (!statisticSnapshot.isEmpty()) {
            log.debug(logMessage);
        }
    }

    private static String getPerSecond(long count, long elapsedMillis) {
        double value = count * 1000.0 / elapsedMillis;
        return String.format("%.2f", value);
    }

    /**
     * Create statistic counters snapshot. 
     * All counters is reseting to zero on snapshot creation.
     * @return Statistic counters snapshot.
     */
    private static Map<String, StatisticCounter> getSnapshotAndReset() {
        Map<String, StatisticCounter> snapshot = new HashMap<>();
        for (Map.Entry<String, StatisticCounter> counter : counters.entrySet()) {
            snapshot.put(counter.getKey(), new StatisticCounter(counter.getValue()));
        }
        return snapshot;
    }
}
