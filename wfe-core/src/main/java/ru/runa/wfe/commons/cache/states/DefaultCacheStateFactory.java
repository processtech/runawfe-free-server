package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

public class DefaultCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl> {

    public DefaultCacheStateFactory() {
        super();
    }

    @Override
    public CacheState<CacheImpl> createEmptyState(CacheImpl cache, Object context) {
        return EmptyCacheState.createEmptyState();
    }

    @Override
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache, Object context) {
        return new CacheInitializingState<CacheImpl>(cache);
    }

    @Override
    public CacheState<CacheImpl> createInitializedState(CacheImpl cache, Object context) {
        return new CompletedCacheState<CacheImpl>(cache);
    }

    @Override
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions, Object context) {
        return new DirtyCacheState<CacheImpl>(cache, dirtyTransactions);
    }
}
