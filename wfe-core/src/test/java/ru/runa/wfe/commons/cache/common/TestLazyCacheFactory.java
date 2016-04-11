package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;
import ru.runa.wfe.commons.cache.sm.LazyInitializedCacheFactory;

public final class TestLazyCacheFactory implements LazyInitializedCacheFactory<TestCacheIface> {

    /**
     * Data, loaded to cache on buildCache method.
     */
    public final ConcurrentMap<Long, Long> initialCachedData = new ConcurrentHashMap<Long, Long>();

    private TestLazyCacheFactoryCallback callback;

    public TestLazyCacheFactory(TestLazyCacheFactoryCallback callback) {
        this.setCallback(callback);
        for (long i = 1; i <= 10; ++i) {
            initialCachedData.put(i, i);
        }
    }

    @Override
    public TestCacheIface createProxy() {
        if (callback != null) {
            callback.beforeProxyCreation();
        }
        return new TestLazyCacheProxy();
    }

    @Override
    public TestCacheIface buildCache(CacheInitializationContext<TestCacheIface> context) {
        if (callback != null) {
            callback.beforeCacheCreation();
        }
        return new TestLazyCache(context, initialCachedData);
    }

    public TestLazyCacheFactoryCallback getCallback() {
        return callback;
    }

    public void setCallback(TestLazyCacheFactoryCallback callback) {
        this.callback = callback;
    }
}