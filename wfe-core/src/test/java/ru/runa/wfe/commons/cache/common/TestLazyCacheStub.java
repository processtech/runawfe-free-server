package ru.runa.wfe.commons.cache.common;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

public final class TestLazyCacheStub implements TestCacheIface {
    @Override
    public void commitCache() {
    }

    @Override
    public CacheImplementation unlock() {
        return null;
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }

    @Override
    public Long cachedValue(long key) {
        return null;
    }
}