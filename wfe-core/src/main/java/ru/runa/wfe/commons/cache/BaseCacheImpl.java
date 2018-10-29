package ru.runa.wfe.commons.cache;

import com.google.common.collect.Queues;
import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base cache implementation. Contains support for cache versions.
 * 
 * @author Konstantinov Aleksey
 */
public abstract class BaseCacheImpl implements CacheImplementation {
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Static counter for cache version. Calls like GetExecutors/SetExecutors must be perform in consistent way: Cache must not caching executors list
     * if cache version changed during executors list loading.
     */
    private static AtomicInteger cacheVersion = new AtomicInteger(1);

    /**
     * Caches, used to store cached values.
     */
    private final ConcurrentLinkedQueue<Cache<? extends Serializable, ? extends Serializable>> caches = Queues.newConcurrentLinkedQueue();

    /**
     * Current cache version. Calls like GetExecutors/SetExecutors must be perform in consistent way: Cache must not caching executors list if cache
     * version changed during executors list loading.
     */
    private volatile int currentCacheVersion;

    /**
     * Creates base cache implementation.
     */
    protected BaseCacheImpl() {
        currentCacheVersion = cacheVersion.get();
    }

    @Override
    public final void commitCache() {
        for (Cache<? extends Serializable, ? extends Serializable> cache : caches) {
            cache.commitCache();
        }
        currentCacheVersion = cacheVersion.incrementAndGet();
    }

    /**
     * Creates versionned cached data model for future replacement cached data if required.
     * 
     * @param data
     *            Current data, stored in cache.
     * @return Returns versionned cached data model.
     */
    protected <TData> VersionedCacheData<TData> getVersionnedData(TData data) {
        return new VersionedCacheDataImpl<>(data, currentCacheVersion);
    }

    /**
     * Checks if cache data may be updated with new value. If cache version was changed since old cache data request when data may not be updated.
     * 
     * @param oldCachedData
     *            Updated data old state.
     * @return Returns true, if data may be updated and false otherwise.
     */
    protected <TData> boolean mayUpdateVersionnedData(VersionedCacheData<TData> oldCachedData) {
        if (oldCachedData == null) {
            return false;
        }
        return oldCachedData.getVersion() == currentCacheVersion;
    }

    /**
     * Create cache to store cached values.
     * 
     * @param <K>
     *            Key type.
     * @param <V>
     *            Value type.
     * @param cacheName
     *            Cache name.
     * @param infiniteLifeTime
     *            Flag equals true, if element lifetime must be infinite; false to use ehcache settings.
     * @return Cache to store cached values.
     */
    protected <K extends Serializable, V extends Serializable> Cache<K, V> createCache(String cacheName, boolean infiniteLifeTime) {
        Cache<K, V> result = new CacheStatisticProxy<>(new EhCacheSupport<>(cacheName, infiniteLifeTime), cacheName);
        caches.add(result);
        return result;
    }

    /**
     * Create cache to store cached values.
     *
     * @param <K>
     *            Key type.
     * @param <V>
     *            Value type.
     * @param cacheName
     *            Cache name.
     * @return Cache to store cached values.
     */
    protected <K extends Serializable, V extends Serializable> Cache<K, V> createCache(String cacheName) {
        return createCache(cacheName, false);
    }
}
