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
    private final Cache<K, V> delegate;

    /**
     * Statistic counter for cache.
     */
    private final StatisticCounter stats;

    public CacheStatisticProxy(Cache<K, V> delegate, String counterName) {
        super();
        this.delegate = delegate;
        stats = CacheStatistic.registerCacheCounter(counterName);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void commitCache() {
        stats.registerCacheCommit();
        delegate.commitCache();
    }

    @Override
    public boolean contains(K key) {
        return stats.registerCacheContains(delegate.contains(key));
    }

    @Override
    public V get(K key) {
        return stats.registerCacheGet(delegate.get(key));
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> collection) {
        delegate.putAll(collection);
    }

    @Override
    public boolean remove(K key) {
        return delegate.remove(key);
    }

    @Override
    public Iterable<K> keySet() {
        return delegate.keySet();
    }
}
