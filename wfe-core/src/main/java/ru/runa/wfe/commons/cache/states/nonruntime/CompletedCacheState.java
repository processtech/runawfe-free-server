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
 * Cache lifetime state machine. Current state is fully operational cache (cache is initialized).
 */
public class CompletedCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, NonRuntimeCacheContext> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(EmptyCacheState.class);

    /**
     * Current cache instance.
     */
    private final CacheImpl cache;

    /**
     * State context.
     */
    private final NonRuntimeCacheContext stateContext;

    public CompletedCacheState(CacheImpl cache, NonRuntimeCacheContext stateContext) {
        this.cache = cache;
        this.stateContext = stateContext;
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
    public StateCommandResultWithCache<CacheImpl, NonRuntimeCacheContext> getCache(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, NonRuntimeCacheContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> onChange(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        cache.onChange(changedObject);
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, cache);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(cache, dirtyTransaction, stateContext));
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean, NonRuntimeCacheContext> completeTransaction(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
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
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null, new NonRuntimeCacheContext()));
    }
}
