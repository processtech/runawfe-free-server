package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

public class DefaultCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl, DefaultStateContext> {

    public DefaultCacheStateFactory() {
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
        return new CompletedCacheState<CacheImpl>(cache);
    }

    @Override
    public CacheState<CacheImpl, DefaultStateContext> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions,
            DefaultStateContext context) {
        return new DirtyCacheState<CacheImpl>(cache, dirtyTransactions);
    }
}
