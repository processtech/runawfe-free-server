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
public class NonRuntimeCacheStateFactory<CacheImpl extends CacheImplementation> implements CacheStateFactory<CacheImpl, NonRuntimeCacheContext> {

    public NonRuntimeCacheStateFactory() {
        super();
    }

    @Override
    public CacheState<CacheImpl, NonRuntimeCacheContext> createEmptyState(CacheImpl cache, NonRuntimeCacheContext context) {
        if (context == null) {
            context = new NonRuntimeCacheContext();
        }
        return EmptyCacheState.createEmptyState(cache, context);
    }

    @Override
    public CacheState<CacheImpl, NonRuntimeCacheContext> createInitializingState(CacheImpl cache, NonRuntimeCacheContext context) {
        if (context == null) {
            context = new NonRuntimeCacheContext();
        }
        return new CacheInitializingState<CacheImpl>(cache, context);
    }

    @Override
    public CacheState<CacheImpl, NonRuntimeCacheContext> createInitializedState(CacheImpl cache, NonRuntimeCacheContext context) {
        if (context == null) {
            context = new NonRuntimeCacheContext();
        }
        return new CompletedCacheState<CacheImpl>(cache, context);
    }

    @Override
    public CacheState<CacheImpl, NonRuntimeCacheContext> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions,
            NonRuntimeCacheContext context) {
        if (context == null) {
            context = new NonRuntimeCacheContext();
        }
        return new DirtyCacheState<CacheImpl>(cache, dirtyTransactions, context);
    }
}
