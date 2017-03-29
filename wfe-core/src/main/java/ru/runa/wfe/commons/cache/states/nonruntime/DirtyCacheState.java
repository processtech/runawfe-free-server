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
public class DirtyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, NonRuntimeCacheContext> {

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
    public StateCommandResultWithCache<CacheImpl, NonRuntimeCacheContext> getCache(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        CacheImpl currentCache = cache;
        if (currentCache == null) {
            currentCache = context.getCacheFactory().createCache();
        }
        return StateCommandResultWithCache.createNoStateSwitch(currentCache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, NonRuntimeCacheContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> onChange(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        if (cache != null) {
            cache.onChange(changedObject);
        }
        DirtyTransactions<CacheImpl> newDirtyTransactions = dirtyTransactions.addDirtyTransactionAndClone(transaction, cache);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(cache, newDirtyTransactions, stateContext));
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean, NonRuntimeCacheContext> completeTransaction(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        DirtyTransactions<CacheImpl> dirtyTransactionAfterRemove = dirtyTransactions.removeDirtyTransactionAndClone(transaction);
        if (dirtyTransactionAfterRemove.isLocked()) {
            CacheState<CacheImpl, NonRuntimeCacheContext> nextDirtyState =
                    context.getStateFactory().createDirtyState(cache, dirtyTransactionAfterRemove, stateContext);
            return StateCommandResultWithData.create(nextDirtyState, false);
        }
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(cache, stateContext), true);
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> commitCache(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context,
            CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context) {
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> dropCache(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context) {
        CacheState<CacheImpl, NonRuntimeCacheContext> dirtyState =
                context.getStateFactory().createDirtyState(null, dirtyTransactions, new NonRuntimeCacheContext());
        return StateCommandResult.create(dirtyState);
    }
}
