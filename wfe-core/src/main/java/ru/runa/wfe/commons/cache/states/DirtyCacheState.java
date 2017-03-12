package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Cache lifetime state machine. Current state is dirty cache (at least one transaction changing cache persistent object).
 */
public class DirtyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, DefaultStateContext> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(DirtyCacheState.class);

    /**
     * Transactions, which change cache persistent objects.
     */
    private final DirtyTransactions<CacheImpl> dirtyTransactions;

    /**
     * Current cache. Changing transactions may invalidate only small part of cache, so we don't drop cache on every changes.
     */
    private final CacheImpl cache;

    public DirtyCacheState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
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
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction) {
        CacheImpl currentCache = cache;
        if (currentCache == null) {
            currentCache = context.getCacheFactory().createCache();
        }
        return StateCommandResultWithCache.createNoStateSwitch(currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> onChange(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        CacheImpl currentCache = cache;
        if (currentCache != null && !currentCache.onChange(changedObject)) {
            currentCache = null;
        }
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(currentCache, newDirtyTransactions, null));
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean, DefaultStateContext> completeTransaction(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        CacheImpl currentCache = cache;
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl, DefaultStateContext> nextDirtyState =
                    context.getStateFactory().createDirtyState(currentCache, dirtyTransactionAfterRemove, null);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        if (currentCache == null) {
            return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null, null), true);
        }
        CacheImpl completedCache = (CacheImpl) currentCache.unlock();
        if (completedCache == null) {
            return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null, null), true);
        }
        if (completedCache == currentCache) {
            log.error("unlock on cache must always return new cache instance or null. Do not use any sort of flags to unlock cache");
            return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null, null), true);
        }
        return StateCommandResultWithData.create(context.getStateFactory().createInitializedState(completedCache, null), true);
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> commitCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl, DefaultStateContext> context) {
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> dropCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context) {
        return StateCommandResult.create(context.getStateFactory().createDirtyState(null, dirtyTransactions, null));
    }
}
