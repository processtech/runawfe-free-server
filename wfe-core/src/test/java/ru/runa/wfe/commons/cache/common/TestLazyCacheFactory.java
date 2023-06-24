package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;

public final class TestLazyCacheFactory extends SMCacheFactory<TestCacheIface> {

    /**
     * Data, loaded to cache on buildCache method.
     */
    public final ConcurrentMap<Long, Long> initialCachedData = new ConcurrentHashMap<>();

    private TestLazyCacheFactoryCallback callback;

    public TestLazyCacheFactory(TestLazyCacheFactoryCallback callback) {
        super(Type.LAZY);
        this.setCallback(callback);
        for (long i = 1; i <= 10; ++i) {
            initialCachedData.put(i, i);
        }
    }

    @Override
    protected TestCacheIface createCacheStubImpl() {
        if (callback != null) {
            callback.beforeProxyCreation();
        }
        return new TestLazyCacheStub();
    }

    @Override
    protected TestCacheIface createCacheImpl(CacheInitializationProcessContext context) {
        if (callback != null) {
            callback.beforeCacheCreation();
        }
        return new TestLazyCache(initialCachedData);
    }

    public void setCallback(TestLazyCacheFactoryCallback callback) {
        this.callback = callback;
    }
}
