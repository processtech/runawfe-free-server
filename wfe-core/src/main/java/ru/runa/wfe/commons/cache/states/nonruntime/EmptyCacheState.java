package ru.runa.wfe.commons.cache.states.nonruntime;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.DirtyTransactions;
import ru.runa.wfe.commons.cache.states.StateCommandResult;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithCache;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithData;

/**
 * Cache lifetime state machine for non runtime caches. Current state is empty cache (initialization required).
 */
public class EmptyCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    /**
     * Current cache implementation.
     */
    private final CacheImpl cache;

    public EmptyCacheState(CacheStateMachine<CacheImpl> owner, CacheImpl cache) {
        super(owner);
        this.cache = cache;
    }

    @Override
    public boolean isDirtyTransactionExists() {
        return false;
    }

    @Override
    public boolean isDirtyTransaction(Transaction transaction) {
        return false;
    }

    @Override
    public CacheImpl getCacheQuickNoBuild(Transaction transaction) {
        return null;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        return initiateCacheCreation();
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return initiateCacheCreation();
    }

    /**
     * Create cache and start delayed initialization if required.
     *
     * @return Return next state for state machine.
     */
    private StateCommandResultWithCache<CacheImpl> initiateCacheCreation() {
        if (getCacheFactory().hasDelayedInitialization) {
            CacheImpl cache = this.cache != null ? this.cache : getCacheFactory().createCache();
            return StateCommandResultWithCache.create(getStateFactory().createInitializingState(cache), cache);
        }
        CacheImpl cache = getCacheFactory().createCache();
        cache.commitCache();
        return StateCommandResultWithCache.create(getStateFactory().createInitializedState(cache), cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, cache);
        return StateCommandResult.create(getStateFactory().createDirtyState(cache, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        log.error("onAfterTransactionComplete must not be called on " + this);
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(cache), true);
    }
}
