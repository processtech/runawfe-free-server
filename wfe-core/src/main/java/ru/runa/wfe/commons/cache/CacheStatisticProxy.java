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

import java.io.Serializable;
import java.util.Map;

/**
 * Proxy class for {@linkplain Cache} implementations to save cache usage statistic.
 * @author Konstantinov Aleksey
 */
class CacheStatisticProxy<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    /**
     * {@linkplain Cache} implementation to delegate calls. 
     */
    private final Cache<K, V> delegatedImpl;

    /**
     * Statistic counter for cache.
     */
    private final StatisticCounter stats;

    /**
     * @param delegatedImpl
     */
    public CacheStatisticProxy(Cache<K, V> delegatedImpl, String counterName) {
        super();
        this.delegatedImpl = delegatedImpl;
        stats = CacheStatistic.registerCacheCounter(counterName);
    }

    @Override
    public void clear() {
        delegatedImpl.clear();
    }

    @Override
    public void commitCache() {
        stats.registerCacheCommit();
        delegatedImpl.commitCache();
    }

    @Override
    public boolean contains(K key) {
        return stats.registerCacheContains(delegatedImpl.contains(key));
    }

    @Override
    public V get(K key) {
        return stats.registerCacheGet(delegatedImpl.get(key));
    }

    @Override
    public void put(K key, V value) {
        delegatedImpl.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> collection) {
        delegatedImpl.putAll(collection);
    }

    @Override
    public boolean remove(K key) {
        return delegatedImpl.remove(key);
    }

    @Override
    public Iterable<K> keySet() {
        return delegatedImpl.keySet();
    }
}
