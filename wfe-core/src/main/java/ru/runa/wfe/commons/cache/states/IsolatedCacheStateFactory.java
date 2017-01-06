package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

public class IsolatedCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl, DefaultStateContext> {

    public IsolatedCacheStateFactory() {
        super();
    }

    @Override
    public CacheState<CacheImpl, DefaultStateContext> createEmptyState(CacheImpl cache, DefaultStateContext context) {
        return EmptyCacheState.createEmptyState();
    }

    @Override
    public CacheState<CacheImpl, DefaultStateContext> createInitializingState(CacheImpl cache, DefaultStateContext context) {
        return new CacheInitializingState<CacheImpl>(cache);
    }

    @Override
    public CacheState<CacheImpl, DefaultStateContext> createInitializedState(CacheImpl cache, DefaultStateContext context) {
        return new IsolatedCompletedCacheState<CacheImpl>(cache);
    }

    @Override
    public CacheState<CacheImpl, DefaultStateContext> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions,
            DefaultStateContext context) {
        return new IsolatedDirtyCacheState<CacheImpl>(cache, dirtyTransactions);
    }
}
