package ru.runa.wfe.commons.cache.common;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface TestCacheIface extends CacheImplementation {
    public Long cachedValue(long key);
}