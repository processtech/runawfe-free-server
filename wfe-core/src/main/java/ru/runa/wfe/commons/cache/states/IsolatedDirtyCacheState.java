package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache state with existing dirty transactions. State manages cache instances for every dirty transaction and for readonly transactions.
 */
@CommonsLog
public class IsolatedDirtyCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    /**
     * Transactions, which change cache persistent objects.
     */
    private final DirtyTransactions<CacheImpl> dirtyTransactions;

    /**
     * Current cache for not changing transactions.
     */
    private final CacheImpl cache;

    public IsolatedDirtyCacheState(CacheStateMachine<CacheImpl> owner, CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
        super(owner);
        this.dirtyTransactions = dirtyTransactions;
        this.cache = cache;
    }

    @Override
    public boolean isDirtyTransactionExists() {
        return true;
    }

    @Override
    public boolean isDirtyTransaction(Transaction transaction) {
        return dirtyTransactions.isDirtyTransaction(transaction);
    }

    @Override
    public CacheImpl getCacheQuickNoBuild(Transaction transaction) {
        return dirtyTransactions.getCache(transaction, cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        CacheImpl currentCache = dirtyTransactions.getCache(transaction, cache);
        CacheState<CacheImpl> nextState = null;
        if (currentCache == null) {
            currentCache = getCacheFactory().createCache();
            DirtyTransactions<CacheImpl> newDirty = dirtyTransactions;
            CacheImpl readCache = cache;
            if (dirtyTransactions.isDirtyTransaction(transaction)) {
                newDirty = dirtyTransactions.addDirtyTransactionAndClone(transaction, currentCache);
            } else {
                readCache = currentCache;
            }
            nextState = getStateFactory().createDirtyState(readCache, newDirty);
        }
        return StateCommandResultWithCache.create(nextState, currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(dirtyTransactions.getCache(transaction, cache));
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(cache, newDirtyTransactions));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransactions));
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl> nextDirtyState = getStateFactory().createDirtyState(null, dirtyTransactionAfterRemove);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept() {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache() {
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransactions));
    }
}
