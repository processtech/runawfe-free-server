package ru.runa.wfe.commons.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * Proxy class for {@linkplain Cache} implementations to save cache usage statistic.
 * @author Konstantinov Aleksey
 */
class CacheStatisticProxy<K extends Serializable, V extends Serializable> extends Cache<K, V> {

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
