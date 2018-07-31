package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

public class DefaultCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl> {

    @Override
    public CacheState<CacheImpl> createEmptyState(CacheImpl cache) {
        return new EmptyCacheState<>();
    }

    @Override
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache) {
        return new CacheInitializingState<>(cache);
    }

    @Override
    public CacheState<CacheImpl> createInitializedState(CacheImpl cache) {
        return new CompletedCacheState<>(cache);
    }

    @Override
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
        return new DirtyCacheState<>(cache, dirtyTransactions);
    }
}
