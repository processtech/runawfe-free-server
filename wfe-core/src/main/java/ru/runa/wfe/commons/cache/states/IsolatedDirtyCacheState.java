package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Cache state with existing dirty transactions. State manages cache instances for every dirty transaction and for readonly transactions.
 */
public class IsolatedDirtyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, DefaultStateContext> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(IsolatedDirtyCacheState.class);

    /**
     * Transactions, which change cache persistent objects.
     */
    private final DirtyTransactions<CacheImpl> dirtyTransactions;

    /**
     * Current cache for not changing transactions.
     */
    private final CacheImpl cache;

    public IsolatedDirtyCacheState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions) {
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
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction) {
        CacheImpl currentCache = dirtyTransactions.getCache(transaction, cache);
        CacheState<CacheImpl, DefaultStateContext> nextState = null;
        if (currentCache == null) {
            currentCache = context.getCacheFactory().createCache();
            DirtyTransactions<CacheImpl> newDirty = dirtyTransactions;
            CacheImpl readCache = cache;
            if (dirtyTransactions.isDirtyTransaction(transaction)) {
                newDirty = dirtyTransactions.addDirtyTransactionAndClone(transaction, currentCache);
            } else {
                readCache = currentCache;
            }
            nextState = context.getStateFactory().createDirtyState(readCache, newDirty, null);
        }
        return StateCommandResultWithCache.create(nextState, currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(dirtyTransactions.getCache(transaction, cache));
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> onChange(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        CacheImpl currentCache = cache;
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(currentCache, newDirtyTransactions, null));
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResult.create(context.getStateFactory().createDirtyState(null, dirtyTransactions, null));
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean, DefaultStateContext> completeTransaction(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl, DefaultStateContext> nextDirtyState =
                    context.getStateFactory().createDirtyState(null, dirtyTransactionAfterRemove, null);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null, null), true);
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
