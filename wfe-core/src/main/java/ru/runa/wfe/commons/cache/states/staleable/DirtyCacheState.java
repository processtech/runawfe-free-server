package ru.runa.wfe.commons.cache.states.staleable;

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
            currentCache = getCacheFactory().createCacheOrStub();
        }
        return StateCommandResultWithCache.createNoStateSwitch(currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        if (cache != null) {
            cache.onChange(changedObject);
        }
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, cache);
        return StateCommandResult.create(getStateFactory().createDirtyState(cache, newDirtyTransactions));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl> nextDirtyState = getStateFactory().createDirtyState(cache, dirtyTransactionAfterRemove);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(cache), true);
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache() {
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransactions));
    }
}
