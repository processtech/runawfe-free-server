package ru.runa.wfe.commons.cache.states.nonruntime;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;
import ru.runa.wfe.commons.cache.states.DirtyTransactions;

/**
 * Cache state factory for non runtime caches. Cache content may differs from database state for some time.
 *
 * @param <CacheImpl>
 *            Cache implementation type.
 */
public class NonRuntimeCacheStateFactory<CacheImpl extends CacheImplementation> extends CacheStateFactory<CacheImpl> {

    @Override
    public CacheState<CacheImpl> createEmptyState(CacheImpl cache) {
        return new EmptyCacheState<>(getOwner(), cache);
    }

    @Override
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache) {
        return new CacheInitializingState<>(getOwner(), cache);
    }

    @Override
    public CacheState<CacheImpl> createInitializedState(CacheImpl cache) {
        return new CompletedCacheState<>(getOwner(), cache);
    }

    @Override
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
        return new DirtyCacheState<>(getOwner(), cache, dirtyTransactions);
    }
}
