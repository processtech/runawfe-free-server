package ru.runa.wfe.commons.cache.states;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache lifetime state machine. Current state is initializing cache (lazy initialization is in progress).
 */
class CacheInitializingState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    /**
     * Cache (proxy object prior to lazy initialization complete).
     */
    private final CacheImpl cache;

    /**
     * Initialization required flag. True, if initialization is required and false if initialization may be stopped.
     */
    private final AtomicBoolean initializationRequired = new AtomicBoolean(true);

    public CacheInitializingState(CacheStateMachine<CacheImpl> owner, CacheImpl cache) {
        super(owner);
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
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> onBeforeTransactionComplete(Transaction transaction) {
        log.error("onBeforeTransactionComplete must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> onAfterTransactionComplete(Transaction transaction) {
        log.error("onAfterTransactionComplete must not be called on " + this);
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheImpl commitingCache) {
        commitingCache.commitCache();
        return StateCommandResult.create(getStateFactory().createInitializedState(commitingCache));
    }

    @Override
    public void discard() {
        initializationRequired.set(false);
    }

    @Override
    public void accept() {
        getCacheFactory().createCacheDelayed(new CacheInitializationContextImpl<>(this, getStateMachine()));
    }

    public boolean isInitializationStillRequired() {
        return initializationRequired.get();
    }
}
