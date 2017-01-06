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
public class NonRuntimeCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl> {

    public NonRuntimeCacheStateFactory() {
        super();
    }

    @Override
    public CacheState<CacheImpl> createEmptyState(CacheImpl cache, Object context) {
        NonRuntimeCacheContext stateContext = (NonRuntimeCacheContext) context;
        if (stateContext == null) {
            stateContext = new NonRuntimeCacheContext();
        }
        return EmptyCacheState.createEmptyState(cache, stateContext);
    }

    @Override
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache, Object context) {
        NonRuntimeCacheContext stateContext = (NonRuntimeCacheContext) context;
        if (stateContext == null) {
            stateContext = new NonRuntimeCacheContext();
        }
        return new CacheInitializingState<CacheImpl>(cache, stateContext);
    }

    @Override
    public CacheState<CacheImpl> createInitializedState(CacheImpl cache, Object context) {
        NonRuntimeCacheContext stateContext = (NonRuntimeCacheContext) context;
        if (stateContext == null) {
            stateContext = new NonRuntimeCacheContext();
        }
        return new CompletedCacheState<CacheImpl>(cache, stateContext);
    }

    @Override
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions, Object context) {
        NonRuntimeCacheContext stateContext = (NonRuntimeCacheContext) context;
        if (stateContext == null) {
            stateContext = new NonRuntimeCacheContext();
        }
        return new DirtyCacheState<CacheImpl>(cache, dirtyTransactions, stateContext);
    }
}
