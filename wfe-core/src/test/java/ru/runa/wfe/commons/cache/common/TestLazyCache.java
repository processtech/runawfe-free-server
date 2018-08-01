package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;

public final class TestLazyCache implements TestCacheIface {

    private final ConcurrentMap<Long, Long> cachedData = new ConcurrentHashMap<>();
    private static final AtomicInteger version = new AtomicInteger(0);
    private int cacheVersion;

    public TestLazyCache(CacheInitializationContext<TestCacheIface> context, ConcurrentMap<Long, Long> initialCachedData) {
        cachedData.putAll(initialCachedData);
        cacheVersion = version.get();
    }

    @Override
    public void commitCache() {
        cacheVersion = version.incrementAndGet();
    }

    @Override
    public CacheImplementation unlock() {
        return null;
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        if (cachedData.containsKey(changedObject.object)) {
            cachedData.remove(changedObject.object);
            return true;
        }
        return false;
    }

    @Override
    public Long cachedValue(long key) {
        return cachedData.get(key);
    }
}