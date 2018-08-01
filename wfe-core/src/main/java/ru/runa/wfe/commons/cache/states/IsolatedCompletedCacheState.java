package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache lifetime state machine. Current state is fully operational cache (cache is initialized).
 */
public class IsolatedCompletedCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    /**
     * Current cache instance.
     */
    private final CacheImpl cache;

    public IsolatedCompletedCacheState(CacheStateMachine<CacheImpl> owner, CacheImpl cache) {
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
        return cache;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(cache, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        log.error("onBeforeTransactionComplete must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        log.error("onAfterTransactionComplete must not be called on " + this);
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
    }
}
