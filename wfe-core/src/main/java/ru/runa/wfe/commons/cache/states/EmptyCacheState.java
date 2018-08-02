package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache lifetime state machine. Current state is empty cache (initialization required).
 */
public class EmptyCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    public EmptyCacheState(CacheStateMachine<CacheImpl> owner) {
        super(owner);
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
        CacheImpl cache = getCacheFactory().createCache();
        if (getCacheFactory().hasDelayedInitialization) {
            return StateCommandResultWithCache.create(getStateFactory().createInitializingState(cache), cache);
        }
        cache.commitCache();
        return StateCommandResultWithCache.create(getStateFactory().createInitializedState(cache), cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        log.error("onAfterTransactionComplete must not be called on " + this);
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
    }
}
