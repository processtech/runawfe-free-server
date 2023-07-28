package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

public class IsolatedCacheStateFactory<CacheImpl extends CacheImplementation> extends CacheStateFactory<CacheImpl> {

    @Override
    public CacheState<CacheImpl> createEmptyState(CacheImpl cache) {
        return new EmptyCacheState<>(getOwner());
    }

    @Override
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache) {
        return new CacheInitializingState<>(getOwner(), cache);
    }

    @Override
    public CacheState<CacheImpl> createCompletedState(CacheImpl cache) {
        return new IsolatedCompletedCacheState<>(getOwner(), cache);
    }

    @Override
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
        return new IsolatedDirtyCacheState<>(getOwner(), cache, dirtyTransactions);
    }
}
