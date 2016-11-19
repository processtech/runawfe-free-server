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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple cache statistic counter.
 * @author Konstantinov Aleksey
 */
public class StatisticCounter {

    /**
     * Counter for cache hit on get operation events.
     */
    private final AtomicInteger hitOnGetCounter = new AtomicInteger();
    /**
     * Counter for cache miss on get operation events.
     */
    private final AtomicInteger missOnGetCounter = new AtomicInteger();
    /**
     * Counter for cache hit on contains operation events.
     */
    private final AtomicInteger hitOnContainsCounter = new AtomicInteger();
    /**
     * Counter for cache miss on contains operation events.
     */
    private final AtomicInteger missOnContainsCounter = new AtomicInteger();
    /**
     * Counter for cache rebuild events.
     */
    private final AtomicInteger rebuildCounter = new AtomicInteger();
    /**
     * Counter for cache commit events.
     */
    private final AtomicInteger commitCounter = new AtomicInteger();

    /**
     * Counter creation date. 
     */
    private final long creationDate;

    /**
     * Counter last reset statistic date. 
     */
    private final AtomicLong resetDate;

    /**
     * Default constructor. All counters set to zero. 
     * Creation date and last reset date set to current date.
     */
    public StatisticCounter() {
        creationDate = System.currentTimeMillis();
        resetDate = new AtomicLong(creationDate);
    }

    /**
     * Create counter copy.
     * All counters copies to created instance and set to zero in source {@linkplain StatisticCounter}.
     * Creation date set to current date; last reset date copies from source {@linkplain StatisticCounter} and reset to current date.  
     */
    public StatisticCounter(StatisticCounter counter) {
        hitOnGetCounter.set(counter.hitOnGetCounter.getAndSet(0));
        missOnGetCounter.set(counter.missOnGetCounter.getAndSet(0));
        hitOnContainsCounter.set(counter.hitOnContainsCounter.getAndSet(0));
        missOnContainsCounter.set(counter.missOnContainsCounter.getAndSet(0));
        rebuildCounter.set(counter.rebuildCounter.getAndSet(0));
        commitCounter.set(counter.commitCounter.getAndSet(0));
        creationDate = System.currentTimeMillis();
        resetDate = new AtomicLong(counter.resetDate.getAndSet(creationDate));
    }

    /**
     * Register cache hit on get operation event.
     */
    public void registerCacheGetHit() {
        hitOnGetCounter.incrementAndGet();
    }

    /**
     * Register cache miss on get operation event.
     */
    public void registerCacheGetMiss() {
        missOnGetCounter.incrementAndGet();
    }

    /**
     * Register cache hit on contains operation event.
     */
    public void registerCacheContainsHit() {
        hitOnContainsCounter.incrementAndGet();
    }

    /**
     * Register cache miss on contains operation event.
     */
    public void registerCacheContainsMiss() {
        missOnContainsCounter.incrementAndGet();
    }

    /**
     * Register cache rebuild event.
     */
    public void registerCacheRebuild() {
        rebuildCounter.incrementAndGet();
    }

    /**
     * Register cache commit event.
     */
    public void registerCacheCommit() {
        commitCounter.incrementAndGet();
    }

    /**
     * Register get operation from cache. Fires miss or hit on get event depends on loaded from cache value.
     * @param <V> Type of values, loaded from cache.
     * @param value Value, loaded from cache.
     * @return Returns value, loaded from cache.
     */
    public <V> V registerCacheGet(V value) {
        if (value == null) {
            registerCacheGetMiss();
        } else {
            registerCacheGetHit();
        }
        return value;
    }

    /**
     * Register contains operation with cache. Fires miss or hit on contains event.
     * @param contains Flag, equals true, is cache contains element and false otherwise.
     * @return Flag, equals true, is cache contains element and false otherwise.
     */
    public boolean registerCacheContains(boolean contains) {
        if (contains) {
            registerCacheContainsHit();
        } else {
            registerCacheContainsMiss();
        }
        return contains;
    }

    /**
     * @return Cache hit counts on get operation.
     */
    public int getHitOnGetValue() {
        return hitOnGetCounter.get();
    }

    /**
     * @return Cache miss counts on get operation.
     */
    public int getMissOnGetValue() {
        return missOnGetCounter.get();
    }

    /**
     * @return Cache hit counts on contains operation events.
     */
    public int getHitOnContainsValue() {
        return hitOnContainsCounter.get();
    }

    /**
     * @return Cache miss counts on contains operation events.
     */
    public int getMissOnContainsValue() {
        return missOnContainsCounter.get();
    }

    /**
     * @return Cache rebuild counts.
     */
    public int getRebuildValue() {
        return rebuildCounter.get();
    }

    /**
     * @return Cache commit counts.
     */
    public int getCommitValue() {
        return commitCounter.get();
    }

    /**
     * @return Counter creation date.
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * @return Counter last reset statistic date.
     */
    public long getResetDate() {
        return resetDate.get();
    }
}
