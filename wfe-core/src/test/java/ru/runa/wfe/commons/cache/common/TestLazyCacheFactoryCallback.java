package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.atomic.AtomicInteger;

public class TestLazyCacheFactoryCallback {

    private final AtomicInteger proxyCreated = new AtomicInteger();
    private final AtomicInteger cacheCreated = new AtomicInteger();

    public void beforeProxyCreation() {
        proxyCreated.incrementAndGet();
    }

    public void beforeCacheCreation() {
        cacheCreated.incrementAndGet();
    }

    public int getProxyCreated() {
        return proxyCreated.get();
    }

    public int getCacheCreated() {
        return cacheCreated.get();
    }
}