package ru.runa.wfe.commons.cache.states.nonruntime;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.DirtyTransactions;
import ru.runa.wfe.commons.cache.states.StateCommandResult;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithCache;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithData;

/**
 * Cache lifetime state machine. Current state is dirty cache (at least one transaction changing cache persistent object).
 */
public class DirtyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl> {

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

    /**
     * State context.
     */
    private final NonRuntimeCacheContext stateContext;

    public DirtyCacheState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions, NonRuntimeCacheContext stateContext) {
        this.dirtyTransactions = dirtyTransactions;
        this.cache = cache;
        this.stateContext = stateContext;
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
    public StateCommandResultWithCache<CacheImpl> getCache(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        CacheImpl currentCache = cache;
        if (currentCache == null) {
            currentCache = context.getCacheFactory().createCache();
        }
        return new StateCommandResultWithCache<CacheImpl>(null, currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return new StateCommandResultWithCache<CacheImpl>(null, cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(CacheStateMachineContext<CacheImpl> context, Transaction transaction,
            ChangedObjectParameter changedObject) {
        cache.onChange(changedObject);
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, cache);
        return new StateCommandResult<CacheImpl>(context.getStateFactory().createDirtyState(cache, newDirtyTransactions, stateContext));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return StateCommandResult.stateNoChangedResult;
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl> nextDirtyState = context.getStateFactory().createDirtyState(cache, dirtyTransactionAfterRemove, stateContext);
            return new StateCommandResultWithData<CacheImpl, Boolean>(nextDirtyState, false);
        }
        return new StateCommandResultWithData<CacheImpl, Boolean>(context.getStateFactory().createInitializedState(cache, stateContext), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context, CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.stateNoChangedResult;
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl> context) {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context) {
        CacheState<CacheImpl> dirtyState = context.getStateFactory().createDirtyState(null, dirtyTransactions, new NonRuntimeCacheContext(null));
        return new StateCommandResult<CacheImpl>(dirtyState);
    }
}
