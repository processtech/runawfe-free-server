package ru.runa.wfe.commons.cache.states;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Factory for creating states for cache state machine.
 *
 * @param <CacheImpl>
 *            Cache implementation type.
 */
public abstract class CacheStateFactory<CacheImpl extends CacheImplementation> {

    private CacheStateMachine<CacheImpl> owner = null;

    public final void setOwner(@NonNull CacheStateMachine<CacheImpl> owner) {
        Preconditions.checkState(this.owner == null);
        this.owner = owner;
    }

    protected final CacheStateMachine<CacheImpl> getOwner() {
        Preconditions.checkState(owner != null);
        return owner;
    }


    /**
     * Creates empty cache state. No cache initialized or initializing. No dirty transactions exists.
     *
     * @param cache
     *            Cache, which may be returned until initialization.
     * @return Return cache state machine state.
     */
    public abstract CacheState<CacheImpl> createEmptyState(CacheImpl cache);

    /**
     * Creates cache state for cache lazy initialization.
     *
     * @param cache
     *            Cache proxy, returned by state until cache initialization complete.
     * @return Return cache state machine state.
     */
    public abstract CacheState<CacheImpl> createInitializingState(CacheImpl cache);

    /**
     * Creates cache state for initialized, fully operational cache.
     *
     * @param cache
     *            Initialized, fully operational cache instance.
     * @return Return cache state machine state.
     */
    public abstract CacheState<CacheImpl> createInitializedState(CacheImpl cache);

    /**
     * Creates dirty cache state. All dirty transactions is passed to state via {@link DirtyTransactions} parameter.
     *
     * @param cache
     *            Cache, which may be returned by state.
     * @param dirtyTransactions
     *            All dirty transactions.
     * @return Return cache state machine state.
     */
    public abstract CacheState<CacheImpl> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions);
}
