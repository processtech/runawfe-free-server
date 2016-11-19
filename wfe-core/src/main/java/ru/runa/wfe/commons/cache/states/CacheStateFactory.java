package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Factory for creating states for cache state machine.
 * 
 * @param <CacheImpl>
 *            Cache implementation type.
 */
public interface CacheStateFactory<CacheImpl extends CacheImplementation> {

    /**
     * Creates empty cache state. No cache initialized or initializing. No dirty transactions exists.
     * 
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl> createEmptyState();

    /**
     * Creates cache state for cache lazy initialization.
     * 
     * @param cache
     *            Cache proxy, returned by state until cache initialization complete.
     * 
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl> createInitializingState(CacheImpl cache);

    /**
     * Creates cache state for initialized, fully operational cache.
     * 
     * @param cache
     *            Initialized, fully operational cache instance.
     * 
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl> createInitializedState(CacheImpl cache);

    /**
     * Creates dirty cache state. All dirty transactions is passed to state via {@link DirtyTransactions<T>} parameter.
     * 
     * @param cache
     *            Cache, which may be returned by state.
     * @param dirtyTransactions
     *            All dirty transactions.
     * 
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions);
}
