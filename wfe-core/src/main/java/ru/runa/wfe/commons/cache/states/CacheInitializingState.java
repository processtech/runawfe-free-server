package ru.runa.wfe.commons.cache.states;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.transaction.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Cache lifetime state machine. Current state is initializing cache (lazy initialization is in progress).
 */
class CacheInitializingState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl> {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(CacheInitializingState.class);

    /**
     * Cache (proxy object prior to lazy initialization complete).
     */
    private final CacheImpl cache;

    /**
     * Initialization required flag. True, if initialization is required and false if initialization may be stopped.
     */
    private final AtomicBoolean initializationRequired = new AtomicBoolean(true);

    public CacheInitializingState(CacheImpl cache) {
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
    public StateCommandResultWithCache<CacheImpl> getCache(CacheStateMachineContext<CacheImpl> context,
            Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(CacheStateMachineContext<CacheImpl> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(null, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("beforeTransactionComplete must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context,
            CacheImpl commitingCache) {
        commitingCache.commitCache();
        return StateCommandResult.create(context.getStateFactory().createInitializedState(commitingCache));
    }

    @Override
    public void discard() {
        initializationRequired.set(false);
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl> context) {
        context.getCacheFactory().startDelayedInitialization(new CacheInitializationContextImpl<>(this, context.getCallback()));
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context) {
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null));
    }

    public boolean isInitializationStillRequired() {
        return initializationRequired.get();
    }
}
