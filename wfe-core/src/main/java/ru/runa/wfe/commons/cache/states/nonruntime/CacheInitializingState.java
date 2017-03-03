package ru.runa.wfe.commons.cache.states.nonruntime;

import java.util.concurrent.atomic.AtomicBoolean;

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
 * Cache lifetime state machine. Current state is initializing cache (lazy initialization is in progress).
 */
class CacheInitializingState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, NonRuntimeCacheContext> {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(CacheInitializingState.class);

    /**
     * Cache (proxy object prior to lazy initialization complete).
     */
    private final CacheImpl cache;

    /**
     * State context.
     */
    private final NonRuntimeCacheContext stateContext;

    /**
     * Initialization required flag. True, if initialization is required and false if initialization may be stopped.
     */
    private final AtomicBoolean initializationRequired = new AtomicBoolean(true);

    public CacheInitializingState(CacheImpl cache, NonRuntimeCacheContext stateContext) {
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
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, cache);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(cache, dirtyTransaction, stateContext));
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context, Transaction transaction) {
        log.error("beforeTransactionComplete must not be called on " + this);
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
            CacheImpl commitingCache) {
        commitingCache.commitCache();
        return StateCommandResult.create(context.getStateFactory().createInitializedState(commitingCache, new NonRuntimeCacheContext()));
    }

    @Override
    public void discard() {
        initializationRequired.set(false);
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context) {
        context.getCacheFactory().startDelayedInitialization(new CacheInitializationContextImpl<CacheImpl>(this, context.getCallback()));
    }

    @Override
    public StateCommandResult<CacheImpl, NonRuntimeCacheContext> dropCache(CacheStateMachineContext<CacheImpl, NonRuntimeCacheContext> context) {
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null, new NonRuntimeCacheContext()));
    }

    public boolean isInitializationStillRequired() {
        return initializationRequired.get();
    }
}
