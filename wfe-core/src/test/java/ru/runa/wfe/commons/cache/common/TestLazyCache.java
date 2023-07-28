package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

public final class TestLazyCache implements TestCacheIface {

    private final ConcurrentMap<Long, Long> cachedData = new ConcurrentHashMap<>();
    private static final AtomicInteger version = new AtomicInteger(0);

    public TestLazyCache(ConcurrentMap<Long, Long> initialCachedData) {
        cachedData.putAll(initialCachedData);
    }

    @Override
    public void commitCache() {
        version.incrementAndGet();
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