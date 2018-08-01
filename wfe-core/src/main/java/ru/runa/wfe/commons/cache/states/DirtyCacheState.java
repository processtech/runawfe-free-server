package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache lifetime state machine. Current state is dirty cache (at least one transaction changing cache persistent object).
 */
public class DirtyCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    /**
     * Transactions, which change cache persistent objects.
     */
    private final DirtyTransactions<CacheImpl> dirtyTransactions;

    /**
     * Current cache. Changing transactions may invalidate only small part of cache, so we don't drop cache on every changes.
     */
    private final CacheImpl cache;

    public DirtyCacheState(CacheStateMachine<CacheImpl> owner, CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
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
        return cache;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        CacheImpl currentCache = cache;
        if (currentCache == null) {
            currentCache = getCacheFactory().createCache();
        }
        return StateCommandResultWithCache.createNoStateSwitch(currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        CacheImpl currentCache = cache;
        if (currentCache != null && !currentCache.onChange(changedObject)) {
            currentCache = null;
        }
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(currentCache, newDirtyTransactions));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        CacheImpl currentCache = cache;
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl> nextDirtyState = getStateFactory().createDirtyState(currentCache, dirtyTransactionAfterRemove);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        if (currentCache == null) {
            return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
        }
        CacheImpl completedCache = (CacheImpl) currentCache.unlock();
        if (completedCache == null) {
            return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
        }
        if (completedCache == currentCache) {
            log.error("unlock on cache must always return new cache instance or null. Do not use any sort of flags to unlock cache");
            return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
        }
        return StateCommandResultWithData.create(getStateFactory().createInitializedState(completedCache), true);
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache() {
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransactions));
    }
}
